package com.ensolegacy.mobile.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ensolegacy.mobile.EnsoApp
import com.ensolegacy.mobile.data.SpeciesCatalog
import com.ensolegacy.mobile.data.local.BonsaiEntity
import com.ensolegacy.mobile.data.local.CareReminderEntity
import com.ensolegacy.mobile.data.repository.BonsaiRepository
import com.ensolegacy.mobile.data.repository.CareReminderRepository
import com.ensolegacy.mobile.domain.CareDefault
import com.ensolegacy.mobile.domain.CareDefaults
import com.ensolegacy.mobile.domain.Species
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for a single tree's detail view. Observes the tree and its care
 * reminders, derives the species' default schedule, and routes the delete /
 * set-up-reminders intents to the repositories.
 */
class DetailViewModel(
    private val bonsaiId: Long,
    private val repository: BonsaiRepository,
    private val careReminders: CareReminderRepository,
    speciesCatalog: SpeciesCatalog,
) : ViewModel() {

    /** Catalog species keyed by lower-cased common name (how the tree stores it). */
    private val speciesByName: Map<String, Species> =
        speciesCatalog.all.associateBy { it.commonName.lowercase() }

    val uiState: StateFlow<DetailUiState> =
        combine(
            repository.observe(bonsaiId),
            careReminders.observeForBonsai(bonsaiId),
        ) { bonsai, reminders ->
            val species = bonsai?.let { speciesByName[it.species.lowercase()] }
            DetailUiState(
                bonsai = bonsai,
                speciesKnown = species != null,
                careDefaults = CareDefaults.forBonsaiType(species?.bonsaiType),
                reminders = reminders,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailUiState(isLoading = true),
        )

    /** Delete this tree. The caller navigates back once invoked. */
    fun delete() {
        viewModelScope.launch { repository.remove(bonsaiId) }
    }

    /** Set up every default reminder for this species (the "Use Default Schedule" path). */
    fun useDefaultSchedule() {
        saveReminders(uiState.value.careDefaults)
    }

    /**
     * Persist [chosen] as this tree's reminders and mark its schedule as set.
     * First-due dates are computed from each cadence; per-task date editing and
     * notifications are later slices.
     */
    fun saveReminders(chosen: List<CareDefault>) {
        if (chosen.isEmpty()) return
        val bonsai = uiState.value.bonsai ?: return
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
            repository.save(bonsai.copy(careScheduleSet = true))
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
                    app.speciesCatalog,
                )
            }
        }
    }
}

data class DetailUiState(
    val bonsai: BonsaiEntity? = null,
    /** Whether this tree's species is one we recognize from the catalog. */
    val speciesKnown: Boolean = false,
    /** Suggested default schedule for this tree's species. */
    val careDefaults: List<CareDefault> = emptyList(),
    /** Reminders already set up for this tree. */
    val reminders: List<CareReminderEntity> = emptyList(),
    val isLoading: Boolean = false,
)
