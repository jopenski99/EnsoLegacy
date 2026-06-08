package com.ensolegacy.mobile.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import android.net.Uri
import com.ensolegacy.mobile.EnsoApp
import com.ensolegacy.mobile.data.ImageStore
import com.ensolegacy.mobile.data.SpeciesCatalog
import com.ensolegacy.mobile.data.local.BonsaiEntity
import com.ensolegacy.mobile.data.local.CareReminderEntity
import com.ensolegacy.mobile.data.local.MilestoneEntity
import com.ensolegacy.mobile.data.local.PhotoEntity
import com.ensolegacy.mobile.data.local.StageTransitionEntity
import com.ensolegacy.mobile.data.repository.BonsaiRepository
import com.ensolegacy.mobile.data.repository.CareReminderRepository
import com.ensolegacy.mobile.data.repository.MilestoneRepository
import com.ensolegacy.mobile.data.repository.StageTransitionRepository
import com.ensolegacy.mobile.domain.BonsaiStage
import com.ensolegacy.mobile.domain.CareDefault
import com.ensolegacy.mobile.domain.CareDefaults
import com.ensolegacy.mobile.domain.HealthStatus
import com.ensolegacy.mobile.domain.Species
import com.ensolegacy.mobile.domain.TreeFormData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for a single tree's detail view. Observes the tree, its care
 * reminders, milestones, photos, and stage-transition history; derives the
 * species' default schedule; and routes user intents to the repositories.
 */
