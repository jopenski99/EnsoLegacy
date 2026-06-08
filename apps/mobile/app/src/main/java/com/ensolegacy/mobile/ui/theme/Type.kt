package com.ensolegacy.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.ensolegacy.mobile.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

/**
 * Newsreader — editorial serif for display/headline/title styles.
 * Matches the calm, reverent tone of the Ensō Legacy design (spec §3).
 */
private val NewsreaderFont = GoogleFont("Newsreader")
val NewsreaderFamily = FontFamily(
    Font(googleFont = NewsreaderFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = NewsreaderFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = NewsreaderFont, fontProvider = provider, weight = FontWeight.Bold),
)

/**
 * Work Sans — clean, legible sans for body text and labels.
 */
private val WorkSansFont = GoogleFont("Work Sans")
val WorkSansFamily = FontFamily(
    Font(googleFont = WorkSansFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = WorkSansFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = WorkSansFont, fontProvider = provider, weight = FontWeight.SemiBold),
)

private val Base = Typography()

val EnsoTypography = Base.copy(
    displayLarge = Base.displayLarge.copy(fontFamily = NewsreaderFamily),
    displayMedium = Base.displayMedium.copy(fontFamily = NewsreaderFamily),
    displaySmall = Base.displaySmall.copy(fontFamily = NewsreaderFamily),
    headlineLarge = Base.headlineLarge.copy(fontFamily = NewsreaderFamily),
    headlineMedium = Base.headlineMedium.copy(fontFamily = NewsreaderFamily),
    headlineSmall = Base.headlineSmall.copy(fontFamily = NewsreaderFamily),
    titleLarge = Base.titleLarge.copy(fontFamily = NewsreaderFamily, fontWeight = FontWeight.SemiBold),
    titleMedium = Base.titleMedium.copy(fontFamily = WorkSansFamily, fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall.copy(fontFamily = WorkSansFamily, fontWeight = FontWeight.SemiBold),
    bodyLarge = Base.bodyLarge.copy(fontFamily = WorkSansFamily),
    bodyMedium = Base.bodyMedium.copy(fontFamily = WorkSansFamily),
    bodySmall = Base.bodySmall.copy(fontFamily = WorkSansFamily),
    labelLarge = Base.labelLarge.copy(fontFamily = WorkSansFamily),
    labelMedium = Base.labelMedium.copy(fontFamily = WorkSansFamily),
    labelSmall = Base.labelSmall.copy(fontFamily = WorkSansFamily),
)
