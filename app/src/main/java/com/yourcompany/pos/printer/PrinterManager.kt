package com.yourcompany.pos.printer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.yourcompany.pos.domain.model.PaymentMethod
import net.nyx.printerservice.print.IPrinterService
import net.nyx.printerservice.print.PrintTextFormat
import java.util.Locale

class PrinterManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val printerPackage = "com.incar.printerservice"
    private val printerAction = "com.incar.printerservice.IPrinterService"

    private var printerService: IPrinterService? = null
    private var reconnectJob: Job? = null

    private val _state = MutableStateFlow(PrinterState())
    val state: StateFlow<PrinterState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PrinterEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<PrinterEvent> = _events.asSharedFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            printerService = IPrinterService.Stub.asInterface(service)
            _state.value = _state.value.copy(
                connected = true,
                connecting = false,
                lastEvent = PrinterEvent.Connected
            )
            emit(PrinterEvent.Connected)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            printerService = null
            _state.value = _state.value.copy(
                connected = false,
                connecting = false,
                lastEvent = PrinterEvent.Disconnected
            )
            emit(PrinterEvent.Disconnected)
            scheduleReconnect()
        }
    }

    fun connect() {
        if (_state.value.connected || _state.value.connecting) return

        _state.value = _state.value.copy(connecting = true, lastEvent = PrinterEvent.Connecting)
        emit(PrinterEvent.Connecting)

        val intent = Intent().apply {
            setPackage(printerPackage)
            action = printerAction
        }

        val ok = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        if (!ok) {
            _state.value = _state.value.copy(
                connecting = false,
                connected = false,
                lastEvent = PrinterEvent.Error("無法綁定印表機服務")
            )
            emit(PrinterEvent.Error("無法綁定印表機服務"))
            scheduleReconnect()
        }
    }

    fun disconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
        runCatching { context.unbindService(serviceConnection) }
        printerService = null
        _state.value = _state.value.copy(
            connected = false,
            connecting = false,
            printing = false,
            lastEvent = PrinterEvent.Disconnected
        )
        emit(PrinterEvent.Disconnected)
    }

    fun printOrderReceipt(
        orderNo: String,
        storeName: String = "NEON POS",
        storeAddress: String = "台北信義門市",
        storePhone: String = "02-1234-5678",
        paymentMethod: PaymentMethod,
        memberId: String? = null,
        memberName: String? = null,
        memberPoints: Int? = null,
        memberDiscountRate: Double = 1.0,
        items: List<DemoLine>,
        subtotalAmount: Double,
        taxAmount: Double,
        discountAmount: Double,
        cashReceivedAmount: Double,
        changeAmount: Double,
        totalAmount: Double,
        memberBalanceBefore: Double? = null,
        memberBalanceAfter: Double? = null
    ) {
        scope.launch {
            executePrint {
                val service = printerService ?: throw IllegalStateException("印表機未連線")
                val center = textFormat(1)
                val left = textFormat(0)

                service.printText("\n", left)
                service.printText("$storeName\n", center)
                service.printText("$storeAddress\n", center)
                service.printText("TEL: $storePhone\n", center)
                service.printText("================================\n", left)
                
                val paymentMethodName = when (paymentMethod) {
                    PaymentMethod.CASH -> "現金"
                    PaymentMethod.CARD -> "信用卡"
                    PaymentMethod.NFC -> "感應支付"
                    PaymentMethod.MIXED -> "複合支付"
                    PaymentMethod.MEMBER_BALANCE -> "會員餘額"
                }

                service.printText("單號: $orderNo\n", left)
                service.printText("時間: ${currentTimestamp()}\n", left)
                service.printText("付款: $paymentMethodName\n", left)
                
                if (!memberId.isNullOrBlank() || !memberName.isNullOrBlank()) {
                    service.printText("會員: ${memberName ?: "未知"} (${memberId ?: "無"})\n", left)
                    val pts = memberPoints?.let { "點數:$it " } ?: ""
                    val disc = if (memberDiscountRate < 1.0) String.format(Locale.getDefault(), "折扣:%.0f%%", (1.0 - memberDiscountRate) * 100) else ""
                    if (pts.isNotEmpty() || disc.isNotEmpty()) {
                        service.printText("優惠: $pts$disc\n", left)
                    }
                    if (paymentMethod == PaymentMethod.MEMBER_BALANCE && memberBalanceBefore != null && memberBalanceAfter != null) {
                        service.printText("餘額: $${memberBalanceBefore} -> $${memberBalanceAfter}\n", left)
                    }
                }
                service.printText("--------------------------------\n", left)
                items.forEach { item ->
                    service.printText("${item.name}\n", left)
                    
                    val qtyStr = "  x${item.quantity}"
                    val totalStr = "$${item.total}"
                    // Sunmi printer 58mm paper supports ~32 english chars.
                    // We pad manually to ensure it looks right.
                    service.printText(String.format(Locale.getDefault(), "%-16s %15s\n", qtyStr, totalStr), left)
                    
                    if (item.modifiers.isNotEmpty()) {
                        service.printText("  - ${item.modifiers.joinToString(", ")}\n", left)
                    }
                    if (!item.note.isNullOrBlank()) {
                        service.printText("  * ${item.note}\n", left)
                    }
                }
                service.printText("--------------------------------\n", left)
                
                service.printText(String.format(Locale.getDefault(), "小計: %26.2f\n", subtotalAmount), left)
                if (discountAmount > 0) {
                    service.printText(String.format(Locale.getDefault(), "折扣: %26.2f\n", -discountAmount), left)
                }
                service.printText(String.format(Locale.getDefault(), "總計: %26.2f\n", totalAmount), left)
                
                if (paymentMethod == PaymentMethod.CASH) {
                    service.printText(String.format(Locale.getDefault(), "實收: %26.2f\n", cashReceivedAmount), left)
                    service.printText(String.format(Locale.getDefault(), "找零: %26.2f\n", changeAmount), left)
                }
                
                service.printText("================================\n", left)
                service.printText("*** 謝謝光臨 ***\n", center)
                service.printText("\n", left)
                service.printEndAutoOut()
            }
        }
    }

    fun printZReport(
        storeName: String,
        storeAddress: String,
        storePhone: String,
        totalRevenue: Double,
        cashTotal: Double,
        cardTotal: Double,
        nfcTotal: Double,
        memberBalanceTotal: Double,
        discountTotal: Double,
        orderCount: Int
    ) {
        scope.launch {
            executePrint {
                val service = printerService ?: throw IllegalStateException("印表機未連線")
                val center = textFormat(1)
                val left = textFormat(0)

                service.printText("\n", left)
                service.printText("$storeName\n", center)
                service.printText("日結報表 (Z-Report)\n", center)
                service.printText("================================\n", left)
                service.printText("列印時間: ${currentTimestamp()}\n", left)
                service.printText("--------------------------------\n", left)
                
                service.printText(String.format(java.util.Locale.getDefault(), "總單數:   %22d\n", orderCount), left)
                service.printText(String.format(java.util.Locale.getDefault(), "折扣總額: %22.2f\n", discountTotal), left)
                service.printText("--------------------------------\n", left)
                service.printText(String.format(java.util.Locale.getDefault(), "現金總額: %22.2f\n", cashTotal), left)
                service.printText(String.format(java.util.Locale.getDefault(), "信用卡:   %22.2f\n", cardTotal), left)
                service.printText(String.format(java.util.Locale.getDefault(), "感應支付: %22.2f\n", nfcTotal), left)
                service.printText(String.format(java.util.Locale.getDefault(), "會員扣款: %22.2f\n", memberBalanceTotal), left)
                service.printText("================================\n", left)
                service.printText(String.format(java.util.Locale.getDefault(), "今日總營收: %20.2f\n", totalRevenue), left)
                service.printText("================================\n", left)
                
                service.printText("*** 報表結束 ***\n", center)
                service.printText("\n", left)
                service.printEndAutoOut()
            }
        }
    }

    fun printDemoReceipt(orderNo: String, items: List<DemoLine>, totalAmount: Double) {
        val subtotalAmount = items.sumOf { it.total }
        printOrderReceipt(
            orderNo = orderNo,
            paymentMethod = PaymentMethod.CASH,
            memberId = null,
            memberName = null,
            memberPoints = null,
            memberDiscountRate = 1.0,
            items = items,
            subtotalAmount = subtotalAmount,
            taxAmount = 0.0,
            discountAmount = 0.0,
            cashReceivedAmount = totalAmount,
            changeAmount = 0.0,
            totalAmount = totalAmount
        )
    }

    fun printBarcode(value: String, width: Int = 300, height: Int = 160) {
        scope.launch {
            executePrint {
                val service = printerService ?: throw IllegalStateException("印表機未連線")
                val ret = service.printBarcode(value, width, height, 1, 1, 0)
                if (ret != 0) throw IllegalStateException("Barcode 列印失敗，code=$ret")
                service.printEndAutoOut()
            }
        }
    }

    fun printQrCode(value: String, size: Int = 300) {
        scope.launch {
            executePrint {
                val service = printerService ?: throw IllegalStateException("印表機未連線")
                val ret = service.printQrCode(value, size, size, 1)
                if (ret != 0) throw IllegalStateException("QR Code 列印失敗，code=$ret")
                service.printEndAutoOut()
            }
        }
    }

    fun setPaperWidth(widthMm: Int) {
        scope.launch {
            executePrint {
                val service = printerService ?: throw IllegalStateException("印表機未連線")
                val ret = service.setPaperWidth(widthMm)
                if (ret != 0) throw IllegalStateException("設定紙寬失敗，code=$ret")
            }
        }
    }

    fun setPrinterDensity(density: Int) {
        scope.launch {
            executePrint {
                val service = printerService ?: throw IllegalStateException("印表機未連線")
                val ret = service.setPrinterDensity(density)
                if (ret != 0) throw IllegalStateException("設定濃度失敗，code=$ret")
            }
        }
    }

    fun isConnected(): Boolean = printerService != null && _state.value.connected

    fun printZReport(
        date: String,
        totalOrders: Int,
        totalSales: Double,
        cashSales: Double,
        cardSales: Double,
        memberSales: Double,
        heldOrders: Int
    ) {
        scope.launch {
            executePrint {
                val service = printerService ?: throw IllegalStateException("印表機未連線")
                val center = textFormat(1)
                val left = textFormat(0)

                service.printText("\n", left)
                service.printText("=== 交班日結報表 (Z-Report) ===\n", center)
                service.printText("Date: $date\n", left)
                service.printText("Time: ${currentTimestamp()}\n", left)
                service.printText("--------------------------------\n", left)
                service.printText(String.format(Locale.getDefault(), "Total Orders : %d\n", totalOrders), left)
                service.printText(String.format(Locale.getDefault(), "Total Sales  : NT$ %.2f\n", totalSales), left)
                service.printText("--------------------------------\n", left)
                service.printText("Payments Breakdown:\n", left)
                service.printText(String.format(Locale.getDefault(), "  CASH       : NT$ %.2f\n", cashSales), left)
                service.printText(String.format(Locale.getDefault(), "  CARD       : NT$ %.2f\n", cardSales), left)
                service.printText(String.format(Locale.getDefault(), "  MEMBER     : NT$ %.2f\n", memberSales), left)
                service.printText("--------------------------------\n", left)
                if (heldOrders > 0) {
                    service.printText(String.format(Locale.getDefault(), "Pending/Held Orders: %d\n", heldOrders), left)
                }
                service.printText("================================\n", left)
                service.printText("\n\n\n\n", left)
                service.paperOut(80)
            }
        }
    }

    private suspend fun executePrint(block: suspend () -> Unit) {
        _state.value = _state.value.copy(printing = true, lastEvent = PrinterEvent.Ready)
        emit(PrinterEvent.Ready)
        try {
            block()
            _state.value = _state.value.copy(printing = false, lastEvent = PrinterEvent.PrintSuccess())
            emit(PrinterEvent.PrintSuccess())
        } catch (e: RemoteException) {
            handlePrintError(e.message ?: "Printer RemoteException")
        } catch (e: IllegalStateException) {
            handlePrintError(e.message ?: "非法狀態")
        } catch (e: Exception) {
            handlePrintError(e.message ?: "未知列印錯誤")
        } finally {
            _state.value = _state.value.copy(printing = false)
        }
    }

    private fun handlePrintError(reason: String) {
        val normalized = when {
            reason.contains("paper", ignoreCase = true) -> PrinterEvent.PaperOut
            reason.contains("disconnect", ignoreCase = true) -> PrinterEvent.Disconnected
            else -> PrinterEvent.PrintFailed(reason)
        }

        _state.value = _state.value.copy(
            connected = printerService != null,
            printing = false,
            lastEvent = normalized
        )
        emit(normalized)
        if (normalized is PrinterEvent.Disconnected) {
            scheduleReconnect()
        }
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            _state.value = _state.value.copy(lastEvent = PrinterEvent.Reconnecting)
            emit(PrinterEvent.Reconnecting)
            delay(5_000)
            connect()
        }
    }

    private fun emit(event: PrinterEvent) {
        _events.tryEmit(event)
    }

    private fun textFormat(align: Int): PrintTextFormat = PrintTextFormat().apply {
        setAli(align)
        setTextSize(26)
        setStyle(1) // 1 is usually BOLD in Sunmi/NYX thermal printer SDKs
    }

    private fun currentTimestamp(): String = java.text.SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.getDefault()
    ).format(java.util.Date())

    data class DemoLine(
        val name: String, 
        val quantity: Int, 
        val total: Double, 
        val note: String? = null, 
        val modifiers: List<String> = emptyList()
    )
}
