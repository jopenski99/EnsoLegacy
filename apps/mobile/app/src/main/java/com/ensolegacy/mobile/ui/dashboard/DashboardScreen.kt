package com.ensolegacy.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ensolegacy.mobile.EnsoApp
import com.ensolegacy.mobile.data.local.BonsaiEntity
import com.ensolegacy.mobile.data.local.ReminderWithBonsai
import com.ensolegacy.mobile.domain.BonsaiStage
import com.ensolegacy.mobile.domain.CareType
import com.ensolegacy.mobile.domain.HealthStatus
import com.ensolegacy.mobile.domain.careStatusOf
import com.ensolegacy.mobile.domain.relativeDueLabel
import com.ensolegacy.mobile.ui.collection.AgeBucket
import com.ensolegacy.mobile.ui.collection.CollectionStats
import com.ensolegacy.mobile.ui.collection.CollectionViewModel
import com.ensolegacy.mobile.ui.components.CareStatusPill
import com.ensolegacy.mobile.ui.components.PulsingDot
import com.ensolegacy.mobile.ui.components.healthColor

/**
 * Dashboard tab — a read-only overview of the collection (total count plus an
 * Age / Stage / Health breakdown). Shares [CollectionViewModel] with the
 * collection list; both derive from the same repository flow, so the counts
 * always match what's on the Collection tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onTreeClick: (Long) -> Unit = {},
    viewModel: CollectionViewModel = viewModel(factory = CollectionViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEmpty = uiState.stats.total == 0 && !uiState.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ensō Legacy",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: notifications screen */ }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { /* TODO: profile screen */ },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->
        if (isEmpty) {
            EmptyDashboard(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DashboardCard(stats = uiState.stats)
                NeedsCareSection(reminders = uiState.dueReminders, onTreeClick = onTreeClick)
                SetUpCareSection(trees = uiState.needsSetup, onTreeClick = onTreeClick)
            }
        }
    }
}

@Composable
private fun DashboardCard(stats: CollectionStats) {
    var dimension by rememberSaveable { mutableStateOf(DashboardDimension.HEALTH) }
    // Precomputed here so healthColor (composable) isn't called inside a non-composable lambda below.
    val healthyBarColor = healthColor(HealthStatus.HEALTHY)
    val needsCareBarColor = healthColor(HealthStatus.NEEDS_CARE)
    val criticalBarColor = healthColor(HealthStatus.CRITICAL)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Your collection",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${stats.total}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (stats.total == 1) "tree" else "trees",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }

            DimensionTabs(selected = dimension, onSelect = { dimension = it })

            // Breakdown rows for the selected dimension: (label, count, bar color).
            val rows: List<Triple<String, Int, Color>> = when (dimension) {
                DashboardDimension.AGE ->
                    AgeBucket.entries.mapNotNull { bucket ->
                        stats.byAge[bucket]?.let { Triple(bucket.label, it, MaterialTheme.colorScheme.primary) }
                    }
                DashboardDimension.STAGE ->
                    BonsaiStage.entries.mapNotNull { stage ->
                        stats.byStage[stage]?.let { Triple(stage.label, it, MaterialTheme.colorScheme.primary) }
                    }
                DashboardDimension.HEALTH ->
                    HealthStatus.entries.mapNotNull { status ->
                        val barColor = when (status) {
                            HealthStatus.HEALTHY -> healthyBarColor
                            HealthStatus.NEEDS_CARE -> needsCareBarColor
                            HealthStatus.CRITICAL -> criticalBarColor
                        }
                        stats.byHealth[status]?.let { Triple(status.label, it, barColor) }
                    }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { (label, count, color) ->
                    StatBar(label = label, count = count, total = stats.total, color = color)
                }
            }
        }
    }
}

/**
 * Upcoming/past-due care tasks across the collection, shown as a horizontal
 * scrollable row of reminder cards. Each card shows the task type (emoji +
 * label), the tree name, a relative due date, and a status pill. When every
 * reminder is more than 30 days out and none are overdue, shows an all-clear.
 * The actionable filtering is done in [CollectionViewModel] — this composable
 * receives only overdue / due-soon reminders.
 */
@Composable
private fun NeedsCareSection(reminders: List<ReminderWithBonsai>, onTreeClick: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Needs care",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (reminders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    text = "All your trees are healthy.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            val now = System.currentTimeMillis()
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 0.dp),
            ) {
                items(reminders, key = { it.reminder.id }) { item ->
                    CareReminderCard(item = item, now = now, onClick = { onTreeClick(item.reminder.bonsaiId) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CareReminderCard(item: ReminderWithBonsai, now: Long, onClick: () -> Unit) {
    val type = CareType.fromValue(item.reminder.type)
    val status = careStatusOf(item.reminder.nextDueAt, now)
    Card(
        onClick = onClick,
        modifier = Modifier.width(170.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "${type.emoji} ${type.label}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = item.bonsaiName ?: "(tree deleted)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = relativeDueLabel(item.reminder.nextDueAt, now),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CareStatusPill(status = status)
        }
    }
}

/**
 * Trees with no care schedule yet — a gentle nudge to set one up. Each row
 * carries the spec §1.4 pulsing dot. Omitted entirely when every tree is set up
 * (no "all clear" filler — this is a to-do list, not a status board).
 */
@Composable
private fun SetUpCareSection(trees: List<BonsaiEntity>, onTreeClick: (Long) -> Unit) {
    if (trees.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Set up care reminders",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            trees.forEach { tree ->
                SetUpCareRow(tree = tree, onClick = { onTreeClick(tree.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetUpCareRow(tree: BonsaiEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TreeAvatar(tree = tree, placeholderBg = MaterialTheme.colorScheme.surfaceContainerHigh)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = tree.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Care schedule available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            PulsingDot(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            )
        }
    }
}

/** Small rounded tree thumbnail: the cover photo if set, else a monogram. */
@Composable
private fun TreeAvatar(tree: BonsaiEntity, placeholderBg: Color, size: Dp = 44.dp) {
    val context = LocalContext.current
    val imageStore = remember { (context.applicationContext as EnsoApp).imageStore }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(placeholderBg),
        contentAlignment = Alignment.Center,
    ) {
        val coverPath = tree.coverPhotoPath
        if (coverPath != null) {
            AsyncImage(
                model = imageStore.resolve(coverPath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = tree.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DimensionTabs(selected: DashboardDimension, onSelect: (DashboardDimension) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DashboardDimension.entries.forEach { dim ->
            val isSelected = dim == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(dim) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = dim.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatBar(label: String, count: Int, total: Int, color: Color) {
    val fraction = if (total > 0) count.toFloat() / total else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(color),
                )
            }
        }
    }
}

@Composable
private fun EmptyDashboard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Nothing to show yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add a tree on the Collection tab and its story begins here.",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private enum class DashboardDimension(val label: String) {
    AGE("Age"),
    STAGE("Stage"),
    HEALTH("Health"),
}
