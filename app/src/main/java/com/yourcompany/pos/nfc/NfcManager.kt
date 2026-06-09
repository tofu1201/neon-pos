package com.yourcompany.pos.nfc

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class NfcManager(
    private val activity: Activity,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    private val _state = MutableStateFlow(
        NfcState(
            available = adapter != null,
            enabled = adapter?.isEnabled == true,
            reading = false,
            lastEvent = if (adapter?.isEnabled == true) NfcEvent.Enabled else NfcEvent.Disabled
        )
    )
    val state: StateFlow<NfcState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NfcEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<NfcEvent> = _events.asSharedFlow()

    private val readerCallback = NfcAdapter.ReaderCallback { tag ->
        handleTag(tag)
    }

    fun startReaderMode() {
        val nfcAdapter = adapter ?: run {
            emit(NfcEvent.Error("裝置不支援 NFC"))
            return
        }
        if (!nfcAdapter.isEnabled) {
            _state.value = _state.value.copy(enabled = false, reading = false, lastEvent = NfcEvent.Disabled)
            emit(NfcEvent.Disabled)
            return
        }

        val flags =
            NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NFC_BARCODE or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        val extras = Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        }

        nfcAdapter.enableReaderMode(activity, readerCallback, flags, extras)
        _state.value = _state.value.copy(
            enabled = true,
            reading = true,
            lastEvent = NfcEvent.Enabled
        )
        emit(NfcEvent.Enabled)
    }

    fun stopReaderMode() {
        adapter?.disableReaderMode(activity)
        _state.value = _state.value.copy(reading = false, lastEvent = NfcEvent.Disabled)
        emit(NfcEvent.Disabled)
    }

    fun onNewIntent(intent: Intent) {
        if (
            intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            val tag = intent.getParcelableExtraCompat<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                handleTag(tag)
            }
        }
    }

    fun isAvailable(): Boolean = adapter != null
    fun isEnabled(): Boolean = adapter?.isEnabled == true

    private fun handleTag(tag: Tag) {
        val tagId = tag.id.toHexString()
        val techList = tag.techList.toList()
        val parsedContent = runCatching { NfcTagParser.parse(tag) }.getOrNull()

        val discovered = NfcEvent.TagDiscovered(tagId = tagId, rawTechList = techList)
        _state.value = _state.value.copy(lastEvent = discovered, tagContent = parsedContent)
        emit(discovered)

        if (parsedContent != null) {
            val parsedEvent = NfcEvent.ParsedContentDetected(parsedContent)
            _state.value = _state.value.copy(lastEvent = parsedEvent, tagContent = parsedContent)
            emit(parsedEvent)
        }

        val classifiedEvent = when (val content = parsedContent) {
            is NfcTagContent.MemberCard -> NfcEvent.LoginCardRead(cardId = content.memberId)
            is NfcTagContent.ProductTag -> NfcEvent.ProductTagRead(productTagId = content.sku)
            null -> when {
                techList.contains(MifareClassic::class.java.name) || techList.contains(NfcA::class.java.name) -> {
                    NfcEvent.LoginCardRead(cardId = tagId)
                }
                techList.contains(Ndef::class.java.name) -> {
                    NfcEvent.ProductTagRead(productTagId = tagId)
                }
                else -> discovered
            }
            else -> discovered
        }

        _state.value = _state.value.copy(lastEvent = classifiedEvent)
        emit(classifiedEvent)
    }

    private fun emit(event: NfcEvent) {
        _events.tryEmit(event)
    }
}

private inline fun <reified T> Intent.getParcelableExtraCompat(name: String): T? {
    return if (android.os.Build.VERSION.SDK_INT >= 33) {
        getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(name)
    }
}

private fun ByteArray.toHexString(): String =
    joinToString(separator = "") { byte -> "%02X".format(byte) }
