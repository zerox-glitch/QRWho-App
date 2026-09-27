package com.example.qr.engine

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.net.URLEncoder

enum class PayloadKind(val displayName: String) {
    URL("Website URL"),
    WIFI("Wi-Fi Network"),
    VCARD("Contact Card"),
    EVENT("Calendar Event"),
    EMAIL("Email"),
    PHONE("Phone Call"),
    SMS("SMS Message"),
    WHATSAPP("WhatsApp"),
    PAYMENT("Payment / Crypto"),
    GEO("Location / Map"),
    TEXT("Plain Text")
}

data class QrPayload(
    val kind: PayloadKind = PayloadKind.URL,
    val url: String = "https://qrwho.vercel.app",
    // Wi-Fi
    val wifiSsid: String = "Studio_WiFi",
    val wifiPassword: String = "qrwho2026",
    val wifiEncryption: String = "WPA", // WPA, WEP, nopass
    val wifiHidden: Boolean = false,
    // Contact / vCard
    val vcardFirstName: String = "QRWho",
    val vcardLastName: String = "Studio",
    val vcardPhone: String = "+1 555 019 2834",
    val vcardEmail: String = "hello@qrwho.app",
    val vcardOrg: String = "QRWho Creative Lab",
    val vcardTitle: String = "Artistic QR Engine",
    val vcardUrl: String = "https://qrwho.vercel.app",
    // Event
    val eventTitle: String = "QRWho Design Showcase",
    val eventLocation: String = "San Francisco, CA",
    val eventDescription: String = "Explore scannable generative QR art and photo weaving.",
    val eventStart: String = "20261015T180000Z",
    val eventEnd: String = "20261015T210000Z",
    // Email
    val emailTo: String = "contact@qrwho.app",
    val emailSubject: String = "Created with QRWho",
    val emailBody: String = "Hello! I scanned your artistic QR code created on QRWho.",
    // Phone & SMS
    val phoneNumber: String = "+1 800 555 0199",
    val smsNumber: String = "+1 800 555 0199",
    val smsMessage: String = "Scanned your QR code!",
    // WhatsApp
    val waNumber: String = "+15550192834",
    val waMessage: String = "Hi! Connecting via QRWho.",
    // Payment / Crypto
    val paymentType: String = "UPI", // UPI, Bitcoin, Ethereum, Solana, PayPal
    val paymentAddress: String = "qrwho@upi",
    val paymentAmount: String = "10.00",
    val paymentNote: String = "Artistic QR Tip",
    // Geo
    val geoLat: Double = 37.7749,
    val geoLng: Double = -122.4194,
    val geoQuery: String = "San Francisco",
    // Plain text
    val text: String = "Turn any picture into a working QR code."
) {
    fun toEncodedText(): String {
        return when (kind) {
            PayloadKind.URL -> {
                val clean = url.trim()
                if (clean.startsWith("http://") || clean.startsWith("https://")) clean
                else if (clean.isNotEmpty()) "https://$clean"
                else "https://qrwho.vercel.app"
            }
            PayloadKind.WIFI -> {
                val enc = wifiEncryption.ifEmpty { "WPA" }
                val h = if (wifiHidden) "H:true;" else ""
                val p = if (enc == "nopass" || wifiPassword.isEmpty()) "" else "P:${escapeWifi(wifiPassword)};"
                "WIFI:S:${escapeWifi(wifiSsid)};T:$enc;$p$h;"
            }
            PayloadKind.VCARD -> {
                buildString {
                    append("BEGIN:VCARD\r\n")
                    append("VERSION:3.0\r\n")
                    append("N:${escapeVcard(vcardLastName)};${escapeVcard(vcardFirstName)};;;\r\n")
                    append("FN:${escapeVcard("$vcardFirstName $vcardLastName".trim())}\r\n")
                    if (vcardOrg.isNotEmpty()) append("ORG:${escapeVcard(vcardOrg)}\r\n")
                    if (vcardTitle.isNotEmpty()) append("TITLE:${escapeVcard(vcardTitle)}\r\n")
                    if (vcardPhone.isNotEmpty()) append("TEL;TYPE=CELL:${vcardPhone.trim()}\r\n")
                    if (vcardEmail.isNotEmpty()) append("EMAIL:${vcardEmail.trim()}\r\n")
                    if (vcardUrl.isNotEmpty()) append("URL:${vcardUrl.trim()}\r\n")
                    append("END:VCARD\r\n")
                }
            }
            PayloadKind.EVENT -> {
                buildString {
                    append("BEGIN:VCALENDAR\r\n")
                    append("VERSION:2.0\r\n")
                    append("PRODID:-//QRWho//Artistic QR Generator//EN\r\n")
                    append("BEGIN:VEVENT\r\n")
                    append("SUMMARY:${escapeVcard(eventTitle)}\r\n")
                    if (eventLocation.isNotEmpty()) append("LOCATION:${escapeVcard(eventLocation)}\r\n")
                    if (eventDescription.isNotEmpty()) append("DESCRIPTION:${escapeVcard(eventDescription)}\r\n")
                    append("DTSTART:$eventStart\r\n")
                    append("DTEND:$eventEnd\r\n")
                    append("END:VEVENT\r\n")
                    append("END:VCALENDAR\r\n")
                }
            }
            PayloadKind.EMAIL -> {
                val q = buildList {
                    if (emailSubject.isNotEmpty()) add("subject=${urlEncode(emailSubject)}")
                    if (emailBody.isNotEmpty()) add("body=${urlEncode(emailBody)}")
                }.joinToString("&")
                if (q.isNotEmpty()) "mailto:${emailTo.trim()}?$q" else "mailto:${emailTo.trim()}"
            }
            PayloadKind.PHONE -> "tel:${phoneNumber.replace("[^0-9+]".toRegex(), "")}"
            PayloadKind.SMS -> {
                val num = smsNumber.replace("[^0-9+]".toRegex(), "")
                if (smsMessage.isNotEmpty()) "smsto:$num:${smsMessage}" else "smsto:$num"
            }
            PayloadKind.WHATSAPP -> {
                val num = waNumber.replace("[^0-9]".toRegex(), "")
                val msg = if (waMessage.isNotEmpty()) "?text=${urlEncode(waMessage)}" else ""
                "https://wa.me/$num$msg"
            }
            PayloadKind.PAYMENT -> {
                when (paymentType.uppercase()) {
                    "UPI" -> "upi://pay?pa=${paymentAddress.trim()}&pn=QRWho&am=$paymentAmount&cu=INR&tn=${urlEncode(paymentNote)}"
                    "BITCOIN" -> "bitcoin:${paymentAddress.trim()}${if (paymentAmount.isNotEmpty()) "?amount=$paymentAmount" else ""}"
                    "ETHEREUM" -> "ethereum:${paymentAddress.trim()}${if (paymentAmount.isNotEmpty()) "?value=$paymentAmount" else ""}"
                    "SOLANA" -> "solana:${paymentAddress.trim()}${if (paymentAmount.isNotEmpty()) "?amount=$paymentAmount" else ""}"
                    "PAYPAL" -> "https://paypal.me/${paymentAddress.trim()}${if (paymentAmount.isNotEmpty()) "/$paymentAmount" else ""}"
                    else -> paymentAddress.trim()
                }
            }
            PayloadKind.GEO -> "geo:$geoLat,$geoLng?q=${urlEncode(geoQuery.ifEmpty { "$geoLat,$geoLng" })}"
            PayloadKind.TEXT -> text.ifEmpty { "QRWho" }
        }
    }

    private fun escapeWifi(s: String): String =
        s.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace(":", "\\:")

    private fun escapeVcard(s: String): String =
        s.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n")

    private fun urlEncode(s: String): String = try {
        URLEncoder.encode(s, "UTF-8")
    } catch (_: Exception) {
        s
    }
}

