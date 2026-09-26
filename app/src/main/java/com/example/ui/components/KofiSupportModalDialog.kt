package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

val KofiRed = Color(0xFFFF5E5B)

@Composable
fun KofiDownloadPopup(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var secondsLeft by remember { mutableIntStateOf(15) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
        onDismiss()
    }

    KofiSupportModalDialog(
        countdownSeconds = secondsLeft,
        onSupportClick = {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/qrwho"))
                context.startActivity(intent)
            } catch (e: Exception) {}
        },
        onDismiss = onDismiss
    )
}
@Composable
fun KofiSupportModalDialog(
    countdownSeconds: Int,
    onSupportClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        // Full screen dimmed & blurred background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.86f))
                .padding(20.dp)
                .testTag("kofi_modal_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, KofiRed.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                color = CardDark,
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Countdown & Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(KofiRed.copy(alpha = 0.15f))
                                .border(1.dp, KofiRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalCafe,
                                    contentDescription = null,
                                    tint = KofiRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "KO-FI CREATOR SUPPORT",
                                    color = KofiRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${countdownSeconds}s",
                                    color = KofiRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("kofi_modal_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Coffee Heart Visual Icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(KofiRed.copy(alpha = 0.15f))
                            .border(1.5.dp, KofiRed.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalCafe,
                            contentDescription = "Support Us",
                            tint = KofiRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Emotional Hook Title
                    Text(
                        text = "Support Us to Keep QRWho Free & Make the World Colorful! ☕🎨",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Emotional Hook Description
                    Text(
                        text = "Your QR code is exported & ready! We craft 100% free, private, high-density artistic QR codes with zero subscriptions, ads, or paywalls.\n\nIf QRWho brought your design to life, please consider buying us a coffee on Ko-fi to help us keep innovating!",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 15-second visual progress bar
                    LinearProgressIndicator(
                        progress = { (15f - countdownSeconds.coerceIn(0, 15)) / 15f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = KofiRed,
                        trackColor = Color(0xFF1E293B),
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons: Primary Ko-fi + Close
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                onSupportClick()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KofiRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("kofi_modal_support_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalCafe,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Support on Ko-fi ☕",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("kofi_modal_continue_button")
                        ) {
                            Text(
                                text = if (countdownSeconds > 0) "Continue to App (${countdownSeconds}s)" else "Continue to App",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
