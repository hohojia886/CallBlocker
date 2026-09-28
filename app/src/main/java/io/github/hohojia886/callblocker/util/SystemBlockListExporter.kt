/**
 * Utility exporting custom blocked numbers to the system BlockedNumberContract provider.
 */
package io.github.hohojia886.callblocker.util

import android.content.ContentValues
import android.content.Context
import android.provider.BlockedNumberContract
import io.github.hohojia886.callblocker.R
import io.github.hohojia886.callblocker.data.db.AppDatabase

/** Result data class representing system block list export counts and permission state. */
data class ExportResult(
    val addedCount: Int,
    val isRoleRequired: Boolean = false,
    val errorMessage: String? = null,
    val displaySummary: String = ""
)

object SystemBlockListExporter {

    /** Exports all custom blocked numbers from CallBlocker Room DB to system BlockedNumberContract. */
    suspend fun exportAllToSystemBlockList(context: Context): ExportResult {
        var addedCount = 0
        var isRoleRequired = false

        val dao = AppDatabase.getInstance(context).blockedNumberDao()
        val customList = dao.getAllList()

        if (customList.isEmpty()) {
            return ExportResult(
                addedCount = 0,
                errorMessage = context.getString(R.string.export_error_empty)
            )
        }

        try {
            val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
            for (item in customList) {
                val pattern = item.numberPattern
                val cleanNumber = pattern.replace("*", "")
                if (cleanNumber.isBlank() || cleanNumber == "UNKNOWN") continue

                val isBlockedInSystem = try {
                    BlockedNumberContract.isBlocked(context, cleanNumber)
                } catch (_: Exception) {
                    false
                }

                if (!isBlockedInSystem) {
                    val values = ContentValues().apply {
                        put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, pattern)
                        put(BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER, cleanNumber)
                    }
                    val insertedUri = context.contentResolver.insert(uri, values)
                    if (insertedUri != null) {
                        addedCount++
                    }
                }
            }
        } catch (_: SecurityException) {
            isRoleRequired = true
        } catch (e: Exception) {
            return ExportResult(
                addedCount = addedCount,
                errorMessage = e.localizedMessage ?: context.getString(R.string.export_error_failed)
            )
        }

        val summaryMsg = if (addedCount > 0) {
            context.getString(R.string.export_summary_added, addedCount)
        } else {
            context.getString(R.string.export_summary_already_exists)
        }

        return ExportResult(
            addedCount = addedCount,
            isRoleRequired = isRoleRequired,
            errorMessage = null,
            displaySummary = summaryMsg
        )
    }
}