enum class ModuleShape(val label: String, val apiKey: String) {
    Square("Square", "square"),
    Rounded("Rounded", "rounded"),
    Squircle("Squircle", "squircle"),
    Dots("Dots", "dots"),
    Diamond("Diamond", "diamond"),
    Star("Star", "star"),
    Plus("Plus", "plus"),
    Classy("Classy", "classy"),
    Leaf("Leaf", "leaf"),
    Fluid("Fluid", "fluid"),
    Hex("Hexagon", "hex"),
    Heart("Heart", "heart"),
    Confetti("Confetti", "confetti"),
    Dash("Dash", "dash"),
    Cross("Cross", "cross"),
    Diag("Streak", "diag"),
    Radial("Burst", "radial"),
    Bubbles("Bubbles", "bubbles"),
    HBar("Horizontal Bar", "hbar"),
    VBar("Vertical Bar", "vbar");

    companion object {
        fun fromString(key: String): ModuleShape =
            values().find { it.apiKey.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: Rounded
    }
}

enum class EyeShape(val label: String, val apiKey: String) {
    Square("Square", "square"),
    Rounded("Rounded", "rounded"),
    ExtraRounded("Extra Round", "extra-rounded"),
    Circle("Circle", "circle"),
    Classy("Classy", "classy"),
    Leaf("Leaf", "leaf"),
    Diamond("Diamond", "diamond"),
    Hex("Hexagon", "hex"),
    Target("Target", "target"),
    Ticks("Ticks", "ticks");

