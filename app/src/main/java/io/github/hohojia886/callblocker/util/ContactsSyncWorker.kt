/**
 * Background WorkManager worker synchronizing system contacts to Room local cache using active SIM ISO.
 */
package io.github.hohojia886.callblocker.util

import android.content.Context
import android.provider.ContactsContract
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.hohojia886.callblocker.data.db.AppDatabase
import io.github.hohojia886.callblocker.data.db.ContactsCache

class ContactsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    /** Synchronizes system contacts to Room database cache using current active SIM country ISO. */
    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val primarySimIso = PhoneNumberUtils.getPrimarySimCountryIso(applicationContext)
            val contactsMap = mutableMapOf<String, String?>()

            val cursor = applicationContext.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ),
                null,
                null,
                null
            )

            cursor?.use { c ->
                val numCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

                while (c.moveToNext()) {
                    val rawNum = if (numCol >= 0) c.getString(numCol) else null
                    val name = if (nameCol >= 0) c.getString(nameCol) else null

                    if (!rawNum.isNullOrBlank()) {
                        val e164 = PhoneNumberUtils.formatToE164(rawNum, primarySimIso)
                        if (e164 != "UNKNOWN" && e164 != "*") {
                            contactsMap[e164] = name
                        }
                    }
                }
            }

            val cacheList = contactsMap.map { (num, name) ->
                ContactsCache(normalizedNumber = num, displayName = name)
            }

            db.contactsCacheDao().replaceAll(cacheList)
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}
