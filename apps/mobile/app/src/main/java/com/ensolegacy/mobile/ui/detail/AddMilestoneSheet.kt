package com.ensolegacy.mobile.ui.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ensolegacy.mobile.EnsoApp
import com.ensolegacy.mobile.domain.MAX_MILESTONE_PHOTOS
import com.ensolegacy.mobile.ui.camera.CameraCaptureDialog
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * "Add milestone" flow (spec §2.7) — title, optional note, the date it happened,
 * and the up-to-[MAX_MILESTONE_PHOTOS] photo flow. Photos are captured/imported
 * into storage as they're added (so a thumbnail can show immediately); their
 * paths are staged and only linked to a milestone row on Save. Cancelling
 * discards the staged files so nothing is orphaned.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMilestoneSheet(
    onSubmit: (title: String, notes: String?, occurredAt: Long, photoPaths: List<String>) -> Unit,
    onDiscard: (photoPaths: List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val imageStore = remember { (context.applicationContext as EnsoApp).imageStore }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var occurredAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var staged by remember { mutableStateOf(emptyList<String>()) }
    var showCamera by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val atMax = staged.size >= MAX_MILESTONE_PHOTOS

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val path = imageStore.importFromUri(uri)
                if (path != null) staged = staged + path
            }
        }
    }

    ModalBottomSheet(onDismissRequest = { onDiscard(staged); onDismiss() }, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Add milestone", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. First major styling") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("When", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatDay(occurredAt), style = MaterialTheme.typography.bodyLarge)
                }
                TextButton(onClick = { showDatePicker = true }) { Text("Change") }
            }

            // Photo flow.
            Text(
                text = "Photos ${staged.size}/$MAX_MILESTONE_PHOTOS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (staged.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(staged, key = { it }) { path ->
                        Box {
                            AsyncImage(
                                model = imageStore.resolve(path),
                                contentDescription = "Staged photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                                    .clickable {
                                        imageStore.delete(path)
                                        staged = staged - path
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove photo",
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showCamera = true },
                    enabled = !atMax,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Camera")
                }
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    enabled = !atMax,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Gallery")
                }
            }

            Button(
                onClick = {
                    onSubmit(title, notes, occurredAt, staged)
                    onDismiss()
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save milestone")
            }
            TextButton(
                onClick = { onDiscard(staged); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cancel")
            }
        }
    }

    if (showCamera) {
        CameraCaptureDialog(
            onCaptured = { path ->
                staged = staged + path
                showCamera = false
            },
            onDismiss = { showCamera = false },
        )
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = occurredAt)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { occurredAt = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }
}

private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

private fun formatDay(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(dayFormatter)
