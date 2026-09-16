package br.imd.ufrn.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CapitularPrimary = Color(0xFF6D4AFF)
val CapitularDeepPurple = Color(0xFF3B245F)
val CapitularViolet = Color(0xFF8B5CF6)
val CapitularLavender = Color(0xFFEDE7FF)
val CapitularLavenderStrong = Color(0xFFDFD4FF)
val CapitularBackground = Color(0xFFFAF8FF)
val CapitularSurface = Color(0xFFFFFFFF)
val CapitularSurfaceVariant = Color(0xFFF4F0F8)
val CapitularTextPrimary = Color(0xFF211A2C)
val CapitularTextSecondary = Color(0xFF6F6878)
val CapitularOutline = Color(0xFF9B92A6)
val CapitularOutlineVariant = Color(0xFFE6E0EA)
val CapitularSuccess = Color(0xFF48A987)
val CapitularSuccessContainer = Color(0xFFE8F6F1)
val CapitularCoral = Color(0xFFF28C82)
val CapitularWarmYellow = Color(0xFFF4C95D)
val CapitularError = Color(0xFFC95462)
val CapitularErrorContainer = Color(0xFFFBE9EC)

private val capitularColors =
    lightColorScheme(
        primary = CapitularPrimary,
        onPrimary = CapitularSurface,
        primaryContainer = CapitularLavender,
        onPrimaryContainer = CapitularDeepPurple,
        secondary = CapitularViolet,
        onSecondary = CapitularSurface,
        secondaryContainer = CapitularLavenderStrong,
        onSecondaryContainer = CapitularDeepPurple,
        tertiary = CapitularSuccess,
        onTertiary = CapitularSurface,
        tertiaryContainer = CapitularSuccessContainer,
        onTertiaryContainer = Color(0xFF205A49),
        background = CapitularBackground,
        onBackground = CapitularTextPrimary,
        surface = CapitularSurface,
        onSurface = CapitularTextPrimary,
        surfaceVariant = CapitularSurfaceVariant,
        onSurfaceVariant = CapitularTextSecondary,
        outline = CapitularOutline,
        outlineVariant = CapitularOutlineVariant,
        error = CapitularError,
        onError = CapitularSurface,
        errorContainer = CapitularErrorContainer,
        onErrorContainer = Color(0xFF5B1521),
        surfaceTint = CapitularPrimary,
    )

@Composable
fun CapitularTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = capitularColors,
        content = content,
    )
}
