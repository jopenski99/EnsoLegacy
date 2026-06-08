package com.ensolegacy.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensolegacy.mobile.domain.HealthStatus
import com.ensolegacy.mobile.ui.theme.CriticalDarkBg
import com.ensolegacy.mobile.ui.theme.CriticalDarkText
import com.ensolegacy.mobile.ui.theme.HealthCriticalBg
import com.ensolegacy.mobile.ui.theme.HealthCriticalText
import com.ensolegacy.mobile.ui.theme.HealthHealthyBg
import com.ensolegacy.mobile.ui.theme.HealthHealthyText
import com.ensolegacy.mobile.ui.theme.HealthNeedsCareBg
import com.ensolegacy.mobile.ui.theme.HealthNeedsCareText
import com.ensolegacy.mobile.ui.theme.HealthyDarkBg
import com.ensolegacy.mobile.ui.theme.HealthyDarkText
import com.ensolegacy.mobile.ui.theme.RecoveringDarkBg
import com.ensolegacy.mobile.ui.theme.RecoveringDarkText

/** Theme-aware (background, text) pair for a health status badge. */
@Composable
fun healthBadgeColors(status: HealthStatus): Pair<Color, Color> {
    val dark = isSystemInDarkTheme()
    return when (status) {
        HealthStatus.HEALTHY ->
            if (dark) HealthyDarkBg to HealthyDarkText else HealthHealthyBg to HealthHealthyText
        HealthStatus.NEEDS_CARE ->
            if (dark) RecoveringDarkBg to RecoveringDarkText else HealthNeedsCareBg to HealthNeedsCareText
        HealthStatus.CRITICAL ->
            if (dark) CriticalDarkBg to CriticalDarkText else HealthCriticalBg to HealthCriticalText
    }
}

/** Solid health color for bar/accent uses (e.g. dashboard breakdown bars). */
@Composable
fun healthColor(status: HealthStatus): Color = healthBadgeColors(status).second

/** Tinted pill showing a tree's current health status. */
@Composable
fun HealthPill(health: HealthStatus, modifier: Modifier = Modifier) {
    val (background, text) = healthBadgeColors(health)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(text = health.label, style = MaterialTheme.typography.labelSmall, color = text)
    }
}
