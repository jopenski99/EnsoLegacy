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
import androidx.compose.ui.unit.dp
import com.ensolegacy.mobile.domain.CareStatus

/** Pill showing a care task's due status (On schedule / Due soon / Overdue). */
@Composable
fun CareStatusPill(status: CareStatus, modifier: Modifier = Modifier) {
    val (background, text) = healthBadgeColors(status.toHealthStatus())
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(text = status.label, style = MaterialTheme.typography.labelSmall, color = text)
    }
}

private fun CareStatus.toHealthStatus() = when (this) {
    CareStatus.ON_SCHEDULE -> com.ensolegacy.mobile.domain.HealthStatus.HEALTHY
    CareStatus.DUE_SOON -> com.ensolegacy.mobile.domain.HealthStatus.NEEDS_CARE
    CareStatus.OVERDUE -> com.ensolegacy.mobile.domain.HealthStatus.CRITICAL
}
