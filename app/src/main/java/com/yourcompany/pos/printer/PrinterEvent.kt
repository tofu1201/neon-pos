package com.yourcompany.pos.printer

sealed interface PrinterEvent {
    data object Idle : PrinterEvent
    data object Connecting : PrinterEvent
    data object Connected : PrinterEvent
    data object Disconnected : PrinterEvent
    data object Reconnecting : PrinterEvent
    data object PaperOut : PrinterEvent
    data object Ready : PrinterEvent
    data class PrintSuccess(val message: String = "列印完成") : PrinterEvent
    data class PrintFailed(val reason: String) : PrinterEvent
    data class Error(val reason: String) : PrinterEvent
}
