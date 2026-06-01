package com.ensolegacy.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Primary, approved design: warm paper + olive/clay. */
private val LightColors = lightColorScheme(
    primary = MossPrimary,
    onPrimary = PaperBackground,
    secondary = ClaySecondary,
    onSecondary = PaperBackground,
    tertiary = ClaySecondary,
    onTertiary = PaperBackground,
    background = PaperBackground,
    onBackground = Espresso,
    surface = PaperSurface,
    onSurface = Espresso,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = Taupe,
    outline = PaperOutline,
    error = BrickError,
    onError = PaperBackground,
)

/** DUMMY dark theme — placeholder until a real dark palette is designed. */
private val DarkColorsDummy = darkColorScheme(
    primary = EnsoOlive,
    onPrimary = EnsoNight,
    secondary = EnsoGold,
    onSecondary = EnsoNight,
    tertiary = EnsoGold,
    onTertiary = EnsoNight,
    background = EnsoNight,
    onBackground = EnsoCream,
    surface = EnsoBark,
    onSurface = EnsoCream,
    surfaceVariant = EnsoBarkRaised,
    onSurfaceVariant = EnsoSand,
    outline = EnsoOutline,
    error = EnsoError,
    onError = EnsoNight,
)

@Composable
fun EnsoLegacyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorsDummy else LightColors

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
