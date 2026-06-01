package com.ensolegacy.mobile.ui.components

import androidx.compose.foundation.background
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
import com.ensolegacy.mobile.ui.theme.HealthCriticalBg
import com.ensolegacy.mobile.ui.theme.HealthCriticalText
import com.ensolegacy.mobile.ui.theme.HealthHealthyBg
import com.ensolegacy.mobile.ui.theme.HealthHealthyText
import com.ensolegacy.mobile.ui.theme.HealthNeedsCareBg
import com.ensolegacy.mobile.ui.theme.HealthNeedsCareText

/** Spec §3 health-badge colors: a (background, text) pair per status. */
private fun healthBadgeColors(status: HealthStatus): Pair<Color, Color> = when (status) {
    HealthStatus.HEALTHY -> HealthHealthyBg to HealthHealthyText
    HealthStatus.NEEDS_CARE -> HealthNeedsCareBg to HealthNeedsCareText
    HealthStatus.CRITICAL -> HealthCriticalBg to HealthCriticalText
}

/**
 * Solid health color for non-badge uses (e.g. the dashboard breakdown bars).
 * Uses the badge's darker text tone so it reads on the paper surface.
 */
fun healthColor(status: HealthStatus): Color = healthBadgeColors(status).second

/** Tinted pill showing a tree's current health status (spec health badge). */
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