    companion object {
        fun fromString(key: String): EyeShape =
            values().find { it.apiKey.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: Rounded
    }
}

enum class ImageMode(val label: String, val desc: String, val apiKey: String) {
    Paint("Photo QR", "Photograph is built from the QR. Module centers carry code, outer area is photo halftone.", "paint"),
    Clean("Clean overlay", "The photo sits underneath at full strength; crisp modules with finder plates float on top.", "clean"),
    Mosaic("Color blend", "Each module is one contrast-normalized color from the photo", "mosaic"),
    Halftone("Halftone", "Same lattice in black ink on paper — newspaper dots, not colored rings", "halftone"),
    Duotone("Duotone", "Two inks sampled from the photo, same center-locked weave", "duotone"),
    Mono("Mono ink", "One ink on paper. Density follows the picture", "mono"),
    Backdrop("Backdrop", "Translucent photo background behind crisp code", "backdrop"),
    Logo("Center Logo", "Center emblem only", "logo"),
    None("None", "Style only, no photo", "none");

    companion object {
        fun fromString(key: String): ImageMode =
            values().find { it.apiKey.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: None
    }
}

enum class PhotoKernel(val label: String, val hint: String, val apiKey: String) {
    Auto("Auto", "The engine picks the most photographic style that still passes the camera check", ""),
    Detail("Photo first", "The picture leads — photo tones fill frame, thin dots carry code", "detail"),
    Balanced("Balanced", "Half picture, half dot-grid — default feel", "balanced"),
    CameraSafe("Scan first", "Thickest locked centers — maximum distance + angle tolerance", "camera-safe")
}

data class WeavePreset(
    val id: String,
    val label: String,
    val hint: String,
    val imageMode: ImageMode,
    val moduleShape: ModuleShape,
    val artisticStrength: Float,
    val contrast: Float,
    val effect: QrEffect = QrEffect.None,
    val gradientType: GradientType = GradientType.None,
    val photoKernel: PhotoKernel = PhotoKernel.Detail
)

object WeavePresets {
    val list = listOf(
        WeavePreset(
            id = "portrait",
            label = "Portrait",
            hint = "Finer lattice for faces",
            imageMode = ImageMode.Paint,
            moduleShape = ModuleShape.Rounded,
            artisticStrength = 0.52f,
            contrast = 0.78f,
            photoKernel = PhotoKernel.Detail
        ),
        WeavePreset(
            id = "nature",
            label = "Nature",
            hint = "Color-blend foliage",
            imageMode = ImageMode.Mosaic,
            moduleShape = ModuleShape.Leaf,
            artisticStrength = 0.48f,
            contrast = 0.80f,
            gradientType = GradientType.Diagonal
        ),
        WeavePreset(
            id = "neon",
            label = "Neon",
            hint = "Punchy dark-on-color",
            imageMode = ImageMode.Mosaic,
            moduleShape = ModuleShape.Square,
            artisticStrength = 0.56f,
            contrast = 0.90f,
            effect = QrEffect.Glow,
            gradientType = GradientType.Diagonal
        ),
        WeavePreset(
            id = "ink",
            label = "Ink",
            hint = "Newspaper dots, center-locked",
            imageMode = ImageMode.Halftone,
            moduleShape = ModuleShape.Dots,
            artisticStrength = 0.40f,
            contrast = 0.88f,
            photoKernel = PhotoKernel.Detail
        ),
        WeavePreset(
            id = "luxury",
            label = "Luxury",
            hint = "Two inks, same lattice",
            imageMode = ImageMode.Duotone,
            moduleShape = ModuleShape.Rounded,
            artisticStrength = 0.44f,
            contrast = 0.84f,
            photoKernel = PhotoKernel.Detail
        ),
        WeavePreset(
            id = "minimal",
            label = "Minimal",
            hint = "One ink, density follows the photo",
            imageMode = ImageMode.Mono,
            moduleShape = ModuleShape.Square,
            artisticStrength = 0.28f,
            contrast = 0.92f,
            photoKernel = PhotoKernel.Detail
        ),
        WeavePreset(
            id = "pixel",
            label = "Pixel",
            hint = "Hard squares, Photo QR lattice",
            imageMode = ImageMode.Paint,
            moduleShape = ModuleShape.Square,
            artisticStrength = 0.46f,
            contrast = 0.86f,
            photoKernel = PhotoKernel.Detail
        ),
        WeavePreset(
            id = "organic",
            label = "Organic",
            hint = "Photo lattice, fluid dots",
            imageMode = ImageMode.Paint,
            moduleShape = ModuleShape.Fluid,
            artisticStrength = 0.50f,
            contrast = 0.80f,
            gradientType = GradientType.Diagonal,
            photoKernel = PhotoKernel.Detail
        )
    )
}

enum class QrEffect(val label: String) {
    None("None"),
    Shadow("Drop Shadow"),
    Glow("Neon Glow"),
    Outline("Outline Keyline"),
    Emboss("Emboss")
}

enum class FrameStyle(val label: String) {
    None("No Frame"),
    SimpleBorder("Clean Border"),
    BadgeScanMe("Scan Me Badge"),
    ModernPill("Modern Pill"),
    PhoneFrame("Smartphone Shell"),
    Stamp("Postage Stamp"),
    Ticket("Event Ticket"),
    NeonGlow("Neon Edge"),
    Bracket("Bracket"),
    Badge("Badge"),
    Arch("Arch"),
    Cup("Cup"),
    Card("Card"),
    Label("Label"),
    Speech("Speech Bubble"),
    Note("Note"),
    Globe("Globe"),
    Plaque("Plaque"),
    Pentagon("Pentagon"),
    Hexagon("Hexagon"),
    Diamond("Diamond"),
    Seal("Seal"),
    Bucket("Bucket");

