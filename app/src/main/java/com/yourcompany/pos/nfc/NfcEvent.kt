package com.yourcompany.pos.nfc

sealed interface NfcEvent {
    data object Idle : NfcEvent
    data object Enabled : NfcEvent
    data object Disabled : NfcEvent
    data class TagDiscovered(val tagId: String, val rawTechList: List<String>) : NfcEvent
    data class ParsedContentDetected(val content: NfcTagContent) : NfcEvent
    data class LoginCardRead(val cardId: String) : NfcEvent
    data class ProductTagRead(val productTagId: String) : NfcEvent
    data class Error(val message: String) : NfcEvent
}
