package com.ensolegacy.mobile.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ensolegacy.mobile.data.local.ReminderWithBonsai
import com.ensolegacy.mobile.domain.CareType
import com.ensolegacy.mobile.domain.HealthStatus
import com.ensolegacy.mobile.ui.components.healthBadgeColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Schedule tab — all care reminders across the collection, sorted by due date
 * and grouped into Overdue / Due soon / Upcoming buckets. Each row has a "Done"
 * action and a "Change date" link.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = viewModel(factory = ScheduleViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val now = System.currentTimeMillis()

    val overdue = remember(uiState.reminders) { uiState.reminders.filter { it.reminder.nextDueAt < now } }
    val dueSoon = remember(uiState.reminders) {
        uiState.reminders.filter { r ->
            val days = (r.reminder.nextDueAt - now) / (24L * 60 * 60 * 1000)
            days in 0..14
        }
    }
    val upcoming = remember(uiState.reminders) {
        uiState.reminders.filter { r ->
            (r.reminder.nextDueAt - now) / (24L * 60 * 60 * 1000) > 14
        }
    }

    // Theme-aware badge colors computed at composable scope.
    val (overdueBg, overdueText) = healthBadgeColors(HealthStatus.CRITICAL)
    val (dueSoonBg, dueSoonText) = healthBadgeColors(HealthStatus.NEEDS_CARE)
    val (upcomingBg, upcomingText) = healthBadgeColors(HealthStatus.HEALTHY)

    var rescheduleTarget by remember { mutableStateOf<ReminderWithBonsai?>(null) }

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
        if (uiState.reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No care reminders yet. Open a tree and set up its schedule.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (overdue.isNotEmpty()) {
                    item { BucketHeader("Overdue") }
                    items(overdue, key = { it.reminder.id }) { item ->
                        ReminderRow(
                            item = item,
                            onDone = { viewModel.completeTask(item.reminder.id, item.reminder.intervalDays) },
                            onReschedule = { rescheduleTarget = item },
                            statusBg = overdueBg,
                            statusText = overdueText,
                            statusLabel = "Overdue",
                        )
                    }
                }
                if (dueSoon.isNotEmpty()) {
                    item { BucketHeader("Due soon") }
                    items(dueSoon, key = { it.reminder.id }) { item ->
                        ReminderRow(
                            item = item,
                            onDone = { viewModel.completeTask(item.reminder.id, item.reminder.intervalDays) },
                            onReschedule = { rescheduleTarget = item },
                            statusBg = dueSoonBg,
                            statusText = dueSoonText,
                            statusLabel = "Due soon",
                        )
                    }
                }
                if (upcoming.isNotEmpty()) {
                    item { BucketHeader("Upcoming") }
                    items(upcoming, key = { it.reminder.id }) { item ->
                        ReminderRow(
                            item = item,
                            onDone = { viewModel.completeTask(item.reminder.id, item.reminder.intervalDays) },
                            onReschedule = { rescheduleTarget = item },
                            statusBg = upcomingBg,
                            statusText = upcomingText,
                            statusLabel = "On schedule",
                        )
                    }
                }
            }
        }
    }

    rescheduleTarget?.let { target ->
        val dateState = rememberDatePickerState(initialSelectedDateMillis = target.reminder.nextDueAt)
        DatePickerDialog(
            onDismissRequest = { rescheduleTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        viewModel.reschedule(target.reminder.id, it)
                    }
                    rescheduleTarget = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { rescheduleTarget = null }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }
}

@Composable
private fun BucketHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun ReminderRow(
    item: ReminderWithBonsai,
    onDone: () -> Unit,
    onReschedule: () -> Unit,
    statusBg: androidx.compose.ui.graphics.Color,
    statusText: androidx.compose.ui.graphics.Color,
    statusLabel: String,
) {
    val type = CareType.fromValue(item.reminder.type)
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${type.emoji} ${type.label}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = item.bonsaiName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatDate(item.reminder.nextDueAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusText,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = onDone,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) { Text("Done", style = MaterialTheme.typography.labelSmall) }
                    TextButton(
                        onClick = onReschedule,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) { Text("Change date", style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

private fun formatDate(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
