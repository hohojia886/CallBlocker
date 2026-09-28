/**
 * System block list importer querying BlockedNumberContract and ContactsContract using active SIM ISO.
 */
package io.github.hohojia886.callblocker.util

import android.content.Context
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import io.github.hohojia886.callblocker.R
import io.github.hohojia886.callblocker.data.db.AppDatabase
import io.github.hohojia886.callblocker.data.db.BlockedNumber

/** Result data class representing system block list import counts and restriction state. */
data class ImportResult(
    val count: Int,
    val isBlockedNumberContractRestricted: Boolean = false,
    val errorMessage: String? = null,
    val displaySummary: String = ""
)

object SystemBlockListImporter {

    /** Queries system blocked numbers and contacts sent to voicemail, formatting with primary active SIM ISO. */
    suspend fun importSystemBlockList(context: Context): ImportResult {
        val importedMap = mutableMapOf<String, String>()
        var isBlockedNumberContractRestricted = false
        val primarySimIso = PhoneNumberUtils.getPrimarySimCountryIso(context)

        val sourceSystemBlockList = context.getString(R.string.import_source_system_block_list)
        val sourceBlockedContactDefault = context.getString(R.string.import_source_blocked_contact)

        // 1. Query BlockedNumberContract
        try {
            val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val origIndex = cursor.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
                val e164Index = cursor.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER)
                while (cursor.moveToNext()) {
                    val origNumber = if (origIndex >= 0) cursor.getString(origIndex) else null
                    val e164Number = if (e164Index >= 0) cursor.getString(e164Index) else null
                    val rawNumber = e164Number ?: origNumber
                    if (!rawNumber.isNullOrBlank()) {
                        val formatted = PhoneNumberUtils.formatToE164(rawNumber, primarySimIso)
                        importedMap[formatted] = sourceSystemBlockList
                    }
                }
            }
        } catch (_: SecurityException) {
            isBlockedNumberContractRestricted = true
        } catch (_: Exception) {
            // BlockedNumberContract query error fallback
        }

        // 2. Query Contacts marked as SEND_TO_VOICEMAIL = 1
        try {
            val blockedContactIds = mutableSetOf<Long>()
            context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.SEND_TO_VOICEMAIL),
                "${ContactsContract.Contacts.SEND_TO_VOICEMAIL} = 1 OR ${ContactsContract.Contacts.SEND_TO_VOICEMAIL} = '1'",
                null,
                null
            )?.use { c ->
                val idCol = c.getColumnIndex(ContactsContract.Contacts._ID)
                while (c.moveToNext()) {
                    if (idCol >= 0) blockedContactIds.add(c.getLong(idCol))
                }
            }

            for (contactId in blockedContactIds) {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId.toString()),
                    null
                )?.use { c ->
                    val numCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val nameCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    while (c.moveToNext()) {
                        val num = if (numCol >= 0) c.getString(numCol) else null
                        val name = if (nameCol >= 0) c.getString(nameCol) else null
                        val noteText = if (!name.isNullOrBlank()) {
                            context.getString(R.string.import_source_blocked_contact_with_name, name)
                        } else {
                            sourceBlockedContactDefault
                        }
                        if (!num.isNullOrBlank()) {
                            val formatted = PhoneNumberUtils.formatToE164(num, primarySimIso)
                            if (!importedMap.containsKey(formatted)) {
                                importedMap[formatted] = noteText
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // ContactsContract query error fallback
        }

        if (importedMap.isEmpty()) {
            return ImportResult(
                count = 0,
                isBlockedNumberContractRestricted = isBlockedNumberContractRestricted,
                errorMessage = context.getString(R.string.import_error_empty)
            )
        }

        val dao = AppDatabase.getInstance(context).blockedNumberDao()
        val existingList = dao.getAllList()
        val existingMap = existingList.associateBy { it.numberPattern }

        val itemsToInsert = mutableListOf<BlockedNumber>()
        val itemsToUpdate = mutableListOf<BlockedNumber>()
        var newlyAddedCount = 0
        var updatedCount = 0

        for ((pattern, newNote) in importedMap) {
            val existingItem = existingMap[pattern]
            if (existingItem != null) {
                if (existingItem.note != newNote) {
                    itemsToUpdate.add(existingItem.copy(note = newNote))
                    updatedCount++
                }
            } else {
                itemsToInsert.add(BlockedNumber(numberPattern = pattern, note = newNote))
                newlyAddedCount++
            }
        }

        if (itemsToInsert.isNotEmpty()) {
            dao.insertAll(itemsToInsert)
        }
        if (itemsToUpdate.isNotEmpty()) {
            dao.updateAll(itemsToUpdate)
        }

        val totalProcessed = newlyAddedCount + updatedCount
        val summaryMsg = when {
            newlyAddedCount > 0 && updatedCount > 0 -> context.getString(R.string.import_summary_added_and_updated, newlyAddedCount, updatedCount)
            newlyAddedCount > 0 -> context.getString(R.string.import_summary_added_only, newlyAddedCount)
            updatedCount > 0 -> context.getString(R.string.import_summary_updated_only, updatedCount)
            else -> context.getString(R.string.import_summary_already_latest)
        }

        return ImportResult(
            count = totalProcessed,
            isBlockedNumberContractRestricted = isBlockedNumberContractRestricted,
            errorMessage = null,
            displaySummary = summaryMsg
        )
    }
}
