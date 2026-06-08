package com.ensolegacy.mobile.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensolegacy.mobile.data.local.BonsaiEntity
import com.ensolegacy.mobile.domain.AcquisitionSource
import com.ensolegacy.mobile.domain.BonsaiStage
import com.ensolegacy.mobile.domain.HealthStatus
import com.ensolegacy.mobile.domain.Placement
import com.ensolegacy.mobile.domain.Species
import com.ensolegacy.mobile.domain.TreeFormData
import java.time.Year

/**
 * Bottom-sheet form for adding *or editing* a tree. Species is chosen from the
 * bundled starter catalog; stage, health, acquisition source, and placement are
 * chip/dropdown selectors. Provenance (origin, acquiredFrom) are optional text
 * fields. Pass [initial] to pre-fill the form for editing — when null it's a
 * blank "add" form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTreeSheet(
    species: List<Species>,
    onDismiss: () -> Unit,
    onSave: (TreeFormData) -> Unit,
    initial: BonsaiEntity? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = initial != null

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var selectedSpecies by remember {
        mutableStateOf(
            initial?.let { init -> species.firstOrNull { it.commonName.equals(init.species, ignoreCase = true) } },
        )
    }
    var selectedStage by remember {
        mutableStateOf(initial?.let { BonsaiStage.fromValue(it.stage) } ?: BonsaiStage.JUVENILE)
    }
    var selectedHealth by remember {
        mutableStateOf(initial?.let { HealthStatus.fromValue(it.health) } ?: HealthStatus.HEALTHY)
    }
    var selectedYear by remember { mutableStateOf(initial?.acquiredYear) }
    var selectedSource by remember {
        mutableStateOf(AcquisitionSource.fromValue(initial?.acquisitionSource))
    }
    var selectedPlacement by remember {
        mutableStateOf(Placement.fromValue(initial?.placement))
    }
    var origin by remember { mutableStateOf(initial?.origin ?: "") }
    var acquiredFrom by remember { mutableStateOf(initial?.acquiredFrom ?: "") }

    val canSave = name.isNotBlank() && selectedSpecies != null

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (isEdit) "Edit tree" else "Add a tree",
                style = MaterialTheme.typography.titleLarge,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            SpeciesPicker(
                species = species,
                selected = selectedSpecies,
                onSelected = { selectedSpecies = it },
            )

            StagePicker(selected = selectedStage, onSelected = { selectedStage = it })

            HealthPicker(selected = selectedHealth, onSelected = { selectedHealth = it })

            YearPicker(selected = selectedYear, onSelected = { selectedYear = it })

            // Acquisition context (item 9)
            ChipGroupLabel("How was this tree acquired? (optional)")
            AcquisitionSourceChips(selected = selectedSource, onSelected = { selectedSource = it })

            ChipGroupLabel("Where does it live? (optional)")
            PlacementChips(selected = selectedPlacement, onSelected = { selectedPlacement = it })

            // Provenance (item 8)
            OutlinedTextField(
                value = origin,
                onValueChange = { origin = it },
                label = { Text("Origin (optional)") },
                placeholder = { Text("e.g. Collected from Mt. Apo, 2018") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = acquiredFrom,
                onValueChange = { acquiredFrom = it },
                label = { Text("Acquired from (optional)") },
                placeholder = { Text("e.g. Davao Bonsai Society exhibit") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    onSave(
                        TreeFormData(
                            name = name,
                            species = selectedSpecies!!.commonName,
                            stage = selectedStage,
                            health = selectedHealth,
                            acquiredYear = selectedYear,
                            acquisitionSource = selectedSource,
                            placement = selectedPlacement,
                            origin = origin.ifBlank { null },
                            acquiredFrom = acquiredFrom.ifBlank { null },
                        ),
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isEdit) "Save changes" else "Save")
            }
        }
    }
}

@Composable
private fun ChipGroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AcquisitionSourceChips(
    selected: AcquisitionSource?,
    onSelected: (AcquisitionSource?) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AcquisitionSource.entries.forEach { src ->
            FilterChip(
                selected = selected == src,
                onClick = { onSelected(if (selected == src) null else src) },
                label = { Text(src.label) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlacementChips(
    selected: Placement?,
    onSelected: (Placement?) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Placement.entries.forEach { p ->
            FilterChip(
                selected = selected == p,
                onClick = { onSelected(if (selected == p) null else p) },
                label = { Text(p.label) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeciesPicker(
    species: List<Species>,
    selected: Species?,
    onSelected: (Species) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.commonName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Species") },
            placeholder = { Text("Choose a species") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            species.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(option.commonName)
                            Text(
                                text = option.scientificName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StagePicker(selected: BonsaiStage, onSelected: (BonsaiStage) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Stage") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BonsaiStage.entries.forEach { stage ->
                DropdownMenuItem(
                    text = { Text(stage.label) },
                    onClick = {
                        onSelected(stage)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthPicker(selected: HealthStatus, onSelected: (HealthStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Health") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            HealthStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label) },
                    onClick = {
                        onSelected(status)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearPicker(selected: Int?, onSelected: (Int?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val currentYear = remember { Year.now().value }
    val years = remember(currentYear) { (currentYear downTo currentYear - 80).toList() }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.toString() ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Acquired year (optional)") },
            placeholder = { Text("Choose a year") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Not specified") },
                onClick = {
                    onSelected(null)
                    expanded = false
                },
            )
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onSelected(year)
                        expanded = false
                    },
                )
            }
        }
    }
}