    companion object {
        fun fromString(key: String): FrameStyle =
            values().find { it.name.equals(key, ignoreCase = true) || it.label.equals(key, ignoreCase = true) } ?: None
    }
}

enum class GradientType(val label: String, val apiKey: String) {
    None("Solid Color", "none"),
    Linear("Horizontal Linear", "linear"),
    Radial("Centered Radial", "radial"),
    Diagonal("Diagonal Flow", "diagonal");

    companion object {
        fun fromString(key: String): GradientType =
            values().find { it.apiKey.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: None
    }
}

data class QrStyle(
    val moduleShape: ModuleShape = ModuleShape.Rounded,
    val eyeShape: EyeShape = EyeShape.Rounded,
    val ballShape: EyeShape = EyeShape.Circle,
    val fgColor: Int = 0xFF0F172A.toInt(),
    val bgColor: Int = 0xFFFFFFFF.toInt(),
    val eyeColor: Int = 0xFF0F172A.toInt(),
    val ballColor: Int = 0xFF0F172A.toInt(),
    val gradientType: GradientType = GradientType.Diagonal,
    val gradientTo: Int = 0xFF7A5AF8.toInt(),
    val quietZone: Int = 3,
    val moduleGap: Float = 0.06f,
    val imageMode: ImageMode = ImageMode.None,
    val imageOpacity: Float = 0.86f,
    val photoZoom: Float = 1.0f,
    val dotScale: Float = 0.88f,
    val contrast: Float = 1.0f,
    val logoScale: Float = 0.22f,
    val artisticStrength: Float = 0.42f,
    val effect: QrEffect = QrEffect.None,
    val ecc: String = "H", // ISO/IEC 18004 Error Correction Level H default for maximum scannability
    val transparentBg: Boolean = false,
    val frameStyle: FrameStyle = FrameStyle.None,
    val frameCaption: String = "SCAN ME",
    val selectedLogoId: String? = null,
    val artDirection: String? = null,
    val photoKernel: PhotoKernel = PhotoKernel.Auto,
    val smartArt: Boolean = false,
    val minVersion: Int = 7
)

data class QrPreset(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val style: QrStyle,
    val featured: Boolean = false
)
