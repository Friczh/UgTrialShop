package com.hanami.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colors from the HTML design
val BgDeep       = Color(0xFF120D1A)   // body background
val BgSurface    = Color(0xFF1A1025)   // topbar/card background
val NavPillBg    = Color(0xFF2A1F38)   // pill container
val PurpleActive = Color(0xFF7C3AED)   // active/FAB
val PurpleLight  = Color(0xFFC084FC)   // hover accent
val TextPrimary  = Color(0xFFF0E8FF)
val TextMuted    = Color(0xFF9D88B5)
val TextDim      = Color(0xFFC4B5D8)
val Green        = Color(0xFF4CAF50)
val Red          = Color(0xFFEF5350)

private val DarkColorScheme = darkColorScheme(
    primary        = PurpleActive,
    onPrimary      = Color.White,
    secondary      = PurpleLight,
    onSecondary    = BgDeep,
    background     = BgDeep,
    onBackground   = TextPrimary,
    surface        = BgSurface,
    onSurface      = TextPrimary,
    surfaceVariant = NavPillBg,
    onSurfaceVariant = TextMuted,
    outline        = NavPillBg,
)

@Composable
fun HanamiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content
    )
}
