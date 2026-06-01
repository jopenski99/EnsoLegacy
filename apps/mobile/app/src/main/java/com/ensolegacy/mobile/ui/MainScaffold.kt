package com.ensolegacy.mobile.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ensolegacy.mobile.ui.collection.CollectionScreen
import com.ensolegacy.mobile.ui.dashboard.DashboardScreen
import com.ensolegacy.mobile.ui.detail.BonsaiDetailScreen
import com.ensolegacy.mobile.ui.navigation.EnsoDestination
import com.ensolegacy.mobile.ui.schedule.ScheduleScreen
import com.ensolegacy.mobile.ui.settings.SettingsScreen

/**
 * Root of the post-onboarding app: a bottom navigation bar wrapping a NavHost
 * with one destination per [EnsoDestination] tab. Tab switches use a
 * single-top, state-saving pattern so each tab keeps its scroll/selection
 * state and the back stack doesn't pile up duplicate destinations.
 */
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // The bottom bar belongs to the top-level tabs only; pushed screens like the
    // tree detail take over the full screen.
    val onTab = EnsoDestination.entries.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        // Each tab/detail screen owns an inner Scaffold whose TopAppBar already
        // applies the status-bar inset. Zero the outer insets so that inset
        // isn't added a second time, which pushed every title too far down.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!onTab) return@Scaffold

            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                EnsoDestination.entries.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                // Pop back to the graph's start so the back stack
                                // doesn't accumulate one entry per tab tap.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = EnsoDestination.START.route,
            // Reserve space for the bottom bar, and mark those insets consumed so
            // the inner per-screen Scaffolds don't pad for them a second time.
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            val openDetail: (Long) -> Unit = { id -> navController.navigate("bonsai/$id") }

            composable(EnsoDestination.DASHBOARD.route) { DashboardScreen(onTreeClick = openDetail) }
            composable(EnsoDestination.COLLECTION.route) { CollectionScreen(onTreeClick = openDetail) }
            composable(EnsoDestination.SCHEDULE.route) { ScheduleScreen() }
            composable(EnsoDestination.SETTINGS.route) { SettingsScreen() }
            composable(
                route = "bonsai/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                BonsaiDetailScreen(bonsaiId = id, onBack = { navController.popBackStack() })
            }
        }
    }
}
