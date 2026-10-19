package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmberLight,
    onPrimary = OnEmber,
    primaryContainer = EmberContainer,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = AccentGold,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF633F00),
    onSecondaryContainer = Color(0xFFFFDFA0),
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = CharcoalBg,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = CharcoalBorder,
    error = DestructiveRed,
    onError = Color.White
)

@Composable
fun QuickFoodTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
