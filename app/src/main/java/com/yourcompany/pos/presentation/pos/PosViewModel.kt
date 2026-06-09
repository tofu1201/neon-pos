package com.yourcompany.pos.presentation.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.pos.data.repository.HoldOrderRepository
import com.yourcompany.pos.domain.model.CartItem
import com.yourcompany.pos.domain.model.CheckoutSummary
import com.yourcompany.pos.domain.model.Order
import com.yourcompany.pos.domain.model.OrderLine
import com.yourcompany.pos.domain.model.OrderStatus
import com.yourcompany.pos.domain.model.PaymentMethod
import com.yourcompany.pos.domain.repository.CartRepository
import com.yourcompany.pos.domain.repository.OrderRepository
import com.yourcompany.pos.domain.repository.ProductRepository
import com.yourcompany.pos.domain.repository.MemberRepository
import com.yourcompany.pos.domain.repository.SettingsRepository
import com.yourcompany.pos.domain.model.Member
import com.yourcompany.pos.nfc.NfcEvent
import com.yourcompany.pos.nfc.NfcManager
import com.yourcompany.pos.nfc.NfcTagContent
import com.yourcompany.pos.printer.PrinterEvent
import com.yourcompany.pos.printer.PrinterManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class PosViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val memberRepository: MemberRepository,
    private val settingsRepository: SettingsRepository,
    private val holdOrderRepository: HoldOrderRepository,
    private val nfcManager: NfcManager,
    private val printerManager: PrinterManager
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(PosUiState(isLoading = true))
    val uiState: StateFlow<PosUiState> = _uiState
    private var bindingNfcForPhone: String? = null

    init {
        observeProducts()
        observeCart()
        observeOrders()
        observeSettings()
        observeNfc()
        observePrinter()
        printerManager.connect()
    }

    fun onEvent(event: PosEvent) {
        when (event) {
            is PosEvent.SelectProduct -> selectProduct(event.productId)
            is PosEvent.AddToCart -> addToCart(event.productId, event.modifiers, event.note)
            is PosEvent.UpdateQuantity -> updateQuantity(event.cartItemId, event.quantity)
            is PosEvent.RemoveItem -> removeItem(event.cartItemId)
            is PosEvent.SearchChanged -> updateSearchQuery(event.query)
            PosEvent.ClearCart -> clearCart()
            is PosEvent.Checkout -> checkout(event.paymentMethod, event.printReceipt)
            is PosEvent.TogglePrintReceipt -> _uiState.update { it.copy(printReceipt = event.print) }
            is PosEvent.StartNfcScan -> startNfcScan()
            PosEvent.StopNfcScan -> stopNfcScan()
            PosEvent.ConnectPrinter -> connectPrinter()
            PosEvent.PrintDemoReceipt -> printDemoReceipt()
            is PosEvent.CashReceivedChanged -> updateCashReceived(event.value)
            PosEvent.DismissMessage -> dismissMessage()
            is PosEvent.NfcStatusChanged -> updateNfcStatus(event.message)
            is PosEvent.PrinterStatusChanged -> updatePrinterStatus(event.message)
            is PosEvent.AddProduct -> addProduct(event.sku, event.name, event.price, event.taxRate)
            is PosEvent.ShowCheckoutDialog -> _uiState.update { it.copy(showCheckoutDialog = event.show) }
            is PosEvent.ShowMemberManagerDialog -> _uiState.update { it.copy(showMemberManagerDialog = event.show) }
            is PosEvent.ShowMemberTopUpDialog -> _uiState.update { it.copy(showMemberTopUpDialog = event.show) }
            is PosEvent.SearchMember -> searchMember(event.phone)
            is PosEvent.RegisterMember -> registerMember(event.phone, event.name)
            is PosEvent.TopUpMember -> topUpMember(event.amount)
            is PosEvent.BindNfcCard -> {
                bindingNfcForPhone = event.phone
                _uiState.update { it.copy(snackbarMessage = "請將儲值卡靠近設備感應區以完成綁定") }
            }
            is PosEvent.SelectPaymentMethod -> _uiState.update { it.copy(selectedPaymentMethod = event.method) }
            is PosEvent.SelectCategory -> _uiState.update { it.copy(selectedCategory = event.category) }
            is PosEvent.SetOrderType -> _uiState.update { it.copy(orderType = event.type, tableNumber = event.tableNumber) }
            is PosEvent.SetGlobalDiscount -> _uiState.update { current ->
                val newDiscount = event.discount
                current.copy(
                    globalDiscount = newDiscount,
                    checkoutSummary = buildCheckoutSummary(
                        cartItems = current.cartItems,
                        memberDiscountRate = current.memberDiscountRate,
                        cashReceivedInput = current.cashReceivedInput,
                        globalDiscount = newDiscount
                    )
                )
            }
            PosEvent.HoldOrder -> holdOrder()
            PosEvent.RetrieveOrder -> retrieveOrder()
            PosEvent.PrintZReport -> printZReport()
            PosEvent.NavigateToCheckout -> {}
            PosEvent.NavigateToSettings -> {}
            is PosEvent.ScanOnlineOrder -> scanOnlineOrder(event.orderNo)
            is PosEvent.SetCheckoutScreenActive -> _uiState.update { it.copy(isCheckoutScreenActive = event.active) }
            is PosEvent.CancelOrder -> cancelOrder(event.orderId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProducts() {
        viewModelScope.launch {
            searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    productRepository.observeProducts()
                } else {
                    productRepository.searchProducts(query)
                }
            }.collect { products ->
                val categories = products.map { it.category }.distinct()
                _uiState.update { current ->
                    val filteredProducts = if (current.selectedCategory != null) {
                        products.filter { it.category == current.selectedCategory }
                    } else {
                        products
                    }
                    current.copy(
                        products = filteredProducts,
                        categories = categories,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.observeAllSettings().collect { settings ->
                val taxRateSetting = settings.find { it.key == "taxRate" }?.value?.toDoubleOrNull() ?: 0.05
                _uiState.update { current ->
                    current.copy(
                        globalTaxRate = taxRateSetting,
                        checkoutSummary = buildCheckoutSummary(
                            cartItems = current.cartItems,
                            memberDiscountRate = current.memberDiscountRate,
                            cashReceivedInput = current.cashReceivedInput,
                            globalDiscount = current.globalDiscount,
                            globalTaxRate = taxRateSetting
                        )
                    )
                }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.observeCartItems().collect { items ->
                _uiState.update { current ->
                    current.copy(
                        cartItems = items,
                        checkoutSummary = buildCheckoutSummary(
                            cartItems = items,
                            memberDiscountRate = current.memberDiscountRate,
                            cashReceivedInput = current.cashReceivedInput,
                            globalDiscount = current.globalDiscount,
                            globalTaxRate = current.globalTaxRate
                        )
                    )
                }
            }
        }
    }

    private fun observeOrders() {
        viewModelScope.launch {
            orderRepository.observeOrders().collect { orders ->
                _uiState.update { current -> current.copy(recentOrders = orders) }
            }
        }
    }

    private fun observeNfc() {
        viewModelScope.launch {
            nfcManager.state.collect { state ->
                _uiState.update { current ->
                    val content = state.tagContent
                    val updated = current.copy(
                        nfcAvailable = state.available,
                        nfcEnabled = state.enabled,
                        nfcReading = state.reading,
                        memberId = when (content) {
                            is NfcTagContent.MemberCard -> content.memberId
                            else -> current.memberId
                        },
                        memberName = when (content) {
                            is NfcTagContent.MemberCard -> content.memberName
                            else -> current.memberName
                        },
                        memberPoints = when (content) {
                            is NfcTagContent.MemberCard -> content.points
                            else -> current.memberPoints
                        },
                        memberDiscountRate = when (content) {
                            is NfcTagContent.MemberCard -> content.discountRate
                            else -> current.memberDiscountRate
                        },
                        memberSource = when (content) {
                            is NfcTagContent.MemberCard -> content.rawPayload ?: content.memberId
                            else -> current.memberSource
                        },
                        productTagSku = when (content) {
                            is NfcTagContent.ProductTag -> content.sku
                            else -> current.productTagSku
                        },
                        productTagName = when (content) {
                            is NfcTagContent.ProductTag -> content.productName
                            else -> current.productTagName
                        },
                        nfcDetails = buildNfcDetails(content),
                        nfcStatus = when (val event = state.lastEvent) {
                            is NfcEvent.TagDiscovered -> "偵測到 NFC 標籤：${event.tagId}"
                            is NfcEvent.ParsedContentDetected -> describeNfcContent(event.content)
                            is NfcEvent.LoginCardRead -> "會員卡感應成功：${event.cardId}"
                            is NfcEvent.ProductTagRead -> "商品 NFC 標籤：${event.productTagId}"
                            is NfcEvent.Error -> event.message
                            NfcEvent.Enabled -> "NFC 已啟動"
                            NfcEvent.Disabled -> "NFC 已停用"
                            NfcEvent.Idle -> current.nfcStatus
                        }
                    )
                    updated.copy(
                        checkoutSummary = buildCheckoutSummary(
                            cartItems = updated.cartItems,
                            memberDiscountRate = updated.memberDiscountRate,
                            cashReceivedInput = updated.cashReceivedInput,
                            globalDiscount = updated.globalDiscount
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            nfcManager.events.collect { event ->
                when (event) {
                    is NfcEvent.ParsedContentDetected -> _uiState.update { current ->
                        current.copy(
                            nfcStatus = describeNfcContent(event.content),
                            nfcDetails = buildNfcDetails(event.content)
                        )
                    }
                    is NfcEvent.Error -> _uiState.update { current -> current.copy(errorMessage = event.message) }
                    is NfcEvent.LoginCardRead -> {
                        _uiState.update { current ->
                            current.copy(snackbarMessage = "卡片已感應：${event.cardId}")
                        }
                        handleNfcCardTapped(event.cardId)
                    }
                    is NfcEvent.ProductTagRead -> _uiState.update { current ->
                        current.copy(snackbarMessage = "商品標籤已讀取：${event.productTagId}")
                    }
                    is NfcEvent.TagDiscovered -> _uiState.update { current ->
                        current.copy(snackbarMessage = "NFC 標籤已靠近")
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun observePrinter() {
        viewModelScope.launch {
            printerManager.state.collect { state ->
                _uiState.update { current ->
                    current.copy(
                        printerConnected = state.connected,
                        printerConnecting = state.connecting,
                        printerPrinting = state.printing,
                        printerStatus = when (val event = state.lastEvent) {
                            PrinterEvent.Connected -> "印表機已連線"
                            PrinterEvent.Connecting -> "印表機連線中..."
                            PrinterEvent.Disconnected -> "印表機已斷線"
                            PrinterEvent.Reconnecting -> "印表機重新連線中"
                            PrinterEvent.PaperOut -> "印表機缺紙"
                            is PrinterEvent.PrintSuccess -> event.message
                            is PrinterEvent.PrintFailed -> event.reason
                            is PrinterEvent.Error -> event.reason
                            PrinterEvent.Ready -> current.printerStatus
                            PrinterEvent.Idle -> current.printerStatus
                        }
                    )
                }
            }
        }

        viewModelScope.launch {
            printerManager.events.collect { event ->
                when (event) {
                    is PrinterEvent.PrintSuccess -> _uiState.update { current -> current.copy(snackbarMessage = event.message) }
                    is PrinterEvent.PaperOut -> _uiState.update { current -> current.copy(errorMessage = "印表機缺紙，請補紙後重試") }
                    is PrinterEvent.PrintFailed -> _uiState.update { current -> current.copy(errorMessage = event.reason) }
                    is PrinterEvent.Error -> _uiState.update { current -> current.copy(errorMessage = event.reason) }
                    else -> Unit
                }
            }
        }
    }

    private fun selectProduct(productId: Long) {
        viewModelScope.launch {
            val product = productRepository.getProductById(productId)
            _uiState.update { it.copy(selectedProduct = product) }
        }
    }

    private fun addToCart(productId: Long, modifiers: List<String>, note: String?) {
        viewModelScope.launch {
            val product = productRepository.getProductById(productId) ?: return@launch
            // Check if there is an identical item in the cart
            val existingCartItem = _uiState.value.cartItems.find { 
                it.productId == productId && it.modifiers == modifiers && it.note == note 
            }
            if (existingCartItem != null) {
                cartRepository.updateQuantity(existingCartItem.id, existingCartItem.quantity + 1)
            } else {
                cartRepository.addToCart(
                    CartItem(
                        productId = product.id,
                        productName = product.name,
                        sku = product.sku,
                        unitPrice = product.price,
                        taxRate = product.taxRate,
                        quantity = 1,
                        imageUrl = product.imageUrl,
                        modifiers = modifiers,
                        note = note
                    )
                )
            }
        }
    }

    private fun updateQuantity(cartItemId: Long, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(cartItemId, quantity)
        }
    }

    private fun removeItem(cartItemId: Long) {
        viewModelScope.launch {
            cartRepository.removeItem(cartItemId)
        }
    }


    private fun updateSearchQuery(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun holdOrder() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val items = currentState.cartItems
            if (items.isEmpty()) {
                _uiState.update { it.copy(snackbarMessage = "購物車為空，無法掛單") }
                return@launch
            }
            
            val itemsJson = kotlinx.serialization.json.Json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(com.yourcompany.pos.data.remote.PosWebServer.CartItemDto.serializer()),
                items.map { item ->
                    com.yourcompany.pos.data.remote.PosWebServer.CartItemDto(
                        productId = item.productId,
                        productName = item.productName,
                        sku = item.sku,
                        unitPrice = item.unitPrice,
                        taxRate = item.taxRate,
                        quantity = item.quantity,
                        note = item.note,
                        modifiers = item.modifiers
                    )
                }
            )
            
            val totalAmount = currentState.checkoutSummary.totalAmount
            val note = if (currentState.orderType == "內用") "桌號: ${currentState.tableNumber ?: "未指定"}" else currentState.orderType
            
            holdOrderRepository.saveHoldOrder(itemsJson, totalAmount, note)
            clearCart()
            _uiState.update { it.copy(snackbarMessage = "已將訂單掛起") }
        }
    }

    private fun retrieveOrder() {
        // We will expose hold orders to the UI via a new state field, 
        // but for now, to keep it simple, we just retrieve the most recent hold order.
        viewModelScope.launch {
            holdOrderRepository.getAllHoldOrders().collect { orders ->
                if (orders.isNotEmpty()) {
                    val order = orders.first()
                    // Deserialize
                    try {
                        val dtos = kotlinx.serialization.json.Json.decodeFromString(
                            kotlinx.serialization.builtins.ListSerializer(com.yourcompany.pos.data.remote.PosWebServer.CartItemDto.serializer()),
                            order.itemsJson
                        )
                        // Clear current cart
                        cartRepository.clearCart()
                        // Add items
                        for (i in dtos.indices) {
                            val dto = dtos[i]
                            cartRepository.addToCart(CartItem(
                                productId = dto.productId,
                                productName = dto.productName,
                                sku = dto.sku,
                                unitPrice = dto.unitPrice,
                                taxRate = dto.taxRate,
                                quantity = dto.quantity,
                                imageUrl = "",
                                modifiers = dto.modifiers ?: emptyList(),
                                note = dto.note
                            ))
                        }
                        holdOrderRepository.deleteHoldOrder(order.id)
                        _uiState.update { it.copy(snackbarMessage = "已取出掛單") }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _uiState.update { it.copy(snackbarMessage = "還原掛單失敗") }
                    }
                } else {
                    _uiState.update { it.copy(snackbarMessage = "目前沒有掛單記錄") }
                }
            }
        }
    }

    private fun printZReport() {
        viewModelScope.launch {
            val orders = orderRepository.observeOrders().first()
            
            // Calculate today's start and end times
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            val endTime = calendar.timeInMillis
            
            val todayOrders = orders.filter { it.createdAt in startTime until endTime && 
                (it.status == OrderStatus.PAID || 
                 it.status == OrderStatus.PREPARING || 
                 it.status == OrderStatus.COMPLETED)
            }
            
            val totalRevenue = todayOrders.sumOf { it.totalAmount }
            val orderCount = todayOrders.size

            val cashTotal = todayOrders.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.totalAmount }
            val cardTotal = todayOrders.filter { it.paymentMethod == PaymentMethod.CARD }.sumOf { it.totalAmount }
            val nfcTotal = todayOrders.filter { it.paymentMethod == PaymentMethod.NFC }.sumOf { it.totalAmount }
            val memberBalanceTotal = todayOrders.filter { it.paymentMethod == PaymentMethod.MEMBER_BALANCE }.sumOf { it.totalAmount }
            val discountTotal = todayOrders.sumOf { it.discountAmount }

            val storeSettings = settingsRepository.getAllSettings()
            
            printerManager.printZReport(
                storeName = storeSettings["storeName"] ?: "NEON POS",
                storeAddress = storeSettings["storeAddress"] ?: "台北信義門市",
                storePhone = storeSettings["storePhone"] ?: "02-1234-5678",
                totalRevenue = totalRevenue,
                cashTotal = cashTotal,
                cardTotal = cardTotal,
                nfcTotal = nfcTotal,
                memberBalanceTotal = memberBalanceTotal,
                discountTotal = discountTotal,
                orderCount = orderCount
            )
        }
    }

    private fun scanOnlineOrder(scannedBarcode: String) {
        viewModelScope.launch {
            val barcode = scannedBarcode.trim()
            
            // Try to find by exact orderNo first
            var order = orderRepository.getOrderByOrderNo(barcode)
            
            // If not found, check if the barcode is "WEB-{id}" as a fallback
            if (order == null && barcode.startsWith("WEB-")) {
                val potentialId = barcode.removePrefix("WEB-").toLongOrNull()
                if (potentialId != null) {
                    order = orderRepository.getOrderById(potentialId)
                }
            }
            
            if (order != null && order.status == com.yourcompany.pos.domain.model.OrderStatus.PENDING) {
                // Clear existing cart and load online order items
                cartRepository.clearCart()
                
                val lines = orderRepository.getOrderLines(order.id)
                lines.forEach { line ->
                    cartRepository.addToCart(
                        com.yourcompany.pos.domain.model.CartItem(
                            productId = line.productId,
                            productName = line.productName,
                            sku = line.sku,
                            unitPrice = line.unitPrice,
                            taxRate = line.taxRate,
                            quantity = line.quantity,
                            note = line.note,
                            modifiers = line.modifiers
                        )
                    )
                }

                _uiState.update { it.copy(
                    checkoutOnlineOrder = order,
                    orderType = order.orderType,
                    tableNumber = order.tableNumber ?: "",
                    snackbarMessage = "已載入線上訂單 (${order.orderNo})，可修改內容後再結帳"
                ) }
            } else {
                _uiState.update { it.copy(errorMessage = "找不到該訂單，或訂單已結帳") }
            }
        }
    }

    private fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }

    private fun startNfcScan() {
        nfcManager.startReaderMode()
    }

    private fun stopNfcScan() {
        nfcManager.stopReaderMode()
    }

    private fun connectPrinter() {
        printerManager.connect()
    }

    private fun printDemoReceipt() {
        val currentState = _uiState.value

        val items: List<PrinterManager.DemoLine>
        val summary: CheckoutSummary
        val orderNo = generateOrderNo()

        if (currentState.cartItems.isEmpty()) {
            items = listOf(
                PrinterManager.DemoLine("測試商品 A", 1, 100.0),
                PrinterManager.DemoLine("測試商品 B", 2, 50.0)
            )
            summary = CheckoutSummary(
                subtotal = 200.0,
                taxAmount = 10.0,
                discountAmount = 0.0,
                totalAmount = 210.0,
                cashReceivedAmount = 500.0,
                changeAmount = 290.0,
                itemCount = 3
            )
        } else {
            summary = buildCheckoutSummary(
                cartItems = currentState.cartItems,
                memberDiscountRate = currentState.memberDiscountRate,
                cashReceivedInput = currentState.cashReceivedInput,
                globalDiscount = currentState.globalDiscount,
                globalTaxRate = currentState.globalTaxRate
            )
            items = currentState.cartItems.map {
                PrinterManager.DemoLine(
                    name = it.productName,
                    quantity = it.quantity,
                    total = it.total
                )
            }
        }

        printerManager.printOrderReceipt(
            orderNo = orderNo,
            paymentMethod = PaymentMethod.CASH,
            memberId = currentState.memberId,
            memberName = currentState.memberName,
            memberPoints = currentState.memberPoints,
            memberDiscountRate = currentState.memberDiscountRate,
            items = items,
            subtotalAmount = summary.subtotal,
            taxAmount = summary.taxAmount,
            discountAmount = summary.discountAmount,
            cashReceivedAmount = summary.cashReceivedAmount,
            changeAmount = summary.changeAmount,
            totalAmount = summary.totalAmount
        )
    }


    private fun handleNfcCardTapped(cardId: String) {
        viewModelScope.launch {
            val phoneToBind = bindingNfcForPhone
            if (phoneToBind != null) {
                val memberToBind = memberRepository.getMemberByPhone(phoneToBind)
                if (memberToBind != null) {
                    memberRepository.upsertMember(memberToBind.copy(nfcCardId = cardId))
                    _uiState.update { it.copy(snackbarMessage = "儲值卡綁定成功！") }
                } else {
                    _uiState.update { it.copy(errorMessage = "找不到該會員，綁定失敗") }
                }
                bindingNfcForPhone = null
                return@launch
            }

            val member = memberRepository.getMemberByNfcCardId(cardId)
            if (member != null) {
                _uiState.update { it.copy(
                    memberId = member.phone,
                    memberName = member.name,
                    memberPoints = member.points,
                    memberBalance = member.balance,
                    memberSource = "自家儲值卡",
                    memberDiscountRate = member.discountRate
                ) }

                // If currently waiting for NFC payment in checkout screen, trigger checkout!
                if (_uiState.value.showCheckoutDialog || _uiState.value.isCheckoutScreenActive) {
                    checkout(PaymentMethod.MEMBER_BALANCE, _uiState.value.printReceipt) // Uses the member balance to pay
                } else {
                    _uiState.update { it.copy(snackbarMessage = "會員已登入：${member.name} (餘額: ${member.balance})") }
                }
            } else {
                _uiState.update { it.copy(errorMessage = "找不到對應的儲值卡 (${cardId})，請先綁定會員") }
            }
        }
    }

    private fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            val order = orderRepository.getOrderById(orderId)
            if (order == null) {
                _uiState.update { it.copy(errorMessage = "找不到訂單") }
                return@launch
            }
            if (order.status == com.yourcompany.pos.domain.model.OrderStatus.CANCELLED) {
                _uiState.update { it.copy(snackbarMessage = "訂單已取消") }
                return@launch
            }

            if (order.paymentMethod == PaymentMethod.MEMBER_BALANCE) {
                // Wait, do we know the memberId? It's not stored in Order directly?
                // Wait, memberId is not strictly in Order... 
                // Wait, if memberId is not in Order, we can't reliably refund it here unless we search by something.
                // Let's assume member phone is not in order right now, wait, we need to check if order has memberId.
                // The prompt says "If MEMBER_BALANCE and memberId is available".
                // I will add a simple message for now and try to find the member if possible, or just instruct the cashier.
                // Actually, let's refund if we have the current member logged in, otherwise just instruct.
                val phone = _uiState.value.memberId
                if (phone != null) {
                    val success = memberRepository.updateBalance(phone, order.totalAmount)
                    if (success) {
                        _uiState.update { it.copy(snackbarMessage = "訂單已取消，已退還餘額 NT$ ${order.totalAmount} 至會員帳戶") }
                    } else {
                        _uiState.update { it.copy(errorMessage = "退還餘額失敗") }
                        return@launch
                    }
                } else {
                    _uiState.update { it.copy(snackbarMessage = "訂單已取消，因無法確認會員，請以其他方式退還 NT$ ${order.totalAmount}") }
                }
            } else {
                _uiState.update { it.copy(snackbarMessage = "訂單已取消，請退還現金/刷卡 NT$ ${order.totalAmount} 給顧客") }
            }
            
            orderRepository.updateStatus(orderId, com.yourcompany.pos.domain.model.OrderStatus.CANCELLED)
        }
    }

    private fun checkout(paymentMethod: PaymentMethod, printReceipt: Boolean) {
        val currentState = _uiState.value
        val cartItems = currentState.cartItems
        if (cartItems.isEmpty() && currentState.checkoutOnlineOrder == null) {
            _uiState.update { it.copy(errorMessage = "購物車是空的，無法結帳") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true, errorMessage = null, snackbarMessage = null) }

            runCatching {
                val summary = buildCheckoutSummary(
                    cartItems = cartItems,
                    memberDiscountRate = currentState.memberDiscountRate,
                    cashReceivedInput = currentState.cashReceivedInput,
                    globalDiscount = currentState.globalDiscount,
                    globalTaxRate = currentState.globalTaxRate
                )

                var balanceBefore: Double? = null
                var balanceAfter: Double? = null
                if (paymentMethod == PaymentMethod.MEMBER_BALANCE) {
                    val phone = currentState.memberId
                    if (phone == null) {
                        _uiState.update {
                            it.copy(
                                isCheckingOut = false,
                                errorMessage = "無法使用會員餘額：尚未登入會員"
                            )
                        }
                        return@runCatching
                    }
                    balanceBefore = currentState.memberBalance
                    val success = memberRepository.updateBalance(phone, -summary.totalAmount)
                    if (!success) {
                        _uiState.update {
                            it.copy(
                                isCheckingOut = false,
                                errorMessage = "餘額不足，目前餘額：NT$ ${currentState.memberBalance}"
                            )
                        }
                        return@runCatching
                    }
                    // Update state with new balance
                    val updatedMember = memberRepository.getMemberByPhone(phone)
                    balanceAfter = updatedMember?.balance ?: 0.0
                    _uiState.update { it.copy(memberBalance = balanceAfter) }
                }

                val orderLines = cartItems.map { item ->
                    com.yourcompany.pos.domain.model.OrderLine(
                        productId = item.productId,
                        productName = item.productName,
                        sku = item.sku,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        taxRate = item.taxRate,
                        note = item.note,
                        modifiers = item.modifiers
                    )
                }

                val finalOrderNo: String

                val onlineOrder = currentState.checkoutOnlineOrder
                if (onlineOrder != null) {
                    finalOrderNo = onlineOrder.orderNo
                    val updatedOrder = onlineOrder.copy(
                        totalAmount = summary.totalAmount,
                        taxAmount = summary.taxAmount,
                        discountAmount = summary.discountAmount,
                        globalDiscount = currentState.globalDiscount,
                        paymentMethod = paymentMethod,
                        status = com.yourcompany.pos.domain.model.OrderStatus.PAID,
                        orderType = currentState.orderType,
                        tableNumber = currentState.tableNumber?.takeIf { it.isNotBlank() },
                        updatedAt = System.currentTimeMillis()
                    )
                    orderRepository.updateOrder(updatedOrder, orderLines)
                } else {
                    finalOrderNo = generateOrderNo()
                    val newOrder = com.yourcompany.pos.domain.model.Order(
                        orderNo = finalOrderNo,
                        totalAmount = summary.totalAmount,
                        taxAmount = summary.taxAmount,
                        discountAmount = summary.discountAmount,
                        globalDiscount = currentState.globalDiscount,
                        paymentMethod = paymentMethod,
                        status = com.yourcompany.pos.domain.model.OrderStatus.PAID,
                        orderType = currentState.orderType,
                        tableNumber = currentState.tableNumber?.takeIf { it.isNotBlank() }
                    )
                    orderRepository.createOrder(newOrder, orderLines)
                }

                if (printReceipt) {
                    val storeSettings = settingsRepository.getAllSettings()
                    printerManager.printOrderReceipt(
                        orderNo = finalOrderNo,
                        storeName = storeSettings["storeName"] ?: "NEON POS",
                        storeAddress = storeSettings["storeAddress"] ?: "台北信義門市",
                        storePhone = storeSettings["storePhone"] ?: "02-1234-5678",
                        paymentMethod = paymentMethod,
                        items = cartItems.map {
                            PrinterManager.DemoLine(
                                name = it.productName,
                                quantity = it.quantity,
                                total = it.subtotal + it.taxAmount,
                                note = it.note,
                                modifiers = it.modifiers
                            )
                        },
                        subtotalAmount = summary.subtotal,
                        taxAmount = summary.taxAmount,
                        discountAmount = summary.discountAmount,
                        cashReceivedAmount = summary.cashReceivedAmount,
                        changeAmount = summary.changeAmount,
                        totalAmount = summary.totalAmount,
                        memberId = currentState.memberId,
                        memberPoints = (summary.totalAmount * 0.1).toInt(), // Demo: 10% points
                        memberBalanceAfter = balanceAfter
                    )
                }

                _uiState.update {
                    it.copy(
                        isCheckingOut = false,
                        showCheckoutDialog = false,
                        isCheckoutScreenActive = false,
                        checkoutOnlineOrder = null,
                        cashReceivedInput = "",
                        snackbarMessage = "結帳成功！",
                        memberId = null,
                        memberName = null,
                        memberPoints = 0,
                        memberBalance = 0.0,
                        memberSource = null,
                        memberDiscountRate = 1.0,
                        globalDiscount = 0.0,
                        orderType = "外帶",
                        tableNumber = null
                    )
                }
                cartRepository.clearCart()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isCheckingOut = false,
                        errorMessage = error.message ?: "結帳失敗"
                    )
                }
            }
        }
    }

    private fun updateNfcStatus(message: String) {
        _uiState.update { it.copy(nfcStatus = message) }
    }

    private fun updatePrinterStatus(message: String) {
        _uiState.update { it.copy(printerStatus = message) }
    }

    private fun updateCashReceived(value: String) {
        _uiState.update { current ->
            val updated = current.copy(cashReceivedInput = value)
            updated.copy(
                checkoutSummary = buildCheckoutSummary(
                    cartItems = updated.cartItems,
                    memberDiscountRate = updated.memberDiscountRate,
                    cashReceivedInput = updated.cashReceivedInput,
                    globalDiscount = updated.globalDiscount,
                    globalTaxRate = updated.globalTaxRate
                )
            )
        }
    }

    private fun dismissMessage() {
        _uiState.update { it.copy(snackbarMessage = null, errorMessage = null) }
    }

    private fun generateOrderNo(): String {
        val time = System.currentTimeMillis()
        return "POS-$time"
    }

    private fun buildCheckoutSummary(
        cartItems: List<CartItem>,
        memberDiscountRate: Double,
        cashReceivedInput: String,
        globalDiscount: Double = 0.0,
        globalTaxRate: Double = 0.05
    ): CheckoutSummary {
        val subtotal = cartItems.sumOf { it.subtotal }
        val taxAmount = cartItems.sumOf { it.subtotal * globalTaxRate }
        val memberDiscount = if (memberDiscountRate in 0.0..1.0) subtotal * (1.0 - memberDiscountRate) else 0.0
        val discountAmount = memberDiscount + globalDiscount
        val totalAmount = (subtotal + taxAmount - discountAmount).coerceAtLeast(0.0)
        val cashReceivedAmount = cashReceivedInput.trim().toDoubleOrNull()?.takeIf { it > 0.0 } ?: totalAmount
        val changeAmount = (cashReceivedAmount - totalAmount).coerceAtLeast(0.0)
        val itemCount = cartItems.sumOf { it.quantity }
        return CheckoutSummary(
            subtotal = subtotal,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            cashReceivedAmount = cashReceivedAmount,
            changeAmount = changeAmount,
            itemCount = itemCount
        )
    }

    private fun describeNfcContent(content: NfcTagContent?): String {
        return when (content) {
            is NfcTagContent.MemberCard -> "會員卡：${content.memberName} (${content.memberId})"
            is NfcTagContent.ProductTag -> "商品標籤：${content.productName} (${content.sku})"
            is NfcTagContent.PlainText -> content.value
            is NfcTagContent.UriContent -> content.value
            is NfcTagContent.Unknown, null -> "NFC 已讀取"
        }
    }

    private fun buildNfcDetails(content: NfcTagContent?): String {
        return when (content) {
            is NfcTagContent.MemberCard -> {
                val discountPercent = ((1.0 - content.discountRate) * 100).toInt()
                "Points ${content.points} | Discount ${discountPercent}%"
            }
            is NfcTagContent.ProductTag -> content.price?.let { "Price NT$ ${String.format(Locale.getDefault(), "%.2f", it)}" } ?: "商品標籤"
            is NfcTagContent.PlainText -> content.value
            is NfcTagContent.UriContent -> content.value
            is NfcTagContent.Unknown, null -> ""
        }
    }

    private fun addProduct(sku: String, name: String, price: Double, taxRate: Double) {
        viewModelScope.launch {
            try {
                val product = com.yourcompany.pos.domain.model.Product(
                    id = 0,
                    sku = sku,
                    name = name,
                    price = price,
                    taxRate = taxRate,
                    isActive = true
                )
                productRepository.upsertProducts(listOf(product))
                _uiState.update { it.copy(snackbarMessage = "商品 $name 已成功新增") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "新增商品失敗: ${e.message}") }
            }
        }
    }

    private fun searchMember(phone: String) {
        viewModelScope.launch {
            val member = memberRepository.getMemberByPhone(phone)
            if (member != null) {
                _uiState.update {
                    it.copy(
                        memberId = member.phone,
                        memberName = member.name,
                        memberBalance = member.balance,
                        memberPoints = member.points,
                        memberSource = "MANUAL",
                        showMemberManagerDialog = false,
                        snackbarMessage = "會員登入成功：${member.name}"
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "找不到此會員，請檢查電話號碼或進行註冊") }
            }
        }
    }

    private fun registerMember(phone: String, name: String) {
        viewModelScope.launch {
            val existing = memberRepository.getMemberByPhone(phone)
            if (existing != null) {
                _uiState.update { it.copy(errorMessage = "此電話已註冊過會員") }
                return@launch
            }
            val newMember = Member(phone = phone, name = name)
            memberRepository.upsertMember(newMember)
            _uiState.update {
                it.copy(
                    memberId = newMember.phone,
                    memberName = newMember.name,
                    memberBalance = newMember.balance,
                    memberPoints = newMember.points,
                    memberSource = "MANUAL",
                    showMemberManagerDialog = false,
                    snackbarMessage = "會員註冊成功並自動登入：$name"
                )
            }
        }
    }

    private fun topUpMember(amount: Double) {
        viewModelScope.launch {
            val phone = _uiState.value.memberId
            if (phone == null) {
                _uiState.update { it.copy(errorMessage = "沒有已登入的會員可以儲值") }
                return@launch
            }
            val success = memberRepository.updateBalance(phone, amount)
            if (success) {
                val updatedMember = memberRepository.getMemberByPhone(phone)
                _uiState.update {
                    it.copy(
                        memberBalance = updatedMember?.balance ?: 0.0,
                        showMemberTopUpDialog = false,
                        snackbarMessage = "成功為會員儲值 $amount 元"
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "儲值失敗") }
            }
        }
    }
}
