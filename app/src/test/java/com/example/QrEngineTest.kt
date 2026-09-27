package com.example

import com.example.qr.engine.PayloadKind
import com.example.qr.engine.QrGenerator
import com.example.qr.engine.QrPayload
import com.example.qr.engine.QrPresets
import com.example.qr.engine.QrScannabilityEvaluator
import com.example.qr.engine.QrStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QrEngineTest {

    @Test
    fun testPayloadEncoding_url() {
        val payload = QrPayload(kind = PayloadKind.URL, url = "https://qrwho.vercel.app")
        assertEquals("https://qrwho.vercel.app", payload.toEncodedText())
    }

    @Test
    fun testPayloadEncoding_wifi() {
        val payload = QrPayload(
            kind = PayloadKind.WIFI,
            wifiSsid = "Studio_WiFi",
            wifiPassword = "secret;pass",
            wifiEncryption = "WPA"
        )
        val text = payload.toEncodedText()
        assertTrue(text.startsWith("WIFI:S:Studio_WiFi;"))
        assertTrue(text.contains("T:WPA;"))
        assertTrue(text.contains("P:secret\\;pass;"))
    }

    @Test
    fun testPayloadEncoding_vcard() {
        val payload = QrPayload(
            kind = PayloadKind.VCARD,
            vcardFirstName = "QRWho",
            vcardLastName = "Studio",
            vcardPhone = "+123456789"
        )
        val text = payload.toEncodedText()
        assertTrue(text.startsWith("BEGIN:VCARD\r\n"))
        assertTrue(text.contains("FN:QRWho Studio"))
        assertTrue(text.contains("TEL;TYPE=CELL:+123456789"))
        assertTrue(text.endsWith("END:VCARD\r\n"))
    }

    @Test
    fun testQrGeneration_and_Evaluation() {
        val payload = "https://qrwho.vercel.app"
        val style = QrStyle(
            moduleShape = com.example.qr.engine.ModuleShape.Square,
            eyeShape = com.example.qr.engine.EyeShape.Square,
            ballShape = com.example.qr.engine.EyeShape.Square,
            gradientType = com.example.qr.engine.GradientType.None,
            moduleGap = 0f,
            dotScale = 1.0f
        )
        val bmp = QrGenerator.generateQrBitmap(payload, style, sizePx = 512)
        assertNotNull(bmp)
        assertEquals(512, bmp.width)
        assertEquals(512, bmp.height)

        val eval = QrScannabilityEvaluator.evaluate(bmp)
        assertTrue(eval.isScannable)
        assertEquals(payload, eval.decodedText)
        assertEquals("Verified", eval.status)
        assertTrue(eval.score >= 90)
    }

    @Test
    fun testVectorSvgGeneration() {
        val payload = "https://qrwho.vercel.app"
        val style = QrStyle()
        val svg = QrGenerator.generateQrSvg(payload, style, sizePx = 1024)
        assertNotNull(svg)
        assertTrue(svg.contains("<svg"))
        assertTrue(svg.contains("viewBox=\"0 0 1024 1024\""))
        assertTrue(svg.contains("</svg>"))
    }

    @Test
    fun testShowcaseStyles_fallbackList() {
        assertTrue(QrPresets.fallbackList.size >= 9)
        val ukiyo = QrPresets.fallbackList.find { it.id == "art-ukiyo" }
        assertNotNull(ukiyo)
        assertEquals("Ukiyo Wave", ukiyo?.name)
    }
}
