package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.qr.engine.QrScannabilityEvaluator
import com.example.ui.StudioViewModel
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    viewModel: StudioViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var scannedResult by remember { mutableStateOf<String?>(null) }

    // Pick image from gallery to scan
    val pickPhotoForScanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val evaluation = QrScannabilityEvaluator.evaluate(bitmap)
                if (evaluation.decodedText != null) {
                    scannedResult = evaluation.decodedText
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(BgDark)) {
        if (hasCameraPermission) {
            // Live Camera View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        val reader = MultiFormatReader()
                        val hints = mapOf<DecodeHintType, Any>(
                            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                            DecodeHintType.TRY_HARDER to true,
                            DecodeHintType.CHARACTER_SET to "UTF-8"
                        )
                        reader.setHints(hints)
                        var lastAnalysisTime = 0L

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val currentTime = System.currentTimeMillis()
                            // 60ms cadence: fast, responsive scanning matching native camera apps
                            if (currentTime - lastAnalysisTime >= 60L) {
                                lastAnalysisTime = currentTime
                                val buffer = imageProxy.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)
                                val origW = imageProxy.width
                                val origH = imageProxy.height
                                val rotation = imageProxy.imageInfo.rotationDegrees

                                // Rotate buffer according to camera sensor rotation
                                val (rotatedBytes, dims) = rotateYuvSensorBuffer(bytes, origW, origH, rotation)
                                val w = dims.first
                                val h = dims.second

                                // Center viewfinder crop (72% center crop where user is pointing)
                                val cropSize = (minOf(w, h) * 0.72f).toInt()
                                val cropX = (w - cropSize) / 2
                                val cropY = (h - cropSize) / 2

                                var decodedText: String? = null

                                // Pass 1: Center Crop with HybridBinarizer (Ultra-fast primary pass)
                                try {
                                    val centerSource = PlanarYUVLuminanceSource(
                                        rotatedBytes, w, h, cropX, cropY, cropSize, cropSize, false
                                    )
                                    val res = reader.decodeWithState(BinaryBitmap(HybridBinarizer(centerSource)))
                                    decodedText = res.text
                                } catch (_: Exception) {
                                    reader.reset()
                                }

                                // Pass 2: Center Crop Inverted (Dark mode / light modules on dark background)
                                if (decodedText.isNullOrEmpty()) {
                                    try {
                                        val centerSource = PlanarYUVLuminanceSource(
                                            rotatedBytes, w, h, cropX, cropY, cropSize, cropSize, false
                                        )
                                        val res = reader.decodeWithState(BinaryBitmap(HybridBinarizer(centerSource.invert())))
                                        decodedText = res.text
                                    } catch (_: Exception) {
                                        reader.reset()
                                    }
                                }

                                // Pass 3: Center Crop GlobalHistogramBinarizer (Soft lighting, shadows, gradients)
                                if (decodedText.isNullOrEmpty()) {
                                    try {
                                        val centerSource = PlanarYUVLuminanceSource(
                                            rotatedBytes, w, h, cropX, cropY, cropSize, cropSize, false
                                        )
                                        val res = reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(centerSource)))
                                        decodedText = res.text
                                    } catch (_: Exception) {
                                        reader.reset()
                                    }
                                }

                                // Pass 4: Full Frame HybridBinarizer (Codes held at edges of screen)
                                if (decodedText.isNullOrEmpty()) {
                                    try {
                                        val fullSource = PlanarYUVLuminanceSource(
                                            rotatedBytes, w, h, 0, 0, w, h, false
                                        )
                                        val res = reader.decodeWithState(BinaryBitmap(HybridBinarizer(fullSource)))
                                        decodedText = res.text
                                    } catch (_: Exception) {
                                        reader.reset()
                                    }
                                }

                                // Pass 5: Full Frame Inverted
                                if (decodedText.isNullOrEmpty()) {
                                    try {
                                        val fullSource = PlanarYUVLuminanceSource(
                                            rotatedBytes, w, h, 0, 0, w, h, false
                                        )
                                        val res = reader.decodeWithState(BinaryBitmap(HybridBinarizer(fullSource.invert())))
                                        decodedText = res.text
                                    } catch (_: Exception) {
                                        reader.reset()
                                    }
                                }

                                if (!decodedText.isNullOrEmpty() && decodedText != scannedResult) {
                                    scannedResult = decodedText
                                }
                            }
                            imageProxy.close()
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                ctx as androidx.lifecycle.LifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // Viewfinder HUD Reticle Overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1f)
                        .border(2.dp, ElectricCyan, RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.15f))
                )
            }
        } else {
            // Permission Fallback Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = ElectricCyan,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Camera Access Required", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Allow camera access to test-scan QR codes or choose an image from your device photos.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enable Camera", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Top Scanner Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner", tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Point at any QR code", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pick Image from Gallery to scan
                IconButton(
                    onClick = {
                        pickPhotoForScanLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, CardBorder, CircleShape)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Scan Image", tint = TextPrimary, modifier = Modifier.size(20.dp))
                }

                // Close Camera button
                IconButton(
                    onClick = onNavigateToStudio,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, CardBorder, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Camera", tint = TextPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Bottom Detection Card Sheet
        AnimatedVisibility(
            visible = scannedResult != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val text = scannedResult ?: ""
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, EmeraldGreen, RoundedCornerShape(20.dp)),
                color = CardDark
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("QR found — you're all set", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        IconButton(
                            onClick = { scannedResult = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = text,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Remake in Studio Button
                        Button(
                            onClick = {
                                viewModel.onScannedFromCamera(text)
                                onNavigateToStudio()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Remake", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remake", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Copy / Open
                        FilledTonalButton(
                            onClick = {
                                if (text.startsWith("http://") || text.startsWith("https://")) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
                                    context.startActivity(intent)
                                } else {
                                    clipboardManager.setText(AnnotatedString(text))
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CardBorder,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (text.startsWith("http")) Icons.Default.OpenInBrowser else Icons.Default.ContentCopy,
                                contentDescription = "Action",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Stop Camera / Done
                        FilledTonalButton(
                            onClick = onNavigateToStudio,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SurfaceDark,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fast rotation of raw camera Y-plane buffer (luminance) to match screen orientation.
 */
private fun rotateYuvSensorBuffer(data: ByteArray, width: Int, height: Int, rotation: Int): Pair<ByteArray, Pair<Int, Int>> {
    if (rotation == 0) return Pair(data, Pair(width, height))
    val rotated = ByteArray(width * height)
    when (rotation) {
        90 -> {
            var i = 0
            for (x in 0 until width) {
                for (y in height - 1 downTo 0) {
                    rotated[i++] = data[y * width + x]
                }
            }
            return Pair(rotated, Pair(height, width))
        }
        180 -> {
            var i = 0
            for (j in width * height - 1 downTo 0) {
                rotated[i++] = data[j]
            }
            return Pair(rotated, Pair(width, height))
        }
        270 -> {
            var i = 0
            for (x in width - 1 downTo 0) {
                for (y in 0 until height) {
                    rotated[i++] = data[y * width + x]
                }
            }
            return Pair(rotated, Pair(height, width))
        }
        else -> return Pair(data, Pair(width, height))
    }
}

