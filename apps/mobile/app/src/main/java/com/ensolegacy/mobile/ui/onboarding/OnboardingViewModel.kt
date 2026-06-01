package com.ensolegacy.mobile.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ensolegacy.mobile.EnsoApp
import com.ensolegacy.mobile.data.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Decides whether the onboarding flow should be shown and records when it's
 * done. Seeds its state synchronously from [AppPreferences] so the first frame
 * already knows where to send the user — no onboarding flash for returning users.
 */
class OnboardingViewModel(
    private val preferences: AppPreferences,
) : ViewModel() {

    private val _isComplete = MutableStateFlow(preferences.hasCompletedOnboarding)
    val isComplete: StateFlow<Boolean> = _isComplete.asStateFlow()

    /** Mark onboarding finished (or skipped) and advance into the app. */
    fun complete() {
        preferences.hasCompletedOnboarding = true
        _isComplete.value = true
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnsoApp
                OnboardingViewModel(app.appPreferences)
            }
        }
    }
}
