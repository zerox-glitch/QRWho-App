package com.example.qr.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import java.net.URLDecoder

data class QrParsedAction(
    val kind: PayloadKind,
    val title: String,
    val subtitle: String,
    val badgeLabel: String,
    val primaryButtonLabel: String,
    val primaryButtonIcon: ImageVector,
    val secondaryButtonLabel: String = "Copy",
    val secondaryButtonIcon: ImageVector = Icons.Default.ContentCopy,
    val isLocation: Boolean = false,
    val isUrl: Boolean = false,
    val copyableText: String,
    val geoLatitude: Double? = null,
    val geoLongitude: Double? = null,
    val geoQuery: String? = null
)

object QrContentParser {

    /**
     * Checks if a string represents a location QR code (geo: URI, Google Maps link, coordinates, etc.)
     */
    fun isLocationContent(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.startsWith("geo:", ignoreCase = true)) return true
        if (trimmed.contains("maps.google.", ignoreCase = true)) return true
        if (trimmed.contains("google.com/maps", ignoreCase = true)) return true
        if (trimmed.contains("maps.app.goo.gl", ignoreCase = true)) return true
        if (trimmed.contains("goo.gl/maps", ignoreCase = true)) return true
        if (trimmed.contains("maps.apple.com", ignoreCase = true)) return true
        if (trimmed.contains("openstreetmap.org", ignoreCase = true)) return true
        if (trimmed.contains("waze.com", ignoreCase = true)) return true

        // Coordinates pattern like "37.7749, -122.4194"
        val coordPattern = Regex("""^[+-]?\d{1,3}(?:\.\d+)?\s*,\s*[+-]?\d{1,3}(?:\.\d+)?$""")
        if (coordPattern.matches(trimmed)) return true

