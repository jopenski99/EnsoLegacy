package com.ensolegacy.mobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ensolegacy.mobile.ui.MainScaffold
import com.ensolegacy.mobile.ui.onboarding.OnboardingScreen
import com.ensolegacy.mobile.ui.onboarding.OnboardingViewModel
import com.ensolegacy.mobile.ui.settings.BackupViewModel
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

                    // Fresh-install restore prompt: a readable backup on this
                    // device plus an empty collection means the owner probably
                    // reinstalled. "Not now" dismisses it for good — restore
                    // stays available in Settings either way.
                    val backup: BackupViewModel = viewModel(factory = BackupViewModel.Factory)
                    val treeCount by backup.treeCount.collectAsStateWithLifecycle()
                    val promptBackup by backup.promptBackup.collectAsStateWithLifecycle()
                    val promptDismissed by backup.promptDismissed.collectAsStateWithLifecycle()
                    val backupMessage by backup.message.collectAsStateWithLifecycle()
                    var promptHidden by remember { mutableStateOf(false) }

                    val found = promptBackup
                    if (!promptHidden && !promptDismissed && treeCount == 0 && found != null) {
                        AlertDialog(
                            onDismissRequest = {
                                promptHidden = true
                                backup.dismissRestorePrompt()
                            },
                            title = { Text("Restore your collection?") },
                            text = {
                                Text("A backup (${found.displayName}) was found on this device. Restore your trees, photos, and history?")
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    promptHidden = true
                                    backup.restore(found.uri)
                                }) { Text("Restore") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    promptHidden = true
                                    backup.dismissRestorePrompt()
                                }) { Text("Not now") }
                            },
                        )
                    }

                    // App-wide backup result dialog (export/restore outcomes).
                    backupMessage?.let { msg ->
                        AlertDialog(
                            onDismissRequest = backup::clearMessage,
                            text = { Text(msg) },
                            confirmButton = {
                                TextButton(onClick = backup::clearMessage) { Text("OK") }
                            },
                        )
                    }
                } else {
                    OnboardingScreen(onFinish = onboarding::complete)
                }
            }
        }
    }
}
