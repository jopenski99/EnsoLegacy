package com.ensolegacy.mobile.ui.collection

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ensolegacy.mobile.data.local.BonsaiEntity
import com.ensolegacy.mobile.domain.BonsaiStage
import com.ensolegacy.mobile.domain.HealthStatus
import com.ensolegacy.mobile.ui.theme.HealthHealthy
import com.ensolegacy.mobile.ui.theme.HealthNeedsCare

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel = viewModel(factory = CollectionViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    val isEmpty = uiState.collection.isEmpty() && !uiState.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ensō Legacy") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        floatingActionButton = {
            if (!isEmpty) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add tree")
                }
            }
        },
    ) { innerPadding ->
        if (isEmpty) {
            EmptyState(
                onAddFirst = { showAddSheet = true },
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "dashboard") {
                    DashboardHeader(stats = uiState.stats)
                }
                items(uiState.collection, key = { it.id }) { bonsai ->
                    BonsaiCard(bonsai = bonsai, onDelete = { viewModel.removeBonsai(bonsai.id) })
                }
            }
        }
    }

    if (showAddSheet) {
        AddTreeSheet(
            species = viewModel.species,
            onDismiss = { showAddSheet = false },
            onSave = { name, species, stage, health, acquiredYear ->
                viewModel.addBonsai(name, species, stage, health, acquiredYear)
                showAddSheet = false
            },
        )
    }
}

@Composable
private fun BonsaiCard(bonsai: BonsaiEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail placeholder (initial) until tree photos exist.
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = bonsai.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = bonsai.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = bonsai.species,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val stageLabel = BonsaiStage.fromValue(bonsai.stage).label
                val subtitle = bonsai.acquiredYear
                    ?.let { "$stageLabel · since $it" } ?: stageLabel
                Text(text = subtitle, style = MaterialTheme.typography.labelMedium)
                HealthPill(health = HealthStatus.fromValue(bonsai.health))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove ${bonsai.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(stats: CollectionStats) {
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

private enum class DashboardDimension(val label: String) {
    AGE("Age"),
    STAGE("Stage"),
    HEALTH("Health"),
}

@Composable
private fun healthColor(status: HealthStatus): Color = when (status) {
    HealthStatus.HEALTHY -> HealthHealthy
    HealthStatus.NEEDS_CARE -> HealthNeedsCare
    HealthStatus.CRITICAL -> MaterialTheme.colorScheme.error
}

@Composable
private fun HealthPill(health: HealthStatus) {
    val color = healthColor(health)
    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(text = health.label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun EmptyState(onAddFirst: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Ensō ring placeholder until a proper illustration is added.
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "◯", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Your collection is empty",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Every great grove starts with one tree.",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Button(onClick = onAddFirst, modifier = Modifier.fillMaxWidth()) {
            Text("Add your first tree")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { /* TODO: species browser (Browse Catalog) — separate slice */ },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Browse Catalog")
        }
    }
}
