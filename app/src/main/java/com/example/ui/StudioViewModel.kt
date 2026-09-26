package com.example.ui

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.QrEntity
import com.example.data.QrRepository
import com.example.qr.engine.BuiltInLogos
import com.example.qr.engine.ImageMode
import com.example.qr.engine.PayloadKind
import com.example.qr.engine.QrGenerator
import com.example.qr.engine.QrPayload
import com.example.qr.engine.QrPreset
import com.example.qr.engine.QrPresets
import com.example.qr.engine.QrScannabilityEvaluator
import com.example.qr.engine.QrStyle
import com.example.qr.engine.SamplePhotos
import com.example.qr.engine.ScanCheckResult
import com.example.qr.engine.ShowcaseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QrRepository(AppDatabase.getDatabase(application).qrDao())

    val historyList: StateFlow<List<QrEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _payload = MutableStateFlow(QrPayload())
    val payload: StateFlow<QrPayload> = _payload.asStateFlow()

    private val _style = MutableStateFlow(QrPresets.fallbackList.first().style)
    val style: StateFlow<QrStyle> = _style.asStateFlow()

    private val _photoBitmap = MutableStateFlow<Bitmap?>(null)
    val photoBitmap: StateFlow<Bitmap?> = _photoBitmap.asStateFlow()

    private val _customLogo = MutableStateFlow<Bitmap?>(null)
    val customLogo: StateFlow<Bitmap?> = _customLogo.asStateFlow()

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanCheckResult?>(null)
    val scanResult: StateFlow<ScanCheckResult?> = _scanResult.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _activeStudioTab = MutableStateFlow(0)
    val activeStudioTab: StateFlow<Int> = _activeStudioTab.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var renderJob: Job? = null

    init {
        // Initialize presets from assets (all 335 items)
        QrPresets.initialize(application)
        triggerRender()
    }

    fun setStudioTab(index: Int) {
        _activeStudioTab.value = index
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun updatePayload(newPayload: QrPayload) {
        _payload.value = newPayload
        triggerRender()
    }

    fun updateStyle(newStyle: QrStyle) {
        _style.value = newStyle
        triggerRender()
    }

    fun selectPreset(preset: QrPreset) {
        _style.value = preset.style
        _userMessage.value = "Applied ${preset.name} style"
        triggerRender()
    }

    fun applyShowcase(item: ShowcaseItem) {
        val foundPreset = QrPresets.findById(item.presetId)
        if (foundPreset != null) {
            _style.value = foundPreset.style
        }
        _payload.value = _payload.value.copy(
            kind = PayloadKind.URL,
            url = item.defaultUrl
        )
        _userMessage.value = "Loaded ${item.title} into Studio"
        triggerRender()
    }

    fun setPhoto(bitmap: Bitmap?) {
        _photoBitmap.value = bitmap
        if (bitmap != null) {
            _style.value = _style.value.copy(imageMode = ImageMode.Clean)
        }
        triggerRender()
    }

    fun selectSamplePhoto(sampleId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = SamplePhotos.loadSampleBitmap(getApplication(), sampleId)
            withContext(Dispatchers.Main) {
                setPhoto(bmp)
                _userMessage.value = "Sample photo loaded"
            }
        }
    }

    fun setCustomLogo(bitmap: Bitmap?) {
        _customLogo.value = bitmap
        if (bitmap != null) {
            _style.value = _style.value.copy(selectedLogoId = null)
        }
        triggerRender()
    }

    fun selectBuiltInLogo(logoId: String?) {
        _customLogo.value = null
        _style.value = _style.value.copy(selectedLogoId = logoId)
        triggerRender()
    }

    fun autoFixScan() {
        viewModelScope.launch(Dispatchers.Default) {
            val result = QrScannabilityEvaluator.optimizeScan(
                current = _style.value,
                payloadText = _payload.value.toEncodedText(),
                photoBitmap = _photoBitmap.value,
                customLogo = _customLogo.value
            )
            withContext(Dispatchers.Main) {
                _style.value = result.style
                val summary = if (result.notes.isNotEmpty()) {
                    result.notes.take(3).joinToString(", ")
                } else {
                    "Error Correction H & Contrast boosted"
                }
                _userMessage.value = "Fix scan applied: $summary"
            }
            triggerRender()
        }
    }

    private fun triggerRender() {
        renderJob?.cancel()
        renderJob = viewModelScope.launch(Dispatchers.Default) {
            _isGenerating.value = true
            try {
                val currentPayloadText = _payload.value.toEncodedText()
                val currentStyle = _style.value
                val currentPhoto = _photoBitmap.value
                val currentLogo = _customLogo.value

                val bmp = QrGenerator.generateQrBitmap(
                    payload = currentPayloadText,
                    qrStyle = currentStyle,
                    photoBitmap = currentPhoto,
                    customLogo = currentLogo,
                    sizePx = 1024
                )

                val evalResult = QrScannabilityEvaluator.evaluate(bmp)

                withContext(Dispatchers.Main) {
                    _qrBitmap.value = bmp
                    _scanResult.value = evalResult
                    _isGenerating.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isGenerating.value = false
                    _userMessage.value = "Render error: ${e.localizedMessage}"
                }
            }
        }
    }

    fun onScannedFromCamera(scannedText: String) {
        val trimmed = scannedText.trim()
        val detected = when {
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> {
                _payload.value.copy(kind = PayloadKind.URL, url = trimmed)
            }
            trimmed.startsWith("WIFI:", ignoreCase = true) -> {
                // Parse SSID and Password
                val ssid = Regex("S:([^;]+)").find(trimmed)?.groupValues?.get(1) ?: "WiFi"
                val pwd = Regex("P:([^;]+)").find(trimmed)?.groupValues?.get(1) ?: ""
                val enc = Regex("T:([^;]+)").find(trimmed)?.groupValues?.get(1) ?: "WPA"
                _payload.value.copy(kind = PayloadKind.WIFI, wifiSsid = ssid, wifiPassword = pwd, wifiEncryption = enc)
            }
            trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) -> {
                val fn = Regex("FN:([^\r\n]+)").find(trimmed)?.groupValues?.get(1) ?: "Contact"
                val tel = Regex("TEL[^:]*:([^\r\n]+)").find(trimmed)?.groupValues?.get(1) ?: ""
                val email = Regex("EMAIL[^:]*:([^\r\n]+)").find(trimmed)?.groupValues?.get(1) ?: ""
                _payload.value.copy(kind = PayloadKind.VCARD, vcardFirstName = fn, vcardLastName = "", vcardPhone = tel, vcardEmail = email)
            }
            trimmed.startsWith("mailto:", ignoreCase = true) -> {
                val email = trimmed.removePrefix("mailto:").substringBefore("?")
                _payload.value.copy(kind = PayloadKind.EMAIL, emailTo = email)
            }
            trimmed.startsWith("tel:", ignoreCase = true) -> {
                val phone = trimmed.removePrefix("tel:")
                _payload.value.copy(kind = PayloadKind.PHONE, phoneNumber = phone)
            }
            trimmed.startsWith("smsto:", ignoreCase = true) -> {
                val parts = trimmed.removePrefix("smsto:").split(":")
                val num = parts.getOrNull(0) ?: ""
                val msg = parts.getOrNull(1) ?: ""
                _payload.value.copy(kind = PayloadKind.SMS, smsNumber = num, smsMessage = msg)
            }
            trimmed.startsWith("geo:", ignoreCase = true) -> {
                _payload.value.copy(kind = PayloadKind.GEO, geoQuery = trimmed.removePrefix("geo:"))
            }
            else -> {
                _payload.value.copy(kind = PayloadKind.TEXT, text = trimmed)
            }
        }
        _payload.value = detected
        _userMessage.value = "Loaded scanned QR content into Studio!"
        triggerRender()
    }

    fun saveToHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val style = _style.value
            val p = _payload.value
            val entity = QrEntity(
                title = when (p.kind) {
                    PayloadKind.URL -> p.url.ifBlank { "Website URL" }
                    PayloadKind.WIFI -> "Wi-Fi: ${p.wifiSsid}"
                    PayloadKind.VCARD -> "${p.vcardFirstName} ${p.vcardLastName}".trim().ifBlank { "Contact Card" }
                    PayloadKind.EVENT -> p.eventTitle.ifBlank { "Event Calendar" }
                    PayloadKind.EMAIL -> p.emailAddress.ifBlank { "Email Message" }
                    PayloadKind.PHONE -> p.phoneNumber.ifBlank { "Phone Call" }
                    PayloadKind.SMS -> "SMS: ${p.smsNumber}"
                    PayloadKind.GEO -> "Map Location"
                    else -> p.text.take(30).ifBlank { "Text Note" }
                },
                payloadKind = p.kind.name,
                payloadRaw = p.toEncodedText(),
                encodedText = p.toEncodedText(),
                presetName = style.artDirection,
                moduleShape = style.moduleShape.name,
                eyeShape = style.eyeShape.name,
                ballShape = style.ballShape.name,
                fgColor = style.fgColor,
                bgColor = style.bgColor,
                eyeColor = style.eyeColor,
                ballColor = style.ballColor,
                gradientType = style.gradientType.name,
                gradientTo = style.gradientTo,
                frameStyle = style.frameStyle.name,
                frameCaption = style.frameCaption,
                quietZone = style.quietZone,
                moduleGap = style.moduleGap,
                dotScale = style.dotScale,
                contrast = style.contrast,
                imageMode = style.imageMode.name,
                imageOpacity = style.imageOpacity,
                ecc = style.ecc,
                scanScore = _scanResult.value?.score ?: 98
            )
            repository.saveQr(entity)
            withContext(Dispatchers.Main) {
                _userMessage.value = "Saved '${entity.title}' to History!"
            }
        }
    }

    fun restoreFromHistory(entity: QrEntity) {
        val kind = try {
            PayloadKind.valueOf(entity.payloadKind)
        } catch (_: Exception) {
            PayloadKind.TEXT
        }

        val restoredPayload = when (kind) {
            PayloadKind.URL -> _payload.value.copy(kind = PayloadKind.URL, url = entity.encodedText)
            PayloadKind.TEXT -> _payload.value.copy(kind = PayloadKind.TEXT, text = entity.encodedText)
            else -> {
                val trimmed = entity.encodedText.trim()
                when {
                    trimmed.startsWith("WIFI:", ignoreCase = true) -> {
                        val ssid = Regex("S:([^;]+)").find(trimmed)?.groupValues?.get(1) ?: "WiFi"
                        val pwd = Regex("P:([^;]+)").find(trimmed)?.groupValues?.get(1) ?: ""
                        val enc = Regex("T:([^;]+)").find(trimmed)?.groupValues?.get(1) ?: "WPA"
                        _payload.value.copy(kind = PayloadKind.WIFI, wifiSsid = ssid, wifiPassword = pwd, wifiEncryption = enc)
                    }
                    trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) -> {
                        val fn = Regex("FN:([^\r\n]+)").find(trimmed)?.groupValues?.get(1) ?: "Contact"
                        val tel = Regex("TEL[^:]*:([^\r\n]+)").find(trimmed)?.groupValues?.get(1) ?: ""
                        val email = Regex("EMAIL[^:]*:([^\r\n]+)").find(trimmed)?.groupValues?.get(1) ?: ""
                        _payload.value.copy(kind = PayloadKind.VCARD, vcardFirstName = fn, vcardLastName = "", vcardPhone = tel, vcardEmail = email)
                    }
                    trimmed.startsWith("mailto:", ignoreCase = true) -> {
                        _payload.value.copy(kind = PayloadKind.EMAIL, emailAddress = trimmed.removePrefix("mailto:"))
                    }
                    trimmed.startsWith("tel:", ignoreCase = true) -> {
                        _payload.value.copy(kind = PayloadKind.PHONE, phoneNumber = trimmed.removePrefix("tel:"))
                    }
                    trimmed.startsWith("smsto:", ignoreCase = true) -> {
                        val parts = trimmed.removePrefix("smsto:").split(":")
                        _payload.value.copy(kind = PayloadKind.SMS, smsNumber = parts.getOrNull(0) ?: "", smsMessage = parts.getOrNull(1) ?: "")
                    }
                    trimmed.startsWith("geo:", ignoreCase = true) -> {
                        _payload.value.copy(kind = PayloadKind.GEO, geoQuery = trimmed.removePrefix("geo:"))
                    }
                    else -> _payload.value.copy(kind = kind, text = entity.encodedText, url = entity.encodedText)
                }
            }
        }

        val restoredStyle = QrStyle(
            moduleShape = try { ModuleShape.valueOf(entity.moduleShape) } catch (_: Exception) { ModuleShape.Rounded },
            eyeShape = try { EyeShape.valueOf(entity.eyeShape) } catch (_: Exception) { EyeShape.Rounded },
            ballShape = try { EyeShape.valueOf(entity.ballShape) } catch (_: Exception) { EyeShape.Circle },
            fgColor = entity.fgColor,
            bgColor = entity.bgColor,
            eyeColor = entity.eyeColor,
            ballColor = entity.ballColor,
            gradientType = try { GradientType.valueOf(entity.gradientType) } catch (_: Exception) { GradientType.None },
            gradientTo = entity.gradientTo,
            frameStyle = try { FrameStyle.valueOf(entity.frameStyle) } catch (_: Exception) { FrameStyle.None },
            frameCaption = entity.frameCaption,
            quietZone = entity.quietZone,
            moduleGap = entity.moduleGap,
            dotScale = entity.dotScale,
            contrast = entity.contrast,
            imageMode = try { ImageMode.valueOf(entity.imageMode) } catch (_: Exception) { ImageMode.None },
            imageOpacity = entity.imageOpacity,
            ecc = entity.ecc,
            artDirection = entity.presetName
        )

        _payload.value = restoredPayload
        _style.value = restoredStyle
        _userMessage.value = "Restored '${entity.title}' into Studio!"
        triggerRender()
    }

    fun shareHistoryItem(context: Context, item: QrEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val itemStyle = QrStyle(
                    moduleShape = try { ModuleShape.valueOf(item.moduleShape) } catch (_: Exception) { ModuleShape.Rounded },
                    eyeShape = try { EyeShape.valueOf(item.eyeShape) } catch (_: Exception) { EyeShape.Rounded },
                    ballShape = try { EyeShape.valueOf(item.ballShape) } catch (_: Exception) { EyeShape.Circle },
                    fgColor = item.fgColor,
                    bgColor = item.bgColor,
                    eyeColor = item.eyeColor,
                    ballColor = item.ballColor,
                    gradientType = try { GradientType.valueOf(item.gradientType) } catch (_: Exception) { GradientType.None },
                    gradientTo = item.gradientTo,
                    frameStyle = try { FrameStyle.valueOf(item.frameStyle) } catch (_: Exception) { FrameStyle.None },
                    frameCaption = item.frameCaption,
                    quietZone = item.quietZone
                )
                val bmp = QrGenerator.generateQrBitmap(
                    payload = item.encodedText,
                    qrStyle = itemStyle,
                    sizePx = 768
                )
                val cachePath = File(context.cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "qr_share_${item.id}.png")
                FileOutputStream(file).use { out ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, item.title)
                    putExtra(Intent.EXTRA_TEXT, "${item.title}\n${item.encodedText}\n\nCreated with QRWho Studio")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share QR Code"))
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "Share failed: ${e.localizedMessage}"
                }
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    fun exportPng(context: Context, resolutionPx: Int = 2048) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val highResBitmap = QrGenerator.generateQrBitmap(
                    payload = _payload.value.toEncodedText(),
                    qrStyle = _style.value,
                    photoBitmap = _photoBitmap.value,
                    customLogo = _customLogo.value,
                    sizePx = resolutionPx
                )

                val filename = "QRWho_${System.currentTimeMillis()}_${resolutionPx}px.png"
                var outputStream: OutputStream? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRWho")
                    }
                    val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (imageUri != null) {
                        outputStream = context.contentResolver.openOutputStream(imageUri)
                    }
                } else {
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/QRWho"
                    val file = File(imagesDir)
                    if (!file.exists()) file.mkdirs()
                    val image = File(file, filename)
                    outputStream = FileOutputStream(image)
                }

                outputStream?.use {
                    highResBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                withContext(Dispatchers.Main) {
                    _userMessage.value = "Exported print-ready PNG (${resolutionPx}px) to Pictures/QRWho!"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "Export failed: ${e.localizedMessage}"
                }
            }
        }
    }

    fun getSvgString(sizePx: Int = 1024): String {
        return QrGenerator.generateQrSvg(
            payload = _payload.value.toEncodedText(),
            qrStyle = _style.value,
            sizePx = sizePx
        )
    }

    fun exportSvg(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val svgContent = getSvgString(1024)
                val filename = "QRWho_${System.currentTimeMillis()}.svg"
                val dir = File(context.cacheDir, "exports")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                file.writeText(svgContent)

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/svg+xml"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Artistic QR Code (Vector SVG)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export Vector SVG"))

                withContext(Dispatchers.Main) {
                    _userMessage.value = "Exported genuine vector SVG file!"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "SVG export failed: ${e.localizedMessage}"
                }
            }
        }
    }

    fun shareQrCode(context: Context) {
        val bmp = _qrBitmap.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cachePath = File(context.cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "qrwho_share.png")
                FileOutputStream(file).use { out ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Created with QRWho — 100% Free Artistic QR Code Studio")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share QR Code"))
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "Share failed: ${e.localizedMessage}"
                }
            }
        }
    }
}
