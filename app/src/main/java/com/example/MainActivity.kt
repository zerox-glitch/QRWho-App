package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.StudioViewModel
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.ShowcaseScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = androidx.compose.ui.platform.LocalContext.current
                val prefs = remember { context.getSharedPreferences("qrwho_prefs", android.content.Context.MODE_PRIVATE) }
                val hasSeenWelcome = remember { prefs.getBoolean("has_seen_welcome_v1", false) }
                var showSplashScreen by remember { androidx.compose.runtime.mutableStateOf(true) }
                var showWelcomeScreen by remember { androidx.compose.runtime.mutableStateOf(!hasSeenWelcome) }

                val viewModel: StudioViewModel = viewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
                val historyList by viewModel.historyList.collectAsStateWithLifecycle()

                // 0: Home / Landing, 1: Studio, 2: Camera Scanner, 3: History Vault, 4: Showcase
                var currentNavDestination by remember { mutableIntStateOf(1) } // Default to Studio as requested for fast creation

                LaunchedEffect(userMessage) {
                    userMessage?.let { msg ->
                        val hasAction = msg.contains("History", ignoreCase = true) || msg.contains("Preset", ignoreCase = true)
                        val actionText = if (msg.contains("History", ignoreCase = true)) "View Vault" else if (msg.contains("Preset", ignoreCase = true)) "View" else null
                        val result = snackbarHostState.showSnackbar(
                            message = msg,
                            actionLabel = actionText,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            if (msg.contains("History", ignoreCase = true)) {
                                currentNavDestination = 3
                            } else if (msg.contains("Preset", ignoreCase = true)) {
                                currentNavDestination = 1
                            }
                        }
                        viewModel.clearUserMessage()
                    }
                }

                if (showSplashScreen) {
                    SplashScreen(
                        onFinished = { showSplashScreen = false }
                    )
                } else if (showWelcomeScreen) {
                    WelcomeScreen(
                        onStartCreating = {
                            prefs.edit().putBoolean("has_seen_welcome_v1", true).apply()
                            showWelcomeScreen = false
                            currentNavDestination = 1
                        },
                        onOpenScanner = {
                            prefs.edit().putBoolean("has_seen_welcome_v1", true).apply()
                            showWelcomeScreen = false
                            currentNavDestination = 2
                        },
                        onDismiss = {
                            prefs.edit().putBoolean("has_seen_welcome_v1", true).apply()
                            showWelcomeScreen = false
                        },
                        onSelectPreset = { preset ->
                            prefs.edit().putBoolean("has_seen_welcome_v1", true).apply()
                            viewModel.selectPreset(preset)
                            showWelcomeScreen = false
                            currentNavDestination = 1
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = BgDark,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .border(1.dp, CardBorder),
                            containerColor = SurfaceDark,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = currentNavDestination == 0,
                                onClick = { currentNavDestination = 0 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Home",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Home",
                                        fontSize = 10.sp,
                                        fontWeight = if (currentNavDestination == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0C0C0B),
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = ElectricCyan,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_home")
                            )

                            NavigationBarItem(
                                selected = currentNavDestination == 1,
                                onClick = { currentNavDestination = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Studio",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Studio",
                                        fontSize = 10.sp,
                                        fontWeight = if (currentNavDestination == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0C0C0B),
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = ElectricCyan,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_studio")
                            )

                            NavigationBarItem(
                                selected = currentNavDestination == 2,
                                onClick = { currentNavDestination = 2 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Scanner",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Scan",
                                        fontSize = 10.sp,
                                        fontWeight = if (currentNavDestination == 2) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0C0C0B),
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = ElectricCyan,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_scanner")
                            )

                            NavigationBarItem(
                                selected = currentNavDestination == 3,
                                onClick = { currentNavDestination = 3 },
                                icon = {
                                    if (historyList.isNotEmpty()) {
                                        BadgedBox(
                                            badge = {
                                                Badge(
                                                    containerColor = ElectricCyan,
                                                    contentColor = Color(0xFF0C0C0B)
                                                ) {
                                                    Text("${historyList.size}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = "History",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "History",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = "History",
                                        fontSize = 10.sp,
                                        fontWeight = if (currentNavDestination == 3) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0C0C0B),
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = ElectricCyan,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_history")
                            )

                            NavigationBarItem(
                                selected = currentNavDestination == 4,
                                onClick = { currentNavDestination = 4 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Showcase",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Showcase",
                                        fontSize = 10.sp,
                                        fontWeight = if (currentNavDestination == 4) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0C0C0B),
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = ElectricCyan,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_showcase")
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentNavDestination) {
                        0 -> LandingScreen(
                            viewModel = viewModel,
                            onNavigateToStudio = { currentNavDestination = 1 },
                            onNavigateToScanner = { currentNavDestination = 2 },
                            onNavigateToHistory = { currentNavDestination = 3 },
                            onNavigateToShowcase = { currentNavDestination = 4 },
                            onOpenWelcome = { showWelcomeScreen = true },
                            modifier = Modifier.padding(innerPadding)
                        )
                        1 -> StudioScreen(
                            viewModel = viewModel,
                            onNavigateToHistory = { currentNavDestination = 3 },
                            onOpenWelcome = { showWelcomeScreen = true },
                            modifier = Modifier.padding(innerPadding)
                        )
                        2 -> ScannerScreen(
                            viewModel = viewModel,
                            onNavigateToStudio = { currentNavDestination = 1 },
                            modifier = Modifier.padding(innerPadding)
                        )
                        3 -> HistoryScreen(
                            viewModel = viewModel,
                            onNavigateToStudio = { currentNavDestination = 1 },
                            onNavigateToScanner = { currentNavDestination = 2 },
                            modifier = Modifier.padding(innerPadding)
                        )
                        4 -> ShowcaseScreen(
                            viewModel = viewModel,
                            onNavigateToStudio = { currentNavDestination = 1 },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
            }
        }
    }
}
