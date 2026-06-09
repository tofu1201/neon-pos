package com.yourcompany.pos.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import org.json.JSONObject
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object NfcTagParser {
    fun parse(tag: Tag): NfcTagContent? {
        val rawText = readNdefText(tag) ?: return null
        return parseRaw(rawText)
    }

    fun parseRaw(raw: String): NfcTagContent {
        val normalized = raw.trim()
        if (normalized.isBlank()) return NfcTagContent.Unknown(raw)

        parseJson(normalized)?.let { return it }
        parseDelimited(normalized)?.let { return it }

        return when {
            normalized.startsWith("http://", ignoreCase = true) || normalized.startsWith("https://", ignoreCase = true) -> {
                NfcTagContent.UriContent(normalized)
            }
            normalized.startsWith("pos://", ignoreCase = true) -> {
                parsePosUri(normalized) ?: NfcTagContent.PlainText(normalized)
            }
            else -> NfcTagContent.PlainText(normalized)
        }
    }

    private fun parseJson(raw: String): NfcTagContent? {
        return runCatching {
            val json = JSONObject(raw)
            when (json.optString("type").lowercase()) {
                "member", "member_card", "membership" -> NfcTagContent.MemberCard(
                    memberId = json.optString("memberId", json.optString("id", "UNKNOWN")),
                    memberName = json.optString("name", json.optString("memberName", "會員")),
                    points = json.optInt("points", 0),
                    discountRate = json.optDouble("discountRate", 1.0),
                    rawPayload = raw
                )
                "product", "sku", "product_tag" -> NfcTagContent.ProductTag(
                    sku = json.optString("sku", json.optString("productSku", "UNKNOWN")),
                    productName = json.optString("name", json.optString("productName", "商品")),
                    price = if (json.has("price")) json.optDouble("price") else null,
                    category = json.optString("category", null),
                    rawPayload = raw
                )
                else -> NfcTagContent.PlainText(raw)
            }
        }.getOrNull()
    }

    private fun parseDelimited(raw: String): NfcTagContent? {
        val parts = raw.split('|').map { it.trim() }
        if (parts.isEmpty()) return null

        return when (parts.first().lowercase()) {
            "member" -> NfcTagContent.MemberCard(
                memberId = parts.getOrElse(1) { "UNKNOWN" },
                memberName = parts.getOrElse(2) { "會員" },
                points = parts.getOrNull(3)?.toIntOrNull() ?: 0,
                discountRate = parts.getOrNull(4)?.toDoubleOrNull() ?: 1.0,
                rawPayload = raw
            )
            "product" -> NfcTagContent.ProductTag(
                sku = parts.getOrElse(1) { "UNKNOWN" },
                productName = parts.getOrElse(2) { "商品" },
                price = parts.getOrNull(3)?.toDoubleOrNull(),
                category = parts.getOrNull(4),
                rawPayload = raw
            )
            "uri" -> NfcTagContent.UriContent(parts.drop(1).joinToString("|"))
            "text" -> NfcTagContent.PlainText(parts.drop(1).joinToString("|"))
            else -> parsePosUri(raw)
        }
    }

    private fun parsePosUri(raw: String): NfcTagContent? {
        val cleaned = raw.removePrefix("pos://")
        val parts = cleaned.split('/').filter { it.isNotBlank() }
        if (parts.isEmpty()) return null

        return when (parts.first().lowercase()) {
            "member" -> NfcTagContent.MemberCard(
                memberId = parts.getOrElse(1) { "UNKNOWN" },
                memberName = parts.getOrElse(2) { "會員" },
                points = parts.getOrNull(3)?.toIntOrNull() ?: 0,
                discountRate = parts.getOrNull(4)?.toDoubleOrNull() ?: 1.0,
                rawPayload = raw
            )
            "product" -> NfcTagContent.ProductTag(
                sku = parts.getOrElse(1) { "UNKNOWN" },
                productName = parts.getOrElse(2) { "商品" },
                price = parts.getOrNull(3)?.toDoubleOrNull(),
                category = parts.getOrNull(4),
                rawPayload = raw
            )
            else -> NfcTagContent.Unknown(raw)
        }
    }

    private fun readNdefText(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return runCatching {
            ndef.connect()
            val message: NdefMessage = ndef.cachedNdefMessage ?: ndef.ndefMessage
            message.records.firstNotNullOfOrNull { decodeRecord(it) }
        }.getOrNull().also {
            runCatching { ndef.close() }
        }
    }

    private fun decodeRecord(record: NdefRecord): String? {
        return when {
            record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_TEXT) -> {
                decodeTextRecord(record.payload)
            }
            record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_URI) -> {
                decodeUriRecord(record.payload)
            }
            record.tnf == NdefRecord.TNF_MIME_MEDIA -> {
                String(record.payload, StandardCharsets.UTF_8)
            }
            else -> runCatching { String(record.payload, Charset.forName("UTF-8")) }.getOrNull()
        }
    }

    private fun decodeTextRecord(payload: ByteArray): String {
        if (payload.isEmpty()) return ""
        val status = payload[0].toInt()
        val languageCodeLength = status and 0x3F
        val textEncoding = if ((status and 0x80) == 0) StandardCharsets.UTF_8 else StandardCharsets.UTF_16
        return String(payload, 1 + languageCodeLength, payload.size - 1 - languageCodeLength, textEncoding)
    }

    private fun decodeUriRecord(payload: ByteArray): String {
        if (payload.isEmpty()) return ""
        val prefix = when (payload[0].toInt()) {
            0x01 -> "http://www."
            0x02 -> "https://www."
            0x03 -> "http://"
            0x04 -> "https://"
            else -> ""
        }
        val suffix = String(payload, 1, payload.size - 1, StandardCharsets.UTF_8)
        return prefix + suffix
    }
}
