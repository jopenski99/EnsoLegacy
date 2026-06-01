package com.ensolegacy.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ensolegacy.mobile.ui.MainScaffold
import com.ensolegacy.mobile.ui.onboarding.OnboardingScreen
import com.ensolegacy.mobile.ui.onboarding.OnboardingViewModel
import com.ensolegacy.mobile.ui.theme.EnsoLegacyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnsoLegacyTheme {
                val onboarding: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory)
                val onboardingComplete by onboarding.isComplete.collectAsStateWithLifecycle()

                if (onboardingComplete) {
                    MainScaffold()
                } else {
                    OnboardingScreen(onFinish = onboarding::complete)
                }
            }
        }
    }
}
