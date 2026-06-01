package com.ensolegacy.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Typography for Ensō Legacy. Display/headline/title styles use a serif face to
 * match the reverent, editorial tone of the design; body and labels stay in the
 * default sans for legibility. (A bundled brand serif can replace FontFamily.Serif later.)
 */
private val Base = Typography()

val EnsoTypography = Base.copy(
    displayLarge = Base.displayLarge.copy(fontFamily = FontFamily.Serif),
    displayMedium = Base.displayMedium.copy(fontFamily = FontFamily.Serif),
    displaySmall = Base.displaySmall.copy(fontFamily = FontFamily.Serif),
    headlineLarge = Base.headlineLarge.copy(fontFamily = FontFamily.Serif),
    headlineMedium = Base.headlineMedium.copy(fontFamily = FontFamily.Serif),
    headlineSmall = Base.headlineSmall.copy(fontFamily = FontFamily.Serif),
    titleLarge = Base.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
)
