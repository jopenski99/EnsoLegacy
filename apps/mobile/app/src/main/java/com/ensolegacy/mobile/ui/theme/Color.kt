package com.ensolegacy.mobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Ensō Legacy palette.
 *
 * LIGHT is the primary, approved design: warm paper background, olive-green
 * primary, clay-brown accents, espresso text. DARK is a placeholder ("dummy")
 * to be properly designed later.
 */

// --- Light theme (primary / approved) ---------------------------------------
val PaperBackground = Color(0xFFFBF7F1)      // warm cream background
val PaperSurface = Color(0xFFF5EFE4)         // cards / surface
val PaperSurfaceVariant = Color(0xFFECE4D6)  // raised / chips
val PaperOutline = Color(0xFFE2D8C8)         // hairline borders
val MossPrimary = Color(0xFF5F6A2E)          // olive green (primary, titles)
val ClaySecondary = Color(0xFF7A6A4F)        // warm brown accent
val Espresso = Color(0xFF3E3326)             // primary text
val Taupe = Color(0xFF8B7D67)                // muted / secondary text
val BrickError = Color(0xFFA8453A)           // error / critical

// --- Dark theme (DUMMY placeholder — to be redesigned) ----------------------
val EnsoNight = Color(0xFF14110D)
val EnsoBark = Color(0xFF211D17)
val EnsoBarkRaised = Color(0xFF2C261E)
val EnsoOutline = Color(0xFF463E32)
val EnsoOlive = Color(0xFF8C8A46)
val EnsoGold = Color(0xFFC9A24B)
val EnsoCream = Color(0xFFEDE5D6)
val EnsoSand = Color(0xFFA89C86)
val EnsoError = Color(0xFFCF6B5C)

// --- Health status (tuned to read on both themes) ---------------------------
val HealthHealthy = Color(0xFF5E7D3A)
val HealthNeedsCare = Color(0xFFB5852B)
