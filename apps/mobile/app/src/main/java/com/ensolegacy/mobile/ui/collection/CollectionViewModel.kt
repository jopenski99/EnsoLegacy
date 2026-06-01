package com.ensolegacy.mobile.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ensolegacy.mobile.EnsoApp
import com.ensolegacy.mobile.data.SpeciesCatalog
import com.ensolegacy.mobile.data.local.BonsaiEntity
import com.ensolegacy.mobile.data.repository.BonsaiRepository
import com.ensolegacy.mobile.domain.BonsaiStage
import com.ensolegacy.mobile.domain.HealthStatus
import com.ensolegacy.mobile.domain.Species
import java.time.Year
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * MVVM ViewModel for the collection screen. Exposes immutable UI state to
 * Compose and routes user intents to the repository — never touches the DAO.
 */
class CollectionViewModel(
    private val repository: BonsaiRepository,
    speciesCatalog: SpeciesCatalog,
) : ViewModel() {

    /** Static starter catalog backing the add-tree species picker. */
    val species: List<Species> = speciesCatalog.all

    val uiState: StateFlow<CollectionUiState> =
        repository.observeCollection()
            .map { collection ->
                CollectionUiState(
                    collection = collection,
                    stats = CollectionStats.from(collection),
                    isLoading = false,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CollectionUiState(isLoading = true),
            )

    /** Add a tree to the collection. */
    fun addBonsai(
        name: String,
        species: String,
        stage: BonsaiStage,
        health: HealthStatus,
        acquiredYear: Int?,
    ) {
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repository.save(
                BonsaiEntity(
                    name = name.trim(),
                    species = species.trim(),
                    stage = stage.value,
                    health = health.value,
                    acquiredYear = acquiredYear,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
    }

    /** Remove a tree from the collection. */
    fun removeBonsai(id: Long) {
        viewModelScope.launch { repository.remove(id) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnsoApp
                CollectionViewModel(app.bonsaiRepository, app.speciesCatalog)
            }
        }
    }
}

data class CollectionUiState(
    val collection: List<BonsaiEntity> = emptyList(),
    val stats: CollectionStats = CollectionStats(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * Aggregate counts for the collection dashboard. Derived from the collection
 * list, so it always stays in sync with what's on screen.
 */
data class CollectionStats(
    val total: Int = 0,
    val byHealth: Map<HealthStatus, Int> = emptyMap(),
    val byStage: Map<BonsaiStage, Int> = emptyMap(),
    val byAge: Map<AgeBucket, Int> = emptyMap(),
) {
    companion object {
        fun from(collection: List<BonsaiEntity>): CollectionStats {
            val currentYear = Year.now().value
            return CollectionStats(
                total = collection.size,
                byHealth = collection.groupingBy { HealthStatus.fromValue(it.health) }.eachCount(),
                byStage = collection.groupingBy { BonsaiStage.fromValue(it.stage) }.eachCount(),
                byAge = collection.groupingBy { AgeBucket.of(it.acquiredYear, currentYear) }.eachCount(),
            )
        }
    }
}

/** Coarse age buckets for the dashboard's Age view, based on `acquiredYear`. */
enum class AgeBucket(val label: String) {
    UNDER_5("Under 5 yrs"),
    FIVE_TO_10("5–10 yrs"),
    TEN_TO_20("10–20 yrs"),
    OVER_20("20+ yrs"),
    UNKNOWN("Unknown");

    companion object {
        fun of(acquiredYear: Int?, currentYear: Int): AgeBucket {
            if (acquiredYear == null) return UNKNOWN
            return when (currentYear - acquiredYear) {
                in Int.MIN_VALUE..4 -> UNDER_5
                in 5..9 -> FIVE_TO_10
                in 10..19 -> TEN_TO_20
                else -> OVER_20
            }
        }
    }
}
