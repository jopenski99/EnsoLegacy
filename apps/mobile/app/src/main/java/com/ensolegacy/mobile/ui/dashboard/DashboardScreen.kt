package com.ensolegacy.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ensolegacy.mobile.data.local.BonsaiEntity
import com.ensolegacy.mobile.domain.BonsaiStage
import com.ensolegacy.mobile.domain.HealthStatus
import com.ensolegacy.mobile.ui.collection.AgeBucket
import com.ensolegacy.mobile.ui.collection.CollectionStats
import com.ensolegacy.mobile.ui.collection.CollectionViewModel
import com.ensolegacy.mobile.ui.components.HealthPill
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
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
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
                NeedsCareSection(trees = uiState.needsCare, onTreeClick = onTreeClick)
                SetUpCareSection(trees = uiState.needsSetup, onTreeClick = onTreeClick)
            }
        }
    }
}

@Composable
private fun DashboardCard(stats: CollectionStats) {
    var dimension by rememberSaveable { mutableStateOf(DashboardDimension.HEALTH) }
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
                        stats.byHealth[status]?.let { Triple(status.label, it, healthColor(status)) }
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
 * Trees that need care, worst first. Driven by health today; the spec's other
 * signals (overdue reminders, unset care schedule, undocumented trees) fold in
 * here as those features ship. When nothing needs care, shows a calm
 * all-clear rather than empty space.
 */
@Composable
private fun NeedsCareSection(trees: List<BonsaiEntity>, onTreeClick: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Needs care",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (trees.isEmpty()) {
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                trees.forEach { tree ->
                    NeedsCareRow(tree = tree, onClick = { onTreeClick(tree.id) })
                }
            }
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tree.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NeedsCareRow(tree: BonsaiEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tree.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tree.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = tree.species,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HealthPill(health = HealthStatus.fromValue(tree.health))
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
