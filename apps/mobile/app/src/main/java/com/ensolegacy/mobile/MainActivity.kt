package com.ensolegacy.mobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
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

                val notifPermLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* granted or denied — system dialog handled the prompt */ }

                if (onboardingComplete) {
                    // Request POST_NOTIFICATIONS on Android 13+ the first time the main
                    // app is shown. Fires once per composition entry (i.e. once after
                    // onboarding completes, and not on every recomposition).
                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    MainScaffold()
                } else {
                    OnboardingScreen(onFinish = onboarding::complete)
                }
            }
        }
    }
}
