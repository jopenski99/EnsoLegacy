package com.ensolegacy.mobile.ui.theme

import androidx.compose.ui.graphics.Color


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

// --- Dark theme palette ------------------------------------------------------
// Backgrounds
val NightBackground = Color(0xFF111714)
val NightSurface = Color(0xFF1A1F1C)
val NightSurfaceHigh = Color(0xFF222922)

// Primary
val MossLight = Color(0xFFA8C572)
val MossDim = Color(0xFF7A9A4A)
val OnMoss = Color(0xFF0D1A0D)

// Text
val CoolWhite = Color(0xFFE2E8E0)
val MutedGreenGrey = Color(0xFF9AA89C)

// Borders
val NightOutline = Color(0xFF3D4A3F)
val NightOutlineVar = Color(0xFF2A342C)

// Chips
val NightChipBg = Color(0xFF2A3A2C)
val NightChipText = Color(0xFFA8C572)

// Health badges (dark)
val HealthyDarkBg = Color(0xFF1A2E1C)
val HealthyDarkText = Color(0xFF86C98A)
val RecoveringDarkBg = Color(0xFF2E2A14)
val RecoveringDarkText = Color(0xFFC9B86A)
val CriticalDarkBg = Color(0xFF2E1414)
val CriticalDarkText = Color(0xFFC96A6A)

// Alert banner (dark)
val AlertDarkBg = Color(0xFF1A2420)
val AlertDarkBorder = Color(0xFFA8C572)

// --- Health badge colors (light theme — spec §3: light bg + dark text pairs) -
val HealthHealthyBg = Color(0xFFDCFCE7)
val HealthHealthyText = Color(0xFF166534)
val HealthNeedsCareBg = Color(0xFFFEF9C3)
val HealthNeedsCareText = Color(0xFF854D0E)
val HealthCriticalBg = Color(0xFFFEE2E2)
val HealthCriticalText = Color(0xFF991B1B)
