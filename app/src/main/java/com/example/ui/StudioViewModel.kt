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
import com.example.data.CustomPresetEntity
import com.example.data.FavoritePresetEntity
import com.example.data.QrEntity
import com.example.data.QrRepository
import com.example.qr.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val customPresets: StateFlow<List<CustomPresetEntity>> = repository.customPresets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val favoriteIds: StateFlow<Set<String>> = repository.favoriteIds
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
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

    private val _presetFilterCategory = MutableStateFlow("All")
    val presetFilterCategory: StateFlow<String> = _presetFilterCategory.asStateFlow()

    private val _historyFilter = MutableStateFlow("All")
    val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _showKofiDialog = MutableStateFlow(false)
    val showKofiDialog: StateFlow<Boolean> = _showKofiDialog.asStateFlow()

    private val _kofiCountdown = MutableStateFlow(15)
    val kofiCountdown: StateFlow<Int> = _kofiCountdown.asStateFlow()

    private var renderJob: Job? = null
    private var kofiTimerJob: Job? = null

    init {
        // Initialize presets from assets (all 335 items)
        QrPresets.initialize(application)
        triggerRender()
    }

    fun triggerKofiSupportModal() {
        kofiTimerJob?.cancel()
        kofiTimerJob = viewModelScope.launch {
            _kofiCountdown.value = 15
            _showKofiDialog.value = true
            for (i in 14 downTo 0) {
                kotlinx.coroutines.delay(1000)
                _kofiCountdown.value = i
            }
        }
    }

    fun dismissKofiDialog() {
        kofiTimerJob?.cancel()
        _showKofiDialog.value = false
    }

    fun setStudioTab(index: Int) {
        _activeStudioTab.value = index
    }

    fun setPresetCategory(category: String) {
        _presetFilterCategory.value = category
    }

    fun setHistoryFilter(filter: String) {
        _historyFilter.value = filter
    }

    fun navigateToMyPresets() {
        _activeStudioTab.value = 1
        _presetFilterCategory.value = "✨ My Presets"
    }

    fun navigateToFavorites() {
        _activeStudioTab.value = 1
        _presetFilterCategory.value = "★ Favorites"
    }

    fun navigateToHistoryCreated() {
        _activeStudioTab.value = 4
        _historyFilter.value = "Created"
    }

    fun navigateToHistoryScanned() {
        _activeStudioTab.value = 4
        _historyFilter.value = "Scanned"
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
            _style.value = _style.value.copy(
                selectedLogoId = null,
                ecc = "H", // Automatic Error Correction Level H (30%) for guaranteed scannability with center logo
                logoScale = _style.value.logoScale.coerceIn(0.20f, 0.26f)
            )
        }
        triggerRender()
    }

    fun saveCustomPreset(name: String, description: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val s = _style.value
            val entity = CustomPresetEntity(
                name = name.ifBlank { "My Preset ${System.currentTimeMillis() % 1000}" },
                description = description.ifBlank { "Custom ${s.moduleShape.label} Style" },
                moduleShape = s.moduleShape.name,
                eyeShape = s.eyeShape.name,
                ballShape = s.ballShape.name,
                fgColor = s.fgColor,
                bgColor = s.bgColor,
                eyeColor = s.eyeColor,
                ballColor = s.ballColor,
                gradientType = s.gradientType.name,
                gradientTo = s.gradientTo,
                frameStyle = s.frameStyle.name,
                frameCaption = s.frameCaption,
                quietZone = s.quietZone,
                moduleGap = s.moduleGap,
                dotScale = s.dotScale,
                contrast = s.contrast,
                ecc = s.ecc
            )
            repository.saveCustomPreset(entity)
            withContext(Dispatchers.Main) {
                _activeStudioTab.value = 1
                _presetFilterCategory.value = "✨ My Presets"
                _userMessage.value = "Saved '${entity.name}' to My Presets!"
            }
        }
    }

    fun deleteCustomPreset(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCustomPreset(id)
            withContext(Dispatchers.Main) {
                _userMessage.value = "Deleted preset from My Presets"
            }
        }
    }

    fun toggleFavorite(presetId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = favoriteIds.value
            val isFav = current.contains(presetId)
            val presetName = QrPresets.findById(presetId)?.name
                ?: customPresets.value.find { "custom_${it.id}" == presetId }?.name
                ?: "Style"
            if (isFav) {
                repository.removeFavorite(presetId)
                withContext(Dispatchers.Main) {
                    _userMessage.value = "Removed '$presetName' from Favorites"
                }
            } else {
                repository.addFavorite(presetId)
                withContext(Dispatchers.Main) {
                    _userMessage.value = "★ Added '$presetName' to Favorites!"
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
            withContext(Dispatchers.Main) {
                _userMessage.value = "Cleared all history"
            }
        }
    }

    fun clearHistoryByType(isScanned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearByType(isScanned)
            withContext(Dispatchers.Main) {
                _userMessage.value = if (isScanned) "Cleared scanned QR history" else "Cleared saved QR creations"
            }
        }
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
        recordScannedQr(trimmed)
        _userMessage.value = "Loaded scanned QR content into Studio!"
        triggerRender()
    }

    fun recordScannedQr(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val trimmed = text.trim()
            val (kind, title) = when {
                trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) ->
                    Pair(PayloadKind.URL, trimmed.take(45))
                trimmed.startsWith("WIFI:", ignoreCase = true) -> {
                    val ssid = Regex("S:([^;]+)").find(trimmed)?.groupValues?.get(1) ?: "Network"
                    Pair(PayloadKind.WIFI, "Wi-Fi: $ssid")
                }
                trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) -> {
                    val fn = Regex("FN:([^\r\n]+)").find(trimmed)?.groupValues?.get(1) ?: "Contact"
                    Pair(PayloadKind.VCARD, "Contact: $fn")
                }
                trimmed.startsWith("mailto:", ignoreCase = true) ->
                    Pair(PayloadKind.EMAIL, "Email: ${trimmed.removePrefix("mailto:").substringBefore("?")}")
                trimmed.startsWith("tel:", ignoreCase = true) ->
                    Pair(PayloadKind.PHONE, "Phone: ${trimmed.removePrefix("tel:")}")
                trimmed.startsWith("smsto:", ignoreCase = true) ->
                    Pair(PayloadKind.SMS, "SMS: ${trimmed.removePrefix("smsto:").substringBefore(":")}")
                trimmed.startsWith("geo:", ignoreCase = true) ->
                    Pair(PayloadKind.GEO, "Map Location")
                else ->
                    Pair(PayloadKind.TEXT, trimmed.take(35).ifBlank { "Scanned QR" })
            }
            val entity = QrEntity(
                title = title,
                payloadKind = kind.name,
                payloadRaw = trimmed,
                encodedText = trimmed,
                presetName = "Scanned",
                isScanned = true
            )
            repository.saveQr(entity)
        }
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
                    PayloadKind.EMAIL -> p.emailTo.ifBlank { "Email Message" }
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
                _userMessage.value = "Saved '${entity.title}' to History Vault!"
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
                        _payload.value.copy(kind = PayloadKind.EMAIL, emailTo = trimmed.removePrefix("mailto:"))
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
                val chooser = Intent.createChooser(intent, "Share QR Code").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "Share failed: ${e.localizedMessage}"
                }
            }
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
                    triggerKofiSupportModal()
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

                // 1. Download & Save directly to user's device Downloads/QRWho storage
                var savedToDownloads = false
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/svg+xml")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QRWho")
                        }
                        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                out.write(svgContent.toByteArray(Charsets.UTF_8))
                            }
                            savedToDownloads = true
                        }
                    } else {
                        val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "QRWho")
                        if (!downloadsDir.exists()) downloadsDir.mkdirs()
                        val file = File(downloadsDir, filename)
                        file.writeText(svgContent, Charsets.UTF_8)
                        savedToDownloads = true
                    }
                } catch (_: Exception) {}

                // 2. Also write to cache for FileProvider sharing / export chooser
                val dir = File(context.cacheDir, "exports")
                if (!dir.exists()) dir.mkdirs()
                val cacheFile = File(dir, filename)
                cacheFile.writeText(svgContent, Charsets.UTF_8)

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/svg+xml"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Artistic QR Code (Vector SVG)")
                    putExtra(Intent.EXTRA_TEXT, "Vector SVG QR Code created with QRWho")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, "Export Vector SVG").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                try {
                    context.startActivity(chooser)
                } catch (_: Exception) {
                    try {
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_TEXT, svgContent)
                        shareIntent.removeExtra(Intent.EXTRA_STREAM)
                        context.startActivity(Intent.createChooser(shareIntent, "Export SVG Content").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (_: Exception) {}
                }

                withContext(Dispatchers.Main) {
                    _userMessage.value = if (savedToDownloads) {
                        "Vector SVG saved to Downloads/QRWho & ready to export!"
                    } else {
                        "Exported genuine vector SVG file!"
                    }
                    triggerKofiSupportModal()
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
                val chooser = Intent.createChooser(intent, "Share QR Code").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "Share failed: ${e.localizedMessage}"
                }
            }
        }
    }
}
