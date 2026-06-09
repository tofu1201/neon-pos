package com.yourcompany.pos.nfc

data class NfcState(
    val available: Boolean = false,
    val enabled: Boolean = false,
    val reading: Boolean = false,
    val tagContent: NfcTagContent? = null,
    val lastEvent: NfcEvent = NfcEvent.Idle
)
