package com.ensolegacy.mobile.ui.settings

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ensolegacy.mobile.EnsoApp
import com.ensolegacy.mobile.data.AppPreferences
import com.ensolegacy.mobile.data.BackupManager
import com.ensolegacy.mobile.data.repository.BonsaiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs both backup surfaces: the Settings "Backup" section (export, restore
 * from this device, restore from a picked file) and the one-time restore
 * prompt MainActivity shows on a fresh install when a backup is found and the
 * collection is still empty.
 */
class BackupViewModel(
    private val backupManager: BackupManager,
    private val appPreferences: AppPreferences,
    bonsaiRepository: BonsaiRepository,
) : ViewModel() {

    /** All backup zips found on the device, newest first (API 29+). */
    private val _deviceBackups = MutableStateFlow<List<BackupManager.BackupFileInfo>>(emptyList())
    val deviceBackups: StateFlow<List<BackupManager.BackupFileInfo>> = _deviceBackups

    /** The subset of [deviceBackups] this app can actually open (ownership survives reinstall). */
    private val _readableBackups = MutableStateFlow<List<BackupManager.BackupFileInfo>>(emptyList())
    val readableBackups: StateFlow<List<BackupManager.BackupFileInfo>> = _readableBackups

    /** Newest *readable* backup — what the fresh-install prompt offers. */
    private val _promptBackup = MutableStateFlow<BackupManager.BackupFileInfo?>(null)
    val promptBackup: StateFlow<BackupManager.BackupFileInfo?> = _promptBackup

    /**
     * Null until Room emits its first list. The fresh-install prompt keys off
     * an exact 0 so it never fires before the real count is known.
     */
    val treeCount: StateFlow<Int?> = bonsaiRepository.observeCollection()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _working = MutableStateFlow(false)
    val working: StateFlow<Boolean> = _working

    /** One-shot status text for a result dialog; consumed via [clearMessage]. */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    /** Whether "All files access" is granted (API 30+); rechecked on every refresh. */
    private val _allFilesAccess = MutableStateFlow(backupManager.hasAllFilesAccess())
    val allFilesAccess: StateFlow<Boolean> = _allFilesAccess

    private val _promptDismissed = MutableStateFlow(appPreferences.backupRestorePromptDismissed)
    val promptDismissed: StateFlow<Boolean> = _promptDismissed

    init {
        refreshDeviceBackup()
    }

    fun refreshDeviceBackup() {
        viewModelScope.launch {
            // Detection must never crash the app — a MediaStore hiccup (e.g.
            // provider still cold right after a data wipe) just reads as
            // "nothing found" and is logged.
            runCatching {
                _allFilesAccess.value = backupManager.hasAllFilesAccess()
                // Two tiers: MediaStore (backups this install wrote) + direct
                // scan (everything, needs All files access). MediaStore wins
                // on duplicates since its entries stay readable without a grant.
                val found = (backupManager.findBackups() + backupManager.findBackupsDirect())
                    .distinctBy { it.displayName }
                    .sortedByDescending { it.dateAdded }
                val readable = found.filter { backupManager.isReadable(it.uri) }
                Triple(found, readable, readable.firstOrNull())
            }.onSuccess { (found, readable, prompt) ->
                _deviceBackups.value = found
                _readableBackups.value = readable
                _promptBackup.value = prompt
            }.onFailure {
                Log.w(TAG, "Device backup detection failed", it)
                _deviceBackups.value = emptyList()
                _readableBackups.value = emptyList()
                _promptBackup.value = null
            }
        }
    }

    fun export() {
        if (_working.value) return
        viewModelScope.launch {
            _working.value = true
            runCatching { backupManager.export() }
                .onSuccess { r ->
                    _message.value = "Backup saved to Documents/Enso as ${r.displayName} " +
                        "(${count(r.trees, "tree")}, ${count(r.photos, "photo")}). " +
                        "Keep a copy somewhere safe — Drive, a PC — it won't survive a " +
                        "factory reset on its own."
                    refreshDeviceBackup()
                }
                .onFailure { _message.value = "Couldn't create the backup: ${it.message}" }
            _working.value = false
        }
    }

    fun restore(uri: Uri) {
        if (_working.value) return
        viewModelScope.launch {
            _working.value = true
            runCatching { backupManager.restoreFrom(uri) }
                .onSuccess { s ->
                    _message.value = "Restored ${count(s.trees, "tree")}, " +
                        "${count(s.milestones, "milestone")}, ${count(s.photos, "photo")}."
                }
                .onFailure { _message.value = "Couldn't restore that backup: ${it.message}" }
            _working.value = false
        }
    }

    /** "Not now" on the fresh-install prompt — restore stays available in Settings. */
    fun dismissRestorePrompt() {
        appPreferences.backupRestorePromptDismissed = true
        _promptDismissed.value = true
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        private const val TAG = "BackupViewModel"

        private fun count(n: Int, word: String) = "$n $word${if (n == 1) "" else "s"}"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EnsoApp
                BackupViewModel(app.backupManager, app.appPreferences, app.bonsaiRepository)
            }
        }
    }
}
