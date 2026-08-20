package com.mediwise.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary          = PrimaryBlue,
    onPrimary        = Color.White,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = PrimaryBlueDark,
    secondary        = AIPurple,
    onSecondary      = Color.White,
    tertiary         = AccentGreen,
    onTertiary       = Color.White,
    background       = BackgroundPrimary,
    onBackground     = TextPrimary,
    surface          = SurfaceWhite,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error            = ErrorRed,
    onError          = Color.White,
    errorContainer   = ErrorRedLight,
    outline          = Divider,
    outlineVariant   = CardBorder
)

@Composable
fun ClinicalSystemTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}
