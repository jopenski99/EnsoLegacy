package com.ensolegacy.mobile.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    backupViewModel: BackupViewModel = viewModel(factory = BackupViewModel.Factory),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Backup section state (the result message dialog lives in MainActivity).
    val deviceBackups by backupViewModel.deviceBackups.collectAsStateWithLifecycle()
    val readableBackups by backupViewModel.readableBackups.collectAsStateWithLifecycle()
    val backupWorking by backupViewModel.working.collectAsStateWithLifecycle()
    // (displayName?, uri) — the name rides along so the PIN/lockout messages can name the file.
    var confirmRestore by remember { mutableStateOf<Pair<String?, Uri>?>(null) }
    var showBackupChooser by remember { mutableStateOf(false) }
    var showExportPin by remember { mutableStateOf(false) }
    val pickBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> if (uri != null) confirmRestore = (null as String?) to uri }

    // Recheck notification status whenever the user returns from system settings.
    var notifEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notifEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                // Returning from a system settings screen may have changed the
                // All-files grant or the backup folder's contents.
                backupViewModel.refreshDeviceBackup()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) notifEnabled = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader("Notifications")
            SettingsRow(
                label = "Care reminders",
                description = "Daily nudges when a task is overdue",
                trailingContent = {
                    StatusBadge(enabled = notifEnabled)
                },
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                },
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifEnabled) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    SettingsRow(
                        label = "Enable notifications",
                        description = "Allow Ens\u014D Legacy to send care reminders",
                        trailingContent = {
                            Text(
                                text = "Allow",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                        onClick = { notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionHeader("Backup")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                SettingsRow(
                    label = "Export backup",
                    description = "Trees, photos, and history saved to Documents/Enso",
                    trailingContent = {
                        if (backupWorking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    },
                    onClick = { if (!backupWorking) showExportPin = true },
                )
                SettingsRow(
                    label = "Restore from this device",
                    description = when {
                        deviceBackups.isEmpty() -> "No backup found on this device"
                        readableBackups.isEmpty() ->
                            "${deviceBackups.size} found, but not readable here — use the file picker below"
                        readableBackups.size == 1 ->
                            "${readableBackups.first().displayName} · " +
                                backupDate(readableBackups.first().dateAdded)
                        else -> "${readableBackups.size} backups found — tap to choose"
                    },
                    onClick = if (readableBackups.isNotEmpty() && !backupWorking) {
                        {
                            if (readableBackups.size == 1) {
                                confirmRestore = readableBackups.first()
                                    .let { it.displayName to it.uri }
                            } else {
                                showBackupChooser = true
                            }
                        }
                    } else {
                        null
                    },
                )
            } else {
                SettingsRow(
                    label = "On-device backup",
                    description = "Saving to shared storage needs Android 10 or newer",
                )
            }
            SettingsRow(
                label = "Choose a backup file…",
                description = "Restore from an Enso backup zip",
                onClick = {
                    if (!backupWorking) {
                        pickBackupLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                    }
                },
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val allFilesAccess by backupViewModel.allFilesAccess.collectAsStateWithLifecycle()
                SettingsRow(
                    label = "Find older backups",
                    description = "All-files access — spots backups left by a previous install",
                    trailingContent = {
                        StatusBadge(enabled = allFilesAccess)
                    },
                    onClick = {
                        val intent =
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        context.startActivity(intent)
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionHeader("About")
            SettingsRow(
                label = "Version",
                trailingContent = {
                    Text(
                        text = appVersion(context),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            SettingsRow(
                label = "Built with patience",
                description = "For trees that outlive us",
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Multiple backups on the device — let the owner pick which one to restore.
    if (showBackupChooser && readableBackups.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showBackupChooser = false },
            title = { Text("Choose a backup") },
            text = {
                Column {
                    readableBackups.forEach { info ->
                        Text(
                            text = info.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showBackupChooser = false
                                    confirmRestore = info.displayName to info.uri
                                }
                                .padding(vertical = 10.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupChooser = false }) { Text("Cancel") }
            },
        )
    }

    // Export is PIN-protected (format v2) — collect it before writing anything.
    if (showExportPin) {
        ExportPinDialog(
            working = backupWorking,
            onConfirm = { pin ->
                showExportPin = false
                backupViewModel.export(pin)
            },
            onDismiss = { showExportPin = false },
        )
    }

    // Restore is a full replace — confirm before wiping current data.
    confirmRestore?.let { (name, uri) ->
        AlertDialog(
            onDismissRequest = { confirmRestore = null },
            title = { Text("Restore backup?") },
            text = { Text("This replaces everything currently in the app with the contents of the backup.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestore = null
                    backupViewModel.beginRestore(uri, name)
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestore = null }) { Text("Cancel") }
            },
        )
    }
}

// ---------------------------------------------------------------------------

private val backupDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

/** MediaStore DATE_ADDED is epoch *seconds*. */
private fun backupDate(epochSeconds: Long): String =
    Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(backupDayFormatter)

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SettingsRow(
    label: String,
    description: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

@Composable
private fun StatusBadge(enabled: Boolean) {
    val (bgColor, textColor, label) = if (enabled) {
        Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "On",
        )
    } else {
        Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Off",
        )
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

private fun appVersion(context: android.content.Context): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—"
} catch (_: Exception) {
    "—"
}
