package com.ensolegacy.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The four top-level tabs of the app's bottom navigation. Order here is the
 * order they appear in the bar. Each maps to a single [route] used by the
 * NavHost and a [label] / [icon] shown in the bar.
 */
enum class EnsoDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Home),
    COLLECTION("collection", "Collection", Icons.AutoMirrored.Filled.List),
    SCHEDULE("schedule", "Schedule", Icons.Default.DateRange),
    SETTINGS("settings", "Settings", Icons.Default.Settings);

    companion object {
        /** Tab the app opens on. */
        val START = DASHBOARD
    }
}
