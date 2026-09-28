/**
 * Settings ViewModel handling protection rule toggles, system block list imports, DB resets, and encrypted backups.
 */
package io.github.hohojia886.callblocker.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import io.github.hohojia886.callblocker.data.db.AppDatabase
import io.github.hohojia886.callblocker.data.db.BlockedNumber
import io.github.hohojia886.callblocker.data.pref.PreferencesManager
import io.github.hohojia886.callblocker.util.BackupCryptoManager
import io.github.hohojia886.callblocker.util.ExportResult
import io.github.hohojia886.callblocker.util.ImportResult
import io.github.hohojia886.callblocker.util.SystemBlockListExporter
import io.github.hohojia886.callblocker.util.SystemBlockListImporter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Data model representing backup payload content (only blocked numbers). */
data class BackupPayload(
    val blockedNumbers: List<BlockedNumber>?
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val prefs = PreferencesManager(application)
    private val gson = Gson()

    /** Flow emitting state for unknown/private call blocking. */
    val blockUnknown: StateFlow<Boolean> = prefs.blockUnknown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Flow emitting state for non-contact call blocking. */
    val blockNonContacts: StateFlow<Boolean> = prefs.blockNonContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Flow emitting state for international call blocking. */
    val blockInternational: StateFlow<Boolean> = prefs.blockInternational
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Flow emitting state for auto-adding rejected calls to block list. */
    val autoBlockRejected: StateFlow<Boolean> = prefs.autoBlockRejected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Flow emitting outgoing callback exemption period in days (0 = Off, 1 = 1 Day, 3 = 3 Days, 7 = 1 Week). */
    val outgoingCallbackExemptionDays: StateFlow<Int> = prefs.outgoingCallbackExemptionDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Flow emitting current interception action mode (0 = Reject, 1 = Silence). */
    val blockAction: StateFlow<Int> = prefs.blockAction
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Updates unknown number blocking setting. */
    fun setBlockUnknown(enabled: Boolean) {
        viewModelScope.launch { prefs.setBlockUnknown(enabled) }
    }

    /** Updates non-contact call blocking setting. */
    fun setBlockNonContacts(enabled: Boolean) {
        viewModelScope.launch { prefs.setBlockNonContacts(enabled) }
    }

    /** Updates international call blocking setting. */
    fun setBlockInternational(enabled: Boolean) {
        viewModelScope.launch { prefs.setBlockInternational(enabled) }
    }

    /** Updates auto-add rejected calls setting. */
    fun setAutoBlockRejected(enabled: Boolean) {
        viewModelScope.launch { prefs.setAutoBlockRejected(enabled) }
    }

    /** Updates outgoing callback exemption window in days. */
    fun setOutgoingCallbackExemptionDays(days: Int) {
        viewModelScope.launch { prefs.setOutgoingCallbackExemptionDays(days) }
    }

    /** Updates interception action mode. */
    fun setBlockAction(action: Int) {
        viewModelScope.launch { prefs.setBlockAction(action) }
    }

    /** Imports system blocked numbers and sends result callback. */
    fun importSystemBlockList(onResult: (ImportResult) -> Unit) {
        viewModelScope.launch {
            val result = SystemBlockListImporter.importSystemBlockList(getApplication())
            onResult(result)
        }
    }

    /** Exports custom blocked numbers to Android system BlockedNumberContract. */
    fun exportToSystemBlockList(onResult: (ExportResult) -> Unit) {
        viewModelScope.launch {
            val result = SystemBlockListExporter.exportAllToSystemBlockList(getApplication())
            onResult(result)
        }
    }

    /** Resets and clears only the persistent block analytics table. */
    fun resetDashboardStatistics(onResult: () -> Unit) {
        viewModelScope.launch {
            db.blockAnalyticsDao().deleteAll()
            onResult()
        }
    }

    /** Resets and clears only the call history table. */
    fun resetInterceptionHistory(onResult: () -> Unit) {
        viewModelScope.launch {
            db.callHistoryDao().deleteAll()
            onResult()
        }
    }

    /** Resets and clears all data tables in the Room database. */
    fun resetDatabase(onResult: () -> Unit) {
        viewModelScope.launch {
            db.blockedNumberDao().deleteAll()
            db.callHistoryDao().deleteAll()
            db.contactsCacheDao().deleteAll()
            db.blockAnalyticsDao().deleteAll()
            onResult()
        }
    }

    /** Exports blocked numbers list as an encrypted `.spamdb` file using AES-256-GCM. */
    fun exportEncryptedBackup(fileUri: Uri, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val blockedNumbers = db.blockedNumberDao().getAllList()
                val payload = BackupPayload(blockedNumbers)
                val jsonStr = gson.toJson(payload)

                val encryptedBytes = BackupCryptoManager.encrypt(jsonStr, password)

                getApplication<Application>().contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    outputStream.write(encryptedBytes)
                }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Export failed")
            }
        }
    }

    /** Decrypts `.spamdb` file and restores blocked numbers list into Room database. */
    fun restoreEncryptedBackup(fileUri: Uri, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val bytes = getApplication<Application>().contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    inputStream.readBytes()
                } ?: throw Exception("Cannot read backup file")

                val jsonStr = BackupCryptoManager.decrypt(bytes, password)
                val payload = gson.fromJson(jsonStr, BackupPayload::class.java)

                if (payload.blockedNumbers != null) {
                    db.blockedNumberDao().insertAll(payload.blockedNumbers)
                }

                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Restore failed (Wrong password or corrupted file)")
            }
        }
    }
}