        return false
    }

    /**
     * Comprehensive parsing of scanned or saved QR text into structured action data.
     */
    fun parse(rawText: String): QrParsedAction {
        val trimmed = rawText.trim()

        // 1. Location / Google Maps / Geo URIs
        if (isLocationContent(trimmed)) {
            return parseLocation(trimmed)
        }

        // 2. Web URLs
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            val domain = try {
                val u = Uri.parse(trimmed)
                u.host?.removePrefix("www.") ?: trimmed.take(30)
            } catch (_: Exception) {
                trimmed.take(30)
            }
            return QrParsedAction(
                kind = PayloadKind.URL,
                title = "Website Link",
                subtitle = trimmed,
                badgeLabel = "WEBSITE",
                primaryButtonLabel = "Open Website",
                primaryButtonIcon = Icons.Default.OpenInBrowser,
                secondaryButtonLabel = "Copy URL",
                isUrl = true,
                copyableText = trimmed
            )
        }

        // 3. Wi-Fi QR
        if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
            val ssid = Regex("""S:([^;]+)""").find(trimmed)?.groupValues?.get(1) ?: "Wi-Fi Network"
            val pwd = Regex("""P:([^;]+)""").find(trimmed)?.groupValues?.get(1) ?: ""
            val enc = Regex("""T:([^;]+)""").find(trimmed)?.groupValues?.get(1) ?: "WPA"
            val sub = if (pwd.isNotEmpty()) "SSID: $ssid • Password: $pwd ($enc)" else "SSID: $ssid (Open Network)"
            return QrParsedAction(
                kind = PayloadKind.WIFI,
                title = "Wi-Fi: $ssid",
                subtitle = sub,
                badgeLabel = "WI-FI",
                primaryButtonLabel = if (pwd.isNotEmpty()) "Copy Wi-Fi Password" else "Copy SSID",
                primaryButtonIcon = Icons.Default.Wifi,
                secondaryButtonLabel = "Copy Full Details",
                copyableText = if (pwd.isNotEmpty()) pwd else ssid
            )
        }

        // 4. Contact / vCard
        if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true)) {
            val fn = Regex("""FN:([^\r\n]+)""").find(trimmed)?.groupValues?.get(1)
                ?: Regex("""N:([^\r\n;]+);?([^\r\n;]*)""").find(trimmed)?.let {
                    "${it.groupValues.getOrNull(2) ?: ""} ${it.groupValues.getOrNull(1) ?: ""}".trim()
                } ?: "Contact Card"
            val tel = Regex("""TEL[^:]*:([^\r\n]+)""").find(trimmed)?.groupValues?.get(1) ?: ""
            val email = Regex("""EMAIL[^:]*:([^\r\n]+)""").find(trimmed)?.groupValues?.get(1) ?: ""
            val org = Regex("""ORG:([^\r\n]+)""").find(trimmed)?.groupValues?.get(1) ?: ""

            val sub = listOf(tel, email, org).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "vCard 3.0" }
            return QrParsedAction(
                kind = PayloadKind.VCARD,
                title = fn,
                subtitle = sub,
                badgeLabel = "CONTACT",
                primaryButtonLabel = if (tel.isNotEmpty()) "Call $tel" else "Add Contact",
                primaryButtonIcon = if (tel.isNotEmpty()) Icons.Default.Phone else Icons.Default.ContactPage,
                secondaryButtonLabel = "Copy Contact",
                copyableText = trimmed
            )
        }

        // 5. Phone Call
        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val phone = trimmed.removePrefix("tel:").removePrefix("TEL:")
            return QrParsedAction(
                kind = PayloadKind.PHONE,
                title = "Phone Call",
                subtitle = phone,
                badgeLabel = "PHONE",
                primaryButtonLabel = "Call $phone",
                primaryButtonIcon = Icons.Default.Phone,
                secondaryButtonLabel = "Copy Number",
                copyableText = phone
            )
        }

        // 6. Email
        if (trimmed.startsWith("mailto:", ignoreCase = true)) {
            val email = trimmed.removePrefix("mailto:").removePrefix("MAILTO:").substringBefore("?")
            val sub = Regex("""subject=([^&]+)""", RegexOption.IGNORE_CASE).find(trimmed)?.groupValues?.get(1)?.let {
                try { URLDecoder.decode(it, "UTF-8") } catch (_: Exception) { it }
            }
            return QrParsedAction(
                kind = PayloadKind.EMAIL,
                title = "Send Email",
                subtitle = if (sub != null) "$email • Subject: $sub" else email,
                badgeLabel = "EMAIL",
                primaryButtonLabel = "Compose Email",
                primaryButtonIcon = Icons.Default.AlternateEmail,
                secondaryButtonLabel = "Copy Email",
                copyableText = email
            )
        }

        // 7. SMS
        if (trimmed.startsWith("smsto:", ignoreCase = true) || trimmed.startsWith("sms:", ignoreCase = true)) {
            val clean = trimmed.removePrefix("smsto:").removePrefix("SMSTO:").removePrefix("sms:").removePrefix("SMS:")
            val parts = clean.split(":", limit = 2)
            val num = parts.getOrNull(0) ?: ""
            val msg = parts.getOrNull(1) ?: ""
            return QrParsedAction(
                kind = PayloadKind.SMS,
                title = "Send SMS",
                subtitle = if (msg.isNotEmpty()) "To $num: \"$msg\"" else "To $num",
                badgeLabel = "SMS",
                primaryButtonLabel = "Send SMS",
                primaryButtonIcon = Icons.Default.Sms,
                secondaryButtonLabel = "Copy Text",
                copyableText = if (msg.isNotEmpty()) msg else num
            )
        }

        // 8. WhatsApp
        if (trimmed.contains("wa.me/", ignoreCase = true) || trimmed.startsWith("whatsapp://", ignoreCase = true)) {
            return QrParsedAction(
                kind = PayloadKind.WHATSAPP,
                title = "WhatsApp Chat",
                subtitle = trimmed,
                badgeLabel = "WHATSAPP",
                primaryButtonLabel = "Open WhatsApp",
                primaryButtonIcon = Icons.Default.Sms,
                secondaryButtonLabel = "Copy Link",
                isUrl = true,
                copyableText = trimmed
            )
        }

        // 9. Payment / Crypto
        if (trimmed.startsWith("upi://", ignoreCase = true) || trimmed.startsWith("bitcoin:", ignoreCase = true) ||
            trimmed.startsWith("ethereum:", ignoreCase = true) || trimmed.startsWith("solana:", ignoreCase = true)) {
            val scheme = trimmed.substringBefore(":").uppercase()
            return QrParsedAction(
                kind = PayloadKind.PAYMENT,
                title = "$scheme Payment",
                subtitle = trimmed,
                badgeLabel = "PAYMENT",
                primaryButtonLabel = "Open Payment App",
                primaryButtonIcon = Icons.Default.Payment,
                secondaryButtonLabel = "Copy Address",
                copyableText = trimmed
            )
        }

        // 10. Plain text fallback
        return QrParsedAction(
            kind = PayloadKind.TEXT,
            title = "Text Note",
            subtitle = trimmed,
            badgeLabel = "TEXT",
            primaryButtonLabel = "Copy Text",
            primaryButtonIcon = Icons.Default.ContentCopy,
            secondaryButtonLabel = "Copy",
            copyableText = trimmed
        )
    }

    private fun parseLocation(raw: String): QrParsedAction {
        var lat: Double? = null
        var lng: Double? = null
        var query: String? = null
        var locationTitle = "Google Maps Location"
        var locationSubtitle = raw

        try {
            if (raw.startsWith("geo:", ignoreCase = true)) {
                val content = raw.removePrefix("geo:").removePrefix("GEO:")
                val parts = content.split("?", limit = 2)
                val coordsPart = parts.getOrNull(0) ?: ""
                val queryParam = parts.getOrNull(1)

                // Extract lat/lng from "37.7749,-122.4194"
                if (coordsPart.contains(",")) {
                    val latLng = coordsPart.split(",")
                    lat = latLng.getOrNull(0)?.trim()?.toDoubleOrNull()
                    lng = latLng.getOrNull(1)?.trim()?.toDoubleOrNull()
                }

                if (!queryParam.isNullOrBlank()) {
                    val qMatch = Regex("""q=([^&]+)""").find(queryParam)
                    if (qMatch != null) {
                        val rawQ = qMatch.groupValues[1]
                        query = try { URLDecoder.decode(rawQ, "UTF-8") } catch (_: Exception) { rawQ }
                        // If coords were 0,0 check if q has coords
                        if ((lat == null || (lat == 0.0 && lng == 0.0)) && query.contains(",")) {
                            val qCoords = query.split(",")
                            val qLat = qCoords.getOrNull(0)?.trim()?.toDoubleOrNull()
                            val qLng = qCoords.getOrNull(1)?.trim()?.toDoubleOrNull()
                            if (qLat != null && qLng != null) {
                                lat = qLat
                                lng = qLng
                            }
                        }
                    }
                }

                locationTitle = if (!query.isNullOrBlank() && !query.contains(Regex("""^[+-]?\d"""))) {
                    query
                } else if (lat != null && lng != null) {
                    "Coordinates: ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}"
                } else {
                    "Map Location"
                }

                locationSubtitle = if (lat != null && lng != null) {
                    if (!query.isNullOrBlank() && query != "$lat,$lng") {
                        "$query (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})"
                    } else {
                        "Lat: ${String.format("%.5f", lat)}, Lng: ${String.format("%.5f", lng)}"
                    }
                } else {
                    query ?: raw
                }
            } else if (raw.contains("google.com/maps") || raw.contains("maps.google.") || raw.contains("goo.gl/maps")) {
                locationTitle = "Google Maps Location"
                // Try to extract coordinates from @lat,lng or q=lat,lng
                val atMatch = Regex("""@([+-]?\d+\.\d+),([+-]?\d+\.\d+)""").find(raw)
                val qMatch = Regex("""[?&]q=([^&]+)""").find(raw)
                val queryVal = qMatch?.groupValues?.get(1)?.let {
                    try { URLDecoder.decode(it, "UTF-8") } catch (_: Exception) { it }
                }

                if (atMatch != null) {
                    lat = atMatch.groupValues[1].toDoubleOrNull()
                    lng = atMatch.groupValues[2].toDoubleOrNull()
                } else if (queryVal != null && queryVal.contains(",")) {
                    val qp = queryVal.split(",")
                    lat = qp.getOrNull(0)?.trim()?.toDoubleOrNull()
                    lng = qp.getOrNull(1)?.trim()?.toDoubleOrNull()
                }

                query = queryVal
                if (!queryVal.isNullOrBlank()) {
                    locationTitle = queryVal
                }
                locationSubtitle = raw
            } else {
                // Raw coords
                val match = Regex("""^([+-]?\d+(?:\.\d+)?)\s*,\s*([+-]?\d+(?:\.\d+)?)$""").find(raw)
                if (match != null) {
                    lat = match.groupValues[1].toDoubleOrNull()
                    lng = match.groupValues[2].toDoubleOrNull()
                    locationTitle = "Coordinates: $lat, $lng"
                    locationSubtitle = "Latitude: $lat, Longitude: $lng"
                }
            }
        } catch (_: Exception) {}

        return QrParsedAction(
            kind = PayloadKind.GEO,
            title = locationTitle,
            subtitle = locationSubtitle,
            badgeLabel = "LOCATION",
            primaryButtonLabel = "Open in Google Maps",
            primaryButtonIcon = Icons.Default.Navigation,
            secondaryButtonLabel = "Copy Location",
            isLocation = true,
            isUrl = raw.startsWith("http://") || raw.startsWith("https://"),
            copyableText = if (lat != null && lng != null) "$lat, $lng" else raw,
            geoLatitude = lat,
            geoLongitude = lng,
            geoQuery = query
        )
    }

    /**
     * Executes the primary action for any QR code with robust fallbacks.
     */
    fun openPrimaryAction(context: Context, raw: String, parsed: QrParsedAction, onToast: (String) -> Unit = {}) {
        try {
            when {
                parsed.isLocation -> {
                    openLocationInMaps(context, raw, parsed, onToast)
                }
                parsed.kind == PayloadKind.URL || (parsed.isUrl && (raw.startsWith("http://") || raw.startsWith("https://"))) -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(raw)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                parsed.kind == PayloadKind.PHONE -> {
                    val phone = raw.removePrefix("tel:").removePrefix("TEL:")
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                }
                parsed.kind == PayloadKind.EMAIL -> {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(raw)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                parsed.kind == PayloadKind.SMS -> {
                    val num = parsed.copyableText.substringBefore(":")
                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$num")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(smsIntent)
                }
                parsed.kind == PayloadKind.WHATSAPP -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(raw)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                parsed.kind == PayloadKind.VCARD -> {
                    val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val fn = Regex("""FN:([^\r\n]+)""").find(raw)?.groupValues?.get(1)
                        if (fn != null) putExtra(ContactsContract.Intents.Insert.NAME, fn)
                        val tel = Regex("""TEL[^:]*:([^\r\n]+)""").find(raw)?.groupValues?.get(1)
                        if (tel != null) putExtra(ContactsContract.Intents.Insert.PHONE, tel)
                        val email = Regex("""EMAIL[^:]*:([^\r\n]+)""").find(raw)?.groupValues?.get(1)
                        if (email != null) putExtra(ContactsContract.Intents.Insert.EMAIL, email)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        onToast("Contact details copied to clipboard")
                    }
                }
                parsed.kind == PayloadKind.PAYMENT -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(raw)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                else -> {
                    onToast("Content copied to clipboard")
                }
            }
        } catch (e: Exception) {
            // Fallback to web browser or toast
            if (raw.startsWith("http://") || raw.startsWith("https://")) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(raw)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    return
                } catch (_: Exception) {}
            }
            onToast("Copied to clipboard: ${e.localizedMessage ?: "Action completed"}")
        }
    }

    /**
     * Opens location in Google Maps app or browser navigation.
     */
    fun openLocationInMaps(context: Context, raw: String, parsed: QrParsedAction, onToast: (String) -> Unit = {}) {
        try {
            // 1. If it's already a full http/https Google Maps / map link:
            if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
                // Try Google Maps native app first
                val mapAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(raw)).apply {
                    setPackage("com.google.android.apps.maps")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(mapAppIntent)
                    return
                } catch (_: Exception) {
                    // Fallback to standard browser intent
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(raw)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    return
                }
            }

            // 2. Build explicit Google Maps URL from coordinates or query
            val lat = parsed.geoLatitude
            val lng = parsed.geoLongitude
            val query = parsed.geoQuery

            val gmapsWebUrl = when {
                lat != null && lng != null && !query.isNullOrBlank() && query != "$lat,$lng" ->
                    "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"
                lat != null && lng != null ->
                    "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
                !query.isNullOrBlank() ->
                    "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"
                raw.startsWith("geo:", ignoreCase = true) ->
                    "https://www.google.com/maps/search/?api=1&query=${Uri.encode(raw.removePrefix("geo:").removePrefix("GEO:"))}"
                else ->
                    "https://www.google.com/maps/search/?api=1&query=${Uri.encode(raw)}"
            }

            // 3. Try native geo URI first
            val geoUriString = when {
                lat != null && lng != null && !query.isNullOrBlank() ->
                    "geo:$lat,$lng?q=${Uri.encode(query)}"
                lat != null && lng != null ->
                    "geo:$lat,$lng?q=$lat,$lng"
                raw.startsWith("geo:", ignoreCase = true) -> raw
                else -> "geo:0,0?q=${Uri.encode(raw)}"
            }

            val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUriString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Try Google Maps package
            try {
                val googleMapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUriString)).apply {
                    setPackage("com.google.android.apps.maps")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(googleMapsIntent)
                return
            } catch (_: Exception) {
                // Try any map handler
                try {
                    context.startActivity(geoIntent)
                    return
                } catch (_: Exception) {
                    // Fallback to Google Maps Web link in browser
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(gmapsWebUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                }
            }
        } catch (e: Exception) {
            onToast("Could not open location: ${e.localizedMessage}")
        }
    }
}