class DetailViewModel(
    private val bonsaiId: Long,
    private val repository: BonsaiRepository,
    private val careReminders: CareReminderRepository,
    private val milestones: MilestoneRepository,
    private val stageTransitions: StageTransitionRepository,
    private val imageStore: ImageStore,
    speciesCatalog: SpeciesCatalog,
) : ViewModel() {

    /** Starter catalog backing the edit form's species picker. */
    val species: List<Species> = speciesCatalog.all

    /** Catalog species keyed by lower-cased common name (how the tree stores it). */
    private val speciesByName: Map<String, Species> =
        species.associateBy { it.commonName.lowercase() }

    val uiState: StateFlow<DetailUiState> =
        combine(
            repository.observe(bonsaiId),
            careReminders.observeForBonsai(bonsaiId),
            milestones.observeMilestones(bonsaiId),
            milestones.observePhotos(bonsaiId),
            stageTransitions.observeForBonsai(bonsaiId),
        ) { bonsai, reminders, milestoneRows, photos, transitions ->
            val sp = bonsai?.let { speciesByName[it.species.lowercase()] }
            val photosByMilestone = photos
                .filter { it.milestoneId != null }
                .groupBy { it.milestoneId }
            DetailUiState(
                bonsai = bonsai,
                speciesKnown = sp != null,
                careDefaults = CareDefaults.forBonsaiType(sp?.bonsaiType),
                reminders = reminders,
                timeline = milestoneRows.map { milestone ->
                    MilestoneUi(
                        milestone = milestone,
                        photos = photosByMilestone[milestone.id].orEmpty()
                            .sortedBy { it.orderIndex },
                    )
                },
                vaultPhotos = photos.filter { it.milestoneId == null },
                stageHistory = transitions,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailUiState(isLoading = true),
        )

    /**
     * Edit this tree's core details. Uses a targeted UPDATE so the tree's
     * reminders, milestones, and photos are preserved (see REPLACE cascade note).
     * If the stage changed, a transition record is inserted automatically.
     */
    fun updateDetails(data: TreeFormData) {
        val current = uiState.value.bonsai ?: return
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repository.updateDetails(
                id = bonsaiId,
                name = data.name.trim(),
                species = data.species.trim(),
                stage = data.stage.value,
                health = data.health.value,
                acquiredYear = data.acquiredYear,
                acquisitionSource = data.acquisitionSource?.value,
                placement = data.placement?.value,
                origin = data.origin?.trim()?.ifBlank { null },
                acquiredFrom = data.acquiredFrom?.trim()?.ifBlank { null },
                updatedAt = now,
            )
            if (current.stage != data.stage.value) {
                stageTransitions.recordTransition(
                    bonsaiId = bonsaiId,
                    fromStage = current.stage,
                    toStage = data.stage.value,
                    recordedAt = now,
                )
            }
        }
    }

    /** Set the cover photo from an in-app capture (file already on disk). */
    fun setCoverPhoto(path: String) {
        viewModelScope.launch {
            val previous = uiState.value.bonsai?.coverPhotoPath
            repository.setCoverPhoto(bonsaiId, path, System.currentTimeMillis())
            if (previous != null && previous != path) imageStore.delete(previous)
        }
    }

    /** Set the cover photo from a gallery pick (copied into storage first). */
    fun setCoverPhotoFromGallery(uri: Uri) {
        viewModelScope.launch {
            val path = imageStore.importFromUri(uri) ?: return@launch
            setCoverPhoto(path)
        }
    }

    /** Add a captured photo (file already on disk) to the tree's photo vault. */
    fun addVaultPhoto(path: String) {
        viewModelScope.launch { milestones.addVaultPhoto(bonsaiId, path, System.currentTimeMillis()) }
    }

    /** Add a gallery photo to the vault (copied into storage first). */
    fun addVaultPhotoFromGallery(uri: Uri) {
        viewModelScope.launch {
            val path = imageStore.importFromUri(uri) ?: return@launch
            milestones.addVaultPhoto(bonsaiId, path, System.currentTimeMillis())
        }
    }

    fun deleteVaultPhoto(id: Long) {
        viewModelScope.launch { milestones.deletePhoto(id) }
    }

    /** Persist a new milestone with its staged photo files. */
    fun addMilestone(title: String, notes: String?, occurredAt: Long, photoPaths: List<String>) {
        viewModelScope.launch {
            milestones.addMilestone(
                bonsaiId = bonsaiId,
                title = title.trim(),
                notes = notes?.trim()?.ifBlank { null },
                occurredAt = occurredAt,
                photoPaths = photoPaths,
                now = System.currentTimeMillis(),
            )
        }
    }

    fun deleteMilestone(id: Long) {
        viewModelScope.launch { milestones.deleteMilestone(id) }
    }

    /** Clean up staged capture files when an add-milestone flow is cancelled. */
    fun discardPhotos(paths: List<String>) {
        if (paths.isEmpty()) return
        viewModelScope.launch { milestones.discardStaged(paths) }
    }

    /** Mark a care task done: advance its due date by one full interval from now. */
    fun completeReminder(id: Long, intervalDays: Int) {
        viewModelScope.launch {
            careReminders.completeTask(id, intervalDays, System.currentTimeMillis())
        }
    }

    /** Reschedule a care task to a specific date chosen by the owner. */
    fun rescheduleReminder(id: Long, newDueAt: Long) {
        viewModelScope.launch { careReminders.reschedule(id, newDueAt) }
    }

    /** Delete this tree and all its photo files. The caller navigates back. */
    fun delete() {
        viewModelScope.launch {
            milestones.deleteAllFilesFor(bonsaiId)
            imageStore.delete(uiState.value.bonsai?.coverPhotoPath)
            repository.remove(bonsaiId)
        }
    }

    /** Set up every default reminder for this species (the "Use Default Schedule" path). */
    fun useDefaultSchedule() {
        saveReminders(uiState.value.careDefaults)
    }

    /**
     * Persist [chosen] as this tree's reminders and mark its schedule as set.
     * First-due dates are computed from each cadence.
     */
    fun saveReminders(chosen: List<CareDefault>) {
        if (chosen.isEmpty()) return
        if (uiState.value.bonsai == null) return
        val now = System.currentTimeMillis()
        val reminders = chosen.map { default ->
            CareReminderEntity(
                bonsaiId = bonsaiId,
                type = default.type.value,
                intervalDays = default.intervalDays,
                nextDueAt = now + default.intervalDays * MILLIS_PER_DAY,
                createdAt = now,
            )
        }
        viewModelScope.launch {
            careReminders.replaceForBonsai(bonsaiId, reminders)
            // Targeted update, not save(copy(...)): the latter's REPLACE insert
            // would cascade-delete the reminders we just wrote (and the photos).
            repository.markCareScheduleSet(bonsaiId, now)
        }
    }

    companion object {
        private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

        fun provideFactory(bonsaiId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnsoApp
                DetailViewModel(
                    bonsaiId,
                    app.bonsaiRepository,
                    app.careReminderRepository,
                    app.milestoneRepository,
                    app.stageTransitionRepository,
                    app.imageStore,
                    app.speciesCatalog,
                )
            }
        }
    }
}

data class DetailUiState(
    val bonsai: BonsaiEntity? = null,
    val speciesKnown: Boolean = false,
    val careDefaults: List<CareDefault> = emptyList(),
    val reminders: List<CareReminderEntity> = emptyList(),
    val timeline: List<MilestoneUi> = emptyList(),
    val vaultPhotos: List<PhotoEntity> = emptyList(),
    val stageHistory: List<StageTransitionEntity> = emptyList(),
    val isLoading: Boolean = false,
)

data class MilestoneUi(
    val milestone: MilestoneEntity,
    val photos: List<PhotoEntity> = emptyList(),
)
