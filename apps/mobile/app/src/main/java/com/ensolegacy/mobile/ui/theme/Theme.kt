package com.ensolegacy.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Primary, approved design: warm paper + moss/clay (screen-spec §3 tokens). */
private val LightColors = lightColorScheme(
    primary = MossPrimary,
    onPrimary = PaperBackground,
    primaryContainer = MossPrimaryContainer,
    onPrimaryContainer = PaperBackground,
    secondary = ClaySecondary,
    onSecondary = PaperBackground,
    secondaryContainer = ClaySecondaryContainer,
    onSecondaryContainer = ClaySecondary,
    tertiary = ClaySecondary,
    onTertiary = PaperBackground,
    background = PaperBackground,
    onBackground = Espresso,
    surface = PaperSurface,
    onSurface = Espresso,
    surfaceVariant = PaperSurfaceContainer,
    onSurfaceVariant = Taupe,
    surfaceContainer = PaperSurfaceContainer,
    surfaceContainerHigh = PaperSurfaceContainerHigh,
    outline = PaperOutline,
    outlineVariant = PaperOutlineVariant,
    error = BrickError,
    onError = PaperBackground,
)

private val DarkColors = darkColorScheme(
    primary = MossLight,
    onPrimary = OnMoss,
    primaryContainer = MossDim,
    onPrimaryContainer = OnMoss,
    secondary = MossLight,
    onSecondary = OnMoss,
    tertiary = MossDim,
    onTertiary = OnMoss,
    background = NightBackground,
    onBackground = CoolWhite,
    surface = NightSurface,
    onSurface = CoolWhite,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = MutedGreenGrey,
    surfaceContainer = NightSurface,
    surfaceContainerHigh = NightSurfaceHigh,
    outline = NightOutline,
    outlineVariant = NightOutlineVar,
    error = CriticalDarkText,
    onError = NightBackground,
)

@Composable
fun EnsoLegacyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        // Light theme -> dark status-bar icons; dark theme -> light icons.
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EnsoTypography,
        content = content,
    )
}
