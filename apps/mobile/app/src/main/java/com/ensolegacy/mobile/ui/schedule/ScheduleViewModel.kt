package com.ensolegacy.mobile.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ensolegacy.mobile.EnsoApp
import com.ensolegacy.mobile.data.local.ReminderWithBonsai
import com.ensolegacy.mobile.data.repository.CareReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleViewModel(private val repo: CareReminderRepository) : ViewModel() {

    val uiState: StateFlow<ScheduleUiState> = repo.observeAllWithBonsai()
        .map { items ->
            Log.d("ScheduleVM", "Loaded ${items.size} reminders")
            ScheduleUiState(reminders = items)
        }
        .catch { e ->
            Log.e("ScheduleVM", "Room query failed", e)
            e.printStackTrace()
            emit(ScheduleUiState(error = "Could not load reminders: ${e.message}"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleUiState())

    fun completeTask(reminderId: Long, intervalDays: Int) {
        viewModelScope.launch {
            repo.completeTask(reminderId, intervalDays, System.currentTimeMillis())
        }
    }

    fun reschedule(reminderId: Long, newDueAt: Long) {
        viewModelScope.launch { repo.reschedule(reminderId, newDueAt) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnsoApp
                ScheduleViewModel(app.careReminderRepository)
            }
        }
    }
}

data class ScheduleUiState(
    val reminders: List<ReminderWithBonsai> = emptyList(),
    val error: String? = null,
)
