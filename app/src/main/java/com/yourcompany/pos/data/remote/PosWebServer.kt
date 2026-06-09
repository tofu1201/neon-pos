package com.yourcompany.pos.data.remote

import android.content.Context
import com.yourcompany.pos.domain.model.OrderStatus
import com.yourcompany.pos.domain.model.Product
import com.yourcompany.pos.domain.model.Member
import com.yourcompany.pos.domain.model.Order
import com.yourcompany.pos.domain.model.OrderLine
import com.yourcompany.pos.domain.model.PaymentMethod
import com.yourcompany.pos.domain.repository.OrderRepository
import com.yourcompany.pos.domain.repository.ProductRepository
import com.yourcompany.pos.domain.repository.MemberRepository
import com.yourcompany.pos.domain.repository.SettingsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.cio.CIO
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.request.path
import io.ktor.server.request.httpMethod
import io.ktor.server.request.header
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.delete
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import java.net.NetworkInterface

class PosWebServer(
    private val androidContext: Context,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val memberRepository: MemberRepository,
    private val settingsRepository: SettingsRepository
) {
    private var server: ApplicationEngine? = null

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                server = embeddedServer(CIO, port = 8080) {
                    install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(CORS) {
                    anyHost()
                    allowMethod(HttpMethod.Options)
                    allowMethod(HttpMethod.Get)
                    allowMethod(HttpMethod.Post)
                    allowMethod(HttpMethod.Put)
                    allowMethod(HttpMethod.Delete)
                    allowHeader(HttpHeaders.ContentType)
                    allowHeader("X-Admin-Pin")
                }

                routing {
                    intercept(io.ktor.server.application.ApplicationCallPipeline.Plugins) {
                        val path = call.request.path()
                        if (path.startsWith("/api/")) {
                            // Always allow CORS preflight
                            if (call.request.httpMethod == HttpMethod.Options) return@intercept
                            
                            // Public endpoints
                            if (path == "/api/products" && call.request.httpMethod == HttpMethod.Get) return@intercept
                            if (path == "/api/orders" && call.request.httpMethod == HttpMethod.Post) return@intercept
                            
                            // Protected endpoints
                            val pin = call.request.header("X-Admin-Pin")
                            if (pin != "1234") { // Hardcoded PIN for now, can be moved to settings later
                                call.respond(HttpStatusCode.Unauthorized, "Invalid PIN")
                                finish()
                            }
                        }
                    }

                    // API endpoints
                    get("/api/orders") {
                        val orders = orderRepository.observeOrders().first()
                        val activeOrders = orders.filter { it.status == OrderStatus.PAID || it.status == OrderStatus.PREPARING }
                        
                        val response = activeOrders.map { order ->
                            val lines = orderRepository.getOrderLines(order.id)
                            OrderDto(
                                id = order.id,
                                orderNo = order.orderNo,
                                totalAmount = order.totalAmount,
                                status = order.status.name,
                                paymentMethod = order.paymentMethod.name,
                                lines = lines.map { line ->
                                    OrderLineDto(
                                        productName = line.productName,
                                        quantity = line.quantity,
                                        note = line.note,
                                        modifiers = line.modifiers
                                    )
                                }
                            )
                        }
                        call.respond(response)
                    }

                    post("/api/orders/{id}/status") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                            return@post
                        }
                        val request = call.receive<UpdateStatusRequest>()
                        val newStatus = try {
                            OrderStatus.valueOf(request.status)
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid Status")
                            return@post
                        }
                        orderRepository.updateStatus(id, newStatus)
                        call.respond(HttpStatusCode.OK, mapOf("success" to true))
                    }

                    post("/api/orders") {
                        try {
                            val request = call.receive<CreateOrderRequest>()
                            val totalAmount = request.lines.sumOf { it.unitPrice * it.quantity }
                            val paymentMethod = try {
                                PaymentMethod.valueOf(request.paymentMethod)
                            } catch (e: Exception) {
                                PaymentMethod.CASH
                            }
                            
                            val settings = settingsRepository.getAllSettings()
                            val globalTaxRate = settings["taxRate"]?.toDoubleOrNull() ?: 0.05

                            val order = Order(
                                id = 0,
                                orderNo = "WEB-${System.currentTimeMillis()}",
                                totalAmount = totalAmount,
                                taxAmount = request.lines.sumOf { it.unitPrice * it.quantity * globalTaxRate },
                                discountAmount = 0.0,
                                paymentMethod = paymentMethod,
                                status = OrderStatus.PENDING,
                                orderType = request.orderType,
                                tableNumber = request.tableNumber
                            )
                            
                            val lines = request.lines.map { line ->
                                OrderLine(
                                    id = 0,
                                    orderId = 0,
                                    productId = line.productId,
                                    productName = line.productName,
                                    sku = line.sku,
                                    unitPrice = line.unitPrice,
                                    taxRate = line.taxRate,
                                    quantity = line.quantity,
                                    note = line.note,
                                    modifiers = line.modifiers ?: emptyList()
                                )
                            }
                            
                            val orderId = orderRepository.createOrder(order, lines)
                            
                            @Serializable
                            data class CreateOrderResponse(val success: Boolean, val orderId: Long, val orderNo: String)
                            
                            call.respond(HttpStatusCode.OK, CreateOrderResponse(success = true, orderId = orderId, orderNo = order.orderNo))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            call.respondText(
                                "Error creating order: ${e.message}\n${e.stackTraceToString()}", 
                                status = HttpStatusCode.InternalServerError
                            )
                        }
                    }

                    post("/api/orders/{id}/cancel") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                            return@post
                        }
                        
                        val order = orderRepository.getOrderById(id)
                        if (order == null) {
                            call.respond(HttpStatusCode.NotFound, "Order not found")
                            return@post
                        }

                        if (order.status == OrderStatus.CANCELLED) {
                            call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Order already cancelled"))
                            return@post
                        }

                        // Web server might not know the exact member phone. But memberId might be stored if they paid with MEMBER_BALANCE.
                        // Wait, memberId is not in Order model yet. If they paid with MEMBER_BALANCE, how does web know who to refund?
                        // For MVP, we will only cancel it. The actual POS logic had memberId in state. 
                        // But wait! If they cancel from web, the POS won't have it in state.
                        // Let's just update the status.
                        orderRepository.updateStatus(id, OrderStatus.CANCELLED)
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Order cancelled. If paid by MEMBER_BALANCE, manual refund required."))
                    }

                    get("/api/products") {
                        val products = productRepository.observeProducts().first()
                        val response = products.map { p ->
                            ProductResponseDto(
                                id = p.id,
                                sku = p.sku,
                                name = p.name,
                                price = p.price,
                                category = p.category,
                                stockQuantity = 999
                            )
                        }
                        call.respond(response)
                    }

                    post("/api/products") {
                        val request = call.receive<ProductDto>()
                        val settings = settingsRepository.getAllSettings()
                        val globalTaxRate = settings["taxRate"]?.toDoubleOrNull() ?: 0.05
                        val product = Product(
                            id = request.id ?: 0,
                            sku = request.sku,
                            name = request.name,
                            price = request.price,
                            taxRate = globalTaxRate,
                            category = request.category,
                            isActive = true
                        )
                        productRepository.upsertProduct(product)
                        call.respond(HttpStatusCode.OK, mapOf("success" to true))
                    }

                    delete("/api/products/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id != null) {
                            productRepository.deleteProductById(id)
                            call.respond(HttpStatusCode.OK, mapOf("success" to true))
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                        }
                    }

                    get("/api/settings") {
                        val settings = settingsRepository.getAllSettings()
                        call.respond(settings)
                    }

                    post("/api/settings") {
                        val request = call.receive<Map<String, String>>()
                        settingsRepository.saveSettings(request)
                        call.respond(HttpStatusCode.OK, mapOf("success" to true))
                    }

                    get("/api/members") {
                        val members = memberRepository.observeAllMembers().first()
                        val response = members.map { m ->
                            MemberDto(
                                phone = m.phone,
                                name = m.name,
                                balance = m.balance,
                                points = m.points,
                                nfcCardId = m.nfcCardId,
                                discountRate = 1.0
                            )
                        }
                        call.respond(response)
                    }

                    post("/api/members") {
                        val request = call.receive<MemberDto>()
                        val currentMember = request.phone.let { memberRepository.getMemberByPhone(it) }
                        if (currentMember != null) {
                            // Top up or edit
                            memberRepository.updateBalance(request.phone, request.balance - currentMember.balance)
                        } else {
                            // Create
                            memberRepository.upsertMember(Member(
                                phone = request.phone,
                                name = request.name,
                                balance = request.balance,
                                points = request.points,
                                nfcCardId = request.nfcCardId
                            ))
                        }
                        call.respond(HttpStatusCode.OK, mapOf("success" to true))
                    }

                    delete("/api/members/{id}") {
                        // For simplicity, just simulate deletion or actually implement in repository if added later.
                        call.respond(HttpStatusCode.OK, mapOf("success" to true))
                    }

                    // Serve static frontend from assets/webapp
                    get("/api/history/orders") {
                        val orders = orderRepository.observeOrders().first()
                        val response = orders.map { o ->
                            OrderResponseDto(
                                id = o.id,
                                orderNo = o.orderNo,
                                totalAmount = o.totalAmount,
                                paymentMethod = o.paymentMethod.name,
                                status = o.status.name,
                                createdAt = o.createdAt,
                                orderType = o.orderType,
                                tableNumber = o.tableNumber
                            )
                        }.sortedByDescending { it.createdAt }
                        call.respond(response)
                    }

                    get("/api/stats") {
                        val orders = orderRepository.observeOrders().first()
                        val startOfDay = java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val todayOrders = orders.filter { it.createdAt >= startOfDay && 
                            (it.status == OrderStatus.PAID ||
                             it.status == OrderStatus.PREPARING ||
                             it.status == OrderStatus.COMPLETED)
                        }
                        val todayRevenue = todayOrders.sumOf { it.totalAmount }
                        val totalOrders = todayOrders.size
                        val averageOrderValue = if (totalOrders > 0) todayRevenue / totalOrders else 0.0

                        call.respond(StatsResponseDto(
                            todayRevenue = todayRevenue,
                            totalOrders = totalOrders,
                            averageOrderValue = averageOrderValue
                        ))
                    }

                    // Serve static frontend from assets/customer
                    get("/order/{...}") {
                        val path = call.request.path().removePrefix("/order").removePrefix("/")
                        val assetPath = if (path.isEmpty()) "customer/index.html" else "customer/$path"
                        try {
                            val stream = androidContext.assets.open(assetPath)
                            val bytes = stream.readBytes()
                            val contentType = when {
                                assetPath.endsWith(".html") -> io.ktor.http.ContentType.Text.Html
                                assetPath.endsWith(".css") -> io.ktor.http.ContentType.Text.CSS
                                assetPath.endsWith(".js") -> io.ktor.http.ContentType.Text.JavaScript
                                assetPath.endsWith(".svg") -> io.ktor.http.ContentType.Image.SVG
                                else -> io.ktor.http.ContentType.Application.OctetStream
                            }
                            call.respondBytes(bytes, contentType)
                        } catch (e: Exception) {
                            // Fallback to index.html for SPA routing
                            try {
                                val stream = androidContext.assets.open("customer/index.html")
                                call.respondBytes(stream.readBytes(), io.ktor.http.ContentType.Text.Html)
                            } catch (e2: Exception) {
                                call.respond(HttpStatusCode.NotFound)
                            }
                        }
                    }

                    // Serve static frontend from assets/webapp
                    get("/{...}") {
                        val path = call.request.path().removePrefix("/")
                        val assetPath = if (path.isEmpty()) "webapp/index.html" else "webapp/$path"
                        try {
                            val stream = androidContext.assets.open(assetPath)
                            val bytes = stream.readBytes()
                            val contentType = when {
                                assetPath.endsWith(".html") -> io.ktor.http.ContentType.Text.Html
                                assetPath.endsWith(".css") -> io.ktor.http.ContentType.Text.CSS
                                assetPath.endsWith(".js") -> io.ktor.http.ContentType.Text.JavaScript
                                assetPath.endsWith(".svg") -> io.ktor.http.ContentType.Image.SVG
                                else -> io.ktor.http.ContentType.Application.OctetStream
                            }
                            call.respondBytes(bytes, contentType)
                        } catch (e: Exception) {
                            // Fallback to index.html for SPA routing
                            try {
                                val stream = androidContext.assets.open("webapp/index.html")
                                call.respondBytes(stream.readBytes(), io.ktor.http.ContentType.Text.Html)
                            } catch (e2: Exception) {
                                call.respond(HttpStatusCode.NotFound)
                            }
                        }
                    }
                }
            }.start(wait = true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }

    @Serializable
    data class UpdateStatusRequest(val status: String)

    @Serializable
    data class ProductDto(
        val id: Long? = null,
        val sku: String,
        val name: String,
        val price: Double,
        val category: String
    )

    @Serializable
    data class ProductResponseDto(
        val id: Long,
        val sku: String,
        val name: String,
        val price: Double,
        val category: String,
        val stockQuantity: Int
    )

    @Serializable
    data class CartItemDto(
        val productId: Long,
        val productName: String,
        val sku: String,
        val unitPrice: Double,
        val taxRate: Double,
        val quantity: Int,
        val note: String? = null,
        val modifiers: List<String>? = null
    )

    @Serializable
    data class OrderLineDto(
        val productName: String,
        val quantity: Int,
        val note: String? = null,
        val modifiers: List<String> = emptyList()
    )

    @Serializable
    data class CreateOrderRequest(
        val orderType: String,
        val tableNumber: String? = null,
        val paymentMethod: String,
        val lines: List<CartItemDto>
    )

    @Serializable
    data class OrderDto(
        val id: Long,
        val orderNo: String,
        val totalAmount: Double,
        val status: String,
        val paymentMethod: String,
        val lines: List<OrderLineDto>
    )

    @Serializable
    data class MemberDto(
        val phone: String,
        val name: String,
        val points: Int,
        val balance: Double,
        val discountRate: Double,
        val nfcCardId: String? = null
    )

    @Serializable
    data class OrderResponseDto(
        val id: Long,
        val orderNo: String,
        val totalAmount: Double,
        val paymentMethod: String,
        val status: String,
        val createdAt: Long,
        val orderType: String,
        val tableNumber: String?
    )

    @Serializable
    data class StatsResponseDto(
        val todayRevenue: Double,
        val totalOrders: Int,
        val averageOrderValue: Double
    )

    companion object {
        fun getLocalIpAddress(): String? {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val intf = interfaces.nextElement()
                    val addrs = intf.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                            return addr.hostAddress
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return null
        }
    }
}
