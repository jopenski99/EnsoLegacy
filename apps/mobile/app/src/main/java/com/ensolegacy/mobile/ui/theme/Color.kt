package com.ensolegacy.mobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Ensō Legacy palette.
 *
 * LIGHT is the primary, approved design: warm paper background, olive-green
 * primary, clay-brown accents, espresso text. DARK is a placeholder ("dummy")
 * to be properly designed later.
 */

// --- Light theme (primary / approved — values per screen-spec §3) -----------
val PaperBackground = Color(0xFFFFF8F5)            // app background
val PaperSurface = Color(0xFFFFF8F5)               // flat surfaces (bonsai cards, nav bar)
val PaperSurfaceContainer = Color(0xFFFCEBE1)      // elevated cards
val PaperSurfaceContainerHigh = Color(0xFFF6E5DB)  // hero / thumbnail placeholder
val MossPrimary = Color(0xFF5D5F1B)                // moss green (primary)
val MossPrimaryContainer = Color(0xFF767831)       // active nav, filled buttons
val ClaySecondary = Color(0xFF6E5B41)              // warm brown accent / secondary text
val ClaySecondaryContainer = Color(0xFFF9DFBD)     // chips, photo badges
val Espresso = Color(0xFF221A14)                   // primary text
val Taupe = Color(0xFF48473A)                      // secondary text / labels
val PaperOutline = Color(0xFF797868)               // borders
val PaperOutlineVariant = Color(0xFFC9C7B5)        // subtle borders, dividers
val BrickError = Color(0xFFBA1A1A)                 // error / critical

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

// --- Health badge colors (spec §3: light bg + dark text pairs) --------------
val HealthHealthyBg = Color(0xFFDCFCE7)
val HealthHealthyText = Color(0xFF166534)
val HealthNeedsCareBg = Color(0xFFFEF9C3)
val HealthNeedsCareText = Color(0xFF854D0E)
val HealthCriticalBg = Color(0xFFFEE2E2)
val HealthCriticalText = Color(0xFF991B1B)
