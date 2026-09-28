/**
 * Helper utility querying system CallLog.Calls for recent outgoing call history.
 */
package io.github.hohojia886.callblocker.util

import android.content.Context
import android.provider.CallLog

object OutgoingCallHelper {

    /**
     * Checks if the user placed an outgoing call to the specified E.164 number within the last [days] days.
     */
    fun hasRecentOutgoingCall(context: Context, incomingE164Number: String, days: Int): Boolean {
        if (days <= 0 || incomingE164Number.isBlank() || incomingE164Number == "UNKNOWN") {
            return false
        }

        return try {
            val cutoffTime = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L)
            val primarySimIso = PhoneNumberUtils.getPrimarySimCountryIso(context)

            val projection = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE)
            val selection = "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.DATE} >= ?"
            val selectionArgs = arrayOf(CallLog.Calls.OUTGOING_TYPE.toString(), cutoffTime.toString())

            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->
                val numCol = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                while (cursor.moveToNext()) {
                    val rawOutgoingNum = if (numCol >= 0) cursor.getString(numCol) else null
                    if (!rawOutgoingNum.isNullOrBlank()) {
                        val outgoingE164 = PhoneNumberUtils.formatToE164(rawOutgoingNum, primarySimIso)
                        if (outgoingE164 == incomingE164Number) {
                            return true
                        }
                    }
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }
}
