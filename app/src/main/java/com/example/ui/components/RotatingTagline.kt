package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RotatingTagline(
    prefix: String = "Artistic QR codes that ",
    words: List<String> = listOf("scan", "pop", "stand out", "convert", "inspire", "dazzle"),
    fontSize: TextUnit = 11.sp,
    modifier: Modifier = Modifier,
    prefixColor: Color = TextMuted,
    highlightColor: Color = ElectricCyan
) {
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(words) {
        while (true) {
            delay(2200)
            currentIndex = (currentIndex + 1) % words.size
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prefix.isNotEmpty()) {
            Text(
                text = prefix,
                color = prefixColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false
            )
        }
        AnimatedContent(
            targetState = words[currentIndex],
            transitionSpec = {
                (slideInVertically { height -> height / 2 } + fadeIn())
                    .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut())
            },
            label = "RotatingTaglineWord"
        ) { word ->
            Text(
                text = word,
                color = highlightColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
