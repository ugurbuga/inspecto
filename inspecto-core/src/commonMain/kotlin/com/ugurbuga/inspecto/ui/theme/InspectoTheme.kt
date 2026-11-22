package com.ugurbuga.inspecto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.ugurbuga.inspecto.Inspecto

val DarkBackgroundColor = Color(0xFF0F172A)

val InspectoLightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = Color(0xFF0EA5E9),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFE11D48)
)

val InspectoDarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF1E3A8A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF38BDF8),
    surface = DarkBackgroundColor,
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainer = Color(0xFF1E293B),
    surfaceContainerLow = DarkBackgroundColor,
    surfaceContainerHigh = Color(0xFF334155),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF475569),
    error = Color(0xFFF87171)
)

@Composable
internal fun InspectoTheme(
    isDarkTheme: Boolean,
    fontFamily: FontFamily = Inspecto.customFontFamily ?: getInspectoFontFamily(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) InspectoDarkColorScheme else InspectoLightColorScheme

    SetStatusBarIconColor(darkIcons = !isDarkTheme)

    val customTypography = Typography(
        displayLarge = TextStyle(fontFamily = fontFamily),
        displayMedium = TextStyle(fontFamily = fontFamily),
        displaySmall = TextStyle(fontFamily = fontFamily),
        headlineLarge = TextStyle(fontFamily = fontFamily),
        headlineMedium = TextStyle(fontFamily = fontFamily),
        headlineSmall = TextStyle(fontFamily = fontFamily),
        titleLarge = TextStyle(fontFamily = fontFamily),
        titleMedium = TextStyle(fontFamily = fontFamily),
        titleSmall = TextStyle(fontFamily = fontFamily),
        bodyLarge = TextStyle(fontFamily = fontFamily),
        bodyMedium = TextStyle(fontFamily = fontFamily),
        bodySmall = TextStyle(fontFamily = fontFamily),
        labelLarge = TextStyle(fontFamily = fontFamily),
        labelMedium = TextStyle(fontFamily = fontFamily),
        labelSmall = TextStyle(fontFamily = fontFamily)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content
    )
}
