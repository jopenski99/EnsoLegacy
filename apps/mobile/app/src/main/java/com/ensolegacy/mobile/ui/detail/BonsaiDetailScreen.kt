package com.ensolegacy.mobile.ui.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ensolegacy.mobile.EnsoApp
import com.ensolegacy.mobile.data.local.BonsaiEntity
import com.ensolegacy.mobile.data.local.CareReminderEntity
import com.ensolegacy.mobile.data.local.PhotoEntity
import com.ensolegacy.mobile.data.local.StageTransitionEntity
import com.ensolegacy.mobile.domain.AcquisitionSource
import com.ensolegacy.mobile.domain.BonsaiStage
import com.ensolegacy.mobile.domain.CareDefault
import com.ensolegacy.mobile.domain.CareType
import com.ensolegacy.mobile.domain.HealthStatus
import com.ensolegacy.mobile.domain.careStatusOf
import com.ensolegacy.mobile.ui.components.CareStatusPill
import com.ensolegacy.mobile.domain.Placement
import com.ensolegacy.mobile.ui.camera.CameraCaptureDialog
import com.ensolegacy.mobile.ui.collection.AddTreeSheet
import com.ensolegacy.mobile.ui.components.HealthPill
import java.time.Instant
import java.time.Year
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The living record of a single tree. This is the "shell" build: the sections
 * backed by data that exists today (hero placeholder, identity, status, quick
 * stats) are real; the sections that depend on features not yet built
 * (milestones, photo vault, care reminders, stage history, provenance) are
 * shown as calm "coming soon" cards so the full structure from the spec is
 * visible and ready to fill in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BonsaiDetailScreen(
    bonsaiId: Long,
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel(factory = DetailViewModel.provideFactory(bonsaiId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showSetup by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    val bonsai = uiState.bonsai

    // Photo capture state. A "target" says where a captured/picked photo goes;
    // the source chooser, in-app camera, and gallery picker all route by it.
    var chooserTarget by remember { mutableStateOf<CaptureTarget?>(null) }
    var cameraTarget by remember { mutableStateOf<CaptureTarget?>(null) }
    var galleryTarget by remember { mutableStateOf<CaptureTarget?>(null) }
    var showAddMilestone by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val target = galleryTarget
        galleryTarget = null
        if (uri != null) when (target) {
            CaptureTarget.COVER -> viewModel.setCoverPhotoFromGallery(uri)
            CaptureTarget.VAULT -> viewModel.addVaultPhotoFromGallery(uri)
            null -> Unit
        }
    }

    Scaffold(
        floatingActionButton = {
            if (bonsai != null) {
                FloatingActionButton(
                    onClick = { showSetup = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Set up care reminders")
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(bonsai?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (bonsai != null) {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            // Edit reuses the add-tree form, pre-filled.
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    menuOpen = false
                                    showEdit = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    menuOpen = false
                                    confirmDelete = true
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            bonsai == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "This tree is no longer in your collection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> DetailContent(
                bonsai = bonsai,
                speciesKnown = uiState.speciesKnown,
                reminders = uiState.reminders,
                timeline = uiState.timeline,
                vaultPhotos = uiState.vaultPhotos,
                stageHistory = uiState.stageHistory,
                onUseDefault = viewModel::useDefaultSchedule,
                onSetMyOwn = { showSetup = true },
                onEditCover = { chooserTarget = CaptureTarget.COVER },
                onCompleteReminder = viewModel::completeReminder,
                onRescheduleReminder = viewModel::rescheduleReminder,
                onAddMilestone = { showAddMilestone = true },
                onDeleteMilestone = viewModel::deleteMilestone,
                onAddVaultPhoto = { chooserTarget = CaptureTarget.VAULT },
                onDeleteVaultPhoto = viewModel::deleteVaultPhoto,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    chooserTarget?.let { target ->
        MediaSourceSheet(
            onCamera = {
                chooserTarget = null
                cameraTarget = target
            },
            onGallery = {
                chooserTarget = null
                galleryTarget = target
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onDismiss = { chooserTarget = null },
        )
    }

    cameraTarget?.let { target ->
        CameraCaptureDialog(
            onCaptured = { path ->
                when (target) {
                    CaptureTarget.COVER -> viewModel.setCoverPhoto(path)
                    CaptureTarget.VAULT -> viewModel.addVaultPhoto(path)
                }
                cameraTarget = null
            },
            onDismiss = { cameraTarget = null },
        )
    }

    if (showAddMilestone && bonsai != null) {
        AddMilestoneSheet(
            onSubmit = { title, notes, occurredAt, photoPaths ->
                viewModel.addMilestone(title, notes, occurredAt, photoPaths)
            },
            onDiscard = viewModel::discardPhotos,
            onDismiss = { showAddMilestone = false },
        )
    }

    if (showEdit && bonsai != null) {
        AddTreeSheet(
            species = viewModel.species,
            initial = bonsai,
            onDismiss = { showEdit = false },
            onSave = { data ->
                viewModel.updateDetails(data)
                showEdit = false
            },
        )
    }

    if (showSetup && bonsai != null) {
        CareSetupSheet(
            speciesName = bonsai.species,
            speciesKnown = uiState.speciesKnown,
            defaults = uiState.careDefaults,
            onDismiss = { showSetup = false },
            onSave = { chosen ->
                viewModel.saveReminders(chosen)
                showSetup = false
            },
        )
    }

    if (confirmDelete && bonsai != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Remove ${bonsai.name}?") },
            text = { Text("This permanently deletes the tree and its record from your collection.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                    onBack()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun DetailContent(
    bonsai: BonsaiEntity,
    speciesKnown: Boolean,
    reminders: List<CareReminderEntity>,
    timeline: List<MilestoneUi>,
    vaultPhotos: List<PhotoEntity>,
    stageHistory: List<StageTransitionEntity>,
    onUseDefault: () -> Unit,
    onSetMyOwn: () -> Unit,
    onEditCover: () -> Unit,
    onCompleteReminder: (id: Long, intervalDays: Int) -> Unit,
    onRescheduleReminder: (id: Long, newDueAt: Long) -> Unit,
    onAddMilestone: () -> Unit,
    onDeleteMilestone: (Long) -> Unit,
    onAddVaultPhoto: () -> Unit,
    onDeleteVaultPhoto: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Hero(bonsai = bonsai, onEditCover = onEditCover)
        Identity(bonsai = bonsai)
        StatusRow(bonsai = bonsai)
        QuickStats(bonsai = bonsai)

        CareRemindersSection(
            reminders = reminders,
            speciesKnown = speciesKnown,
            speciesName = bonsai.species,
            onUseDefault = onUseDefault,
            onSetMyOwn = onSetMyOwn,
            onComplete = onCompleteReminder,
            onReschedule = onRescheduleReminder,
        )

        TimelineSection(
            timeline = timeline,
            onAddMilestone = onAddMilestone,
            onDeleteMilestone = onDeleteMilestone,
        )

        PhotoVaultSection(
            photos = vaultPhotos,
            onAddPhoto = onAddVaultPhoto,
            onDeletePhoto = onDeleteVaultPhoto,
        )

        StageHistorySection(history = stageHistory)

        ProvenanceSection(bonsai = bonsai)

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Hero(bonsai: BonsaiEntity, onEditCover: () -> Unit) {
    val context = LocalContext.current
    val imageStore = remember { (context.applicationContext as EnsoApp).imageStore }
    val coverPath = bonsai.coverPhotoPath

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onEditCover),
        contentAlignment = Alignment.Center,
    ) {
        if (coverPath != null) {
            AsyncImage(
                model = imageStore.resolve(coverPath),
                contentDescription = "${bonsai.name} cover photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Calm monogram placeholder until a cover photo is set.
            Text(
                text = bonsai.name.take(1).uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        // Camera affordance, bottom-right.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
                .clickable(onClick = onEditCover),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.CameraAlt,
                contentDescription = if (coverPath == null) "Add cover photo" else "Change cover photo",
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Identity(bonsai: BonsaiEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = bonsai.name, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = bonsai.species,
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val source = AcquisitionSource.fromValue(bonsai.acquisitionSource)
        val placement = Placement.fromValue(bonsai.placement)
        if (source != null || placement != null) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (source != null) AcquisitionChip(source.label)
                if (placement != null) AcquisitionChip(placement.label)
            }
        }
    }
}

@Composable
private fun StatusRow(bonsai: BonsaiEntity) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusColumn(label = "Age") {
            Text(text = ageText(bonsai.acquiredYear), style = MaterialTheme.typography.titleMedium)
        }
        StatusColumn(label = "Stage") {
            Text(
                text = BonsaiStage.fromValue(bonsai.stage).label,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        StatusColumn(label = "Health") {
            HealthPill(health = HealthStatus.fromValue(bonsai.health))
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun StatusColumn(label: String, value: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        value()
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuickStats(bonsai: BonsaiEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatLine(
                label = "Date established",
                value = bonsai.acquiredYear?.toString() ?: "Not recorded",
            )
            StatDivider()
            StatLine(label = "Species", value = bonsai.species)
            StatDivider()
            StatLine(label = "Stage", value = BonsaiStage.fromValue(bonsai.stage).label)
            StatDivider()
            StatLine(label = "Added", value = formatDate(bonsai.createdAt))
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StatDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/**
 * Care Reminders section (spec §2.5/§2.6). Once reminders exist they show as
 * cards with their next-due status. Otherwise it's a *non-forced* inline prompt
 * — not a blocking modal — offering to set them up: "We know this species" copy
 * appears when the tree's species is in our catalog. "Use Default Schedule"
 * persists the species defaults; "Set My Own" opens the setup sheet (the FAB
 * does too); "Later" dismisses without setting anything.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CareRemindersSection(
    reminders: List<CareReminderEntity>,
    speciesKnown: Boolean,
    speciesName: String,
    onUseDefault: () -> Unit,
    onSetMyOwn: () -> Unit,
    onComplete: (id: Long, intervalDays: Int) -> Unit,
    onReschedule: (id: Long, newDueAt: Long) -> Unit,
) {
    var dismissed by rememberSaveable { mutableStateOf(false) }
    SectionScaffold(title = "Care reminders") {
        when {
            reminders.isNotEmpty() -> ReminderCards(
                reminders = reminders,
                onComplete = onComplete,
                onReschedule = onReschedule,
            )

            dismissed -> SectionNote(
                "No care reminders set. Tap the schedule button to set them up.",
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (speciesKnown) "We know this species." else "Set up care reminders",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = if (speciesKnown) {
                        "Want to set up care reminders for your $speciesName?"
                    } else {
                        "Let's set up your reminders for this bonsai."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onUseDefault, modifier = Modifier.fillMaxWidth()) {
                    Text("Use Default Schedule")
                }
                OutlinedButton(onClick = onSetMyOwn, modifier = Modifier.fillMaxWidth()) {
                    Text("Set My Own")
                }
                TextButton(onClick = { dismissed = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Later")
                }
            }
        }
    }
}

/** Horizontally scrolling reminder cards (spec §2.5): type, next due, status + Done/Reschedule. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderCards(
    reminders: List<CareReminderEntity>,
    onComplete: (id: Long, intervalDays: Int) -> Unit,
    onReschedule: (id: Long, newDueAt: Long) -> Unit,
) {
    val now = System.currentTimeMillis()
    var rescheduleTarget by remember { mutableStateOf<CareReminderEntity?>(null) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(reminders, key = { it.id }) { reminder ->
            val type = CareType.fromValue(reminder.type)
            val status = careStatusOf(reminder.nextDueAt, now)
            Card(
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
                        text = "Next: ${formatDate(reminder.nextDueAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    CareStatusPill(status = status)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { onComplete(reminder.id, reminder.intervalDays) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.weight(1f),
                        ) { Text("Done", style = MaterialTheme.typography.labelSmall) }
                        TextButton(
                            onClick = { rescheduleTarget = reminder },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.weight(1f),
                        ) { Text("Change date", style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }

    rescheduleTarget?.let { target ->
        val dateState = rememberDatePickerState(initialSelectedDateMillis = target.nextDueAt)
        DatePickerDialog(
            onDismissRequest = { rescheduleTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { onReschedule(target.id, it) }
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

/**
 * Spec §2.6 "Set up care reminders" bottom sheet. Lists the species' default
 * tasks, each toggleable, pre-filled with its cadence and a computed first-due
 * date. Per-task date editing ("Change date") and notifications are later slices.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CareSetupSheet(
    speciesName: String,
    speciesKnown: Boolean,
    defaults: List<CareDefault>,
    onDismiss: () -> Unit,
    onSave: (List<CareDefault>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Every default starts enabled; the owner can switch any off before saving.
    val enabled = remember(defaults) {
        mutableStateOf(defaults.map { it.type }.toSet())
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Set up care reminders", style = MaterialTheme.typography.titleLarge)
            Text(
                text = if (speciesKnown) "Defaults for your $speciesName." else "Choose reminders for this bonsai.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            defaults.forEach { default ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${default.type.emoji} ${default.type.label}",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = default.cadenceLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = default.type in enabled.value,
                        onCheckedChange = { on ->
                            enabled.value = if (on) {
                                enabled.value + default.type
                            } else {
                                enabled.value - default.type
                            }
                        },
                    )
                }
            }

            Button(
                onClick = { onSave(defaults.filter { it.type in enabled.value }) },
                enabled = enabled.value.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save reminders")
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Later")
            }
        }
    }
}

/** Where a captured or picked photo should go. */
private enum class CaptureTarget { COVER, VAULT }

/**
 * Timeline (spec §2.7) — the tree's milestones, newest first, each with its
 * photos. The heart of the Tree Passport. "Add milestone" opens the capture flow.
 */
@Composable
private fun TimelineSection(
    timeline: List<MilestoneUi>,
    onAddMilestone: () -> Unit,
    onDeleteMilestone: (Long) -> Unit,
) {
    SectionScaffold(title = "Timeline") {
        if (timeline.isEmpty()) {
            SectionNote("No milestones yet. Document this tree's story as it unfolds.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                timeline.forEach { item ->
                    MilestoneCard(item = item, onDelete = { onDeleteMilestone(item.milestone.id) })
                }
            }
        }
        OutlinedButton(onClick = onAddMilestone, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("  Add milestone")
        }
    }
}

@Composable
private fun MilestoneCard(item: MilestoneUi, onDelete: () -> Unit) {
    val context = LocalContext.current
    val imageStore = remember { (context.applicationContext as EnsoApp).imageStore }
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.milestone.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = formatDate(item.milestone.occurredAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Milestone options")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        }
        item.milestone.notes?.let { notes ->
            Text(text = notes, style = MaterialTheme.typography.bodyMedium)
        }
        if (item.photos.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(item.photos, key = { it.id }) { photo ->
                    AsyncImage(
                        model = imageStore.resolve(photo.path),
                        contentDescription = "Milestone photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    )
                }
            }
        }
    }
}

/** Photo vault (spec §2.8) — loose diary photos for the tree, beyond milestones. */
@Composable
private fun PhotoVaultSection(
    photos: List<PhotoEntity>,
    onAddPhoto: () -> Unit,
    onDeletePhoto: (Long) -> Unit,
) {
    val context = LocalContext.current
    val imageStore = remember { (context.applicationContext as EnsoApp).imageStore }

    SectionScaffold(title = "Photo vault") {
        if (photos.isEmpty()) {
            SectionNote("A visual diary of this tree, beyond care moments.")
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos, key = { it.id }) { photo ->
                    Box {
                        AsyncImage(
                            model = imageStore.resolve(photo.path),
                            contentDescription = "Vault photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(22.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f))
                                .clickable { onDeletePhoto(photo.id) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = onAddPhoto, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("  Add photo")
        }
    }
}

/** "Take photo / Choose from gallery" chooser for cover & vault photos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaSourceSheet(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            ListItem(
                headlineContent = { Text("Take photo") },
                leadingContent = { Icon(Icons.Outlined.CameraAlt, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onCamera),
            )
            ListItem(
                headlineContent = { Text("Choose from gallery") },
                leadingContent = { Icon(Icons.Outlined.PhotoLibrary, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onGallery),
            )
        }
    }
}

@Composable
private fun ComingSoonSection(title: String, note: String) {
    SectionScaffold(title = title) { SectionNote(note) }
}

/** Section header + content slot, shared by the detail sections. */
@Composable
private fun SectionScaffold(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

/** Muted, italic note inside a soft surface box — the "coming soon" filler. */
@Composable
private fun SectionNote(note: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Text(
            text = note,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AcquisitionChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/** Stage history section — records every life-stage change the owner saved. */
@Composable
private fun StageHistorySection(history: List<StageTransitionEntity>) {
    SectionScaffold(title = "Stage history") {
        if (history.isEmpty()) {
            SectionNote("Stage changes will appear here as this tree grows.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                history.forEachIndexed { i, t ->
                    val from = BonsaiStage.fromValue(t.fromStage).label
                    val to = BonsaiStage.fromValue(t.toStage).label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "$from → $to",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = formatDate(t.recordedAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (i < history.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

/** Provenance section — where this tree came from and who the owner got it from. */
@Composable
private fun ProvenanceSection(bonsai: BonsaiEntity) {
    val hasProvenance = !bonsai.origin.isNullOrBlank() || !bonsai.acquiredFrom.isNullOrBlank()
    SectionScaffold(title = "Provenance") {
        if (!hasProvenance) {
            SectionNote("Add this tree's origin story via the edit menu.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                if (!bonsai.origin.isNullOrBlank()) {
                    ProvenanceLine(label = "Origin", value = bonsai.origin)
                }
                if (!bonsai.origin.isNullOrBlank() && !bonsai.acquiredFrom.isNullOrBlank()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                if (!bonsai.acquiredFrom.isNullOrBlank()) {
                    ProvenanceLine(label = "Acquired from", value = bonsai.acquiredFrom)
                }
            }
        }
    }
}

@Composable
private fun ProvenanceLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

/** "~N yrs" from the acquired year, or an em dash when unknown. */
private fun ageText(acquiredYear: Int?): String {
    if (acquiredYear == null) return "—"
    val years = Year.now().value - acquiredYear
    return if (years <= 0) "<1 yr" else "~$years yr${if (years == 1) "" else "s"}"
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

private fun formatDate(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
