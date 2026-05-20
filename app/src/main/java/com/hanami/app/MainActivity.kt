package com.hanami.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.hanami.app.data.TokenStore
import com.hanami.app.ui.screens.*
import com.hanami.app.ui.theme.BgDeep
import com.hanami.app.ui.theme.HanamiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HanamiTheme {
                HanamiApp()
            }
        }
    }
}

@Composable
fun HanamiApp() {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val token by tokenStore.tokenFlow.collectAsState(initial = "")

    var selectedTab by remember { mutableStateOf(Tab.HOME) }
    var showSettings by remember { mutableStateOf(false) }
    var showConsole by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Top bar with nav pill + utility buttons
        HanamiTopBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onSettingsClick = { showSettings = true },
            onConsoleClick = { showConsole = true }
        )

        // Page content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                Tab.HOME    -> HomeScreen()
                Tab.SHOP    -> ShopScreen(token = token)
                Tab.DEVICE  -> DeviceScreen(token = token)
                Tab.MISSION -> MissionScreen()
            }
        }
    }

    // Dialogs
    if (showSettings) {
        SettingsDialog(
            tokenStore = tokenStore,
            currentToken = token,
            onDismiss = { showSettings = false }
        )
    }

    if (showConsole) {
        ConsoleDialog(onDismiss = { showConsole = false })
    }
}
