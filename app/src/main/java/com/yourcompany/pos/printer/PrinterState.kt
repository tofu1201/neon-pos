package com.yourcompany.pos.printer

data class PrinterState(
    val connected: Boolean = false,
    val connecting: Boolean = false,
    val printing: Boolean = false,
    val lastEvent: PrinterEvent = PrinterEvent.Idle
)
