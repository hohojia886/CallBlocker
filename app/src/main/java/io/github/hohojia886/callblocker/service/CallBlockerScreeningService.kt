/**
 * Core system service extending CallScreeningService to intercept incoming calls in real-time.
 */
package io.github.hohojia886.callblocker.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import io.github.hohojia886.callblocker.R
import io.github.hohojia886.callblocker.data.db.AppDatabase
import io.github.hohojia886.callblocker.data.db.BlockAnalytics
import io.github.hohojia886.callblocker.data.db.BlockReason
import io.github.hohojia886.callblocker.data.db.BlockedNumber
import io.github.hohojia886.callblocker.data.db.CallHistory
import io.github.hohojia886.callblocker.data.pref.PreferencesManager
import io.github.hohojia886.callblocker.util.ContactPickerHelper
import io.github.hohojia886.callblocker.util.NotificationHelper
import io.github.hohojia886.callblocker.util.OutgoingCallHelper
import io.github.hohojia886.callblocker.util.PhoneNumberUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CallBlockerScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    /** Handles incoming call details, evaluates blocking rules, logs blocked history, and sends response with safe fallback. */
    override fun onScreenCall(callDetails: Call.Details) {
        val handle = callDetails.handle
        val rawNumber = handle?.schemeSpecificPart

        serviceScope.launch {
            var response: CallResponse? = null
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val prefs = PreferencesManager(applicationContext)

                val blockUnknownSetting = prefs.blockUnknown.first()
                val blockNonContactsSetting = prefs.blockNonContacts.first()
                val blockInternationalSetting = prefs.blockInternational.first()
                val autoBlockRejectedSetting = prefs.autoBlockRejected.first()
                val blockAction = prefs.blockAction.first() // 0 = Reject, 1 = Silence
                val outgoingExemptionDays = prefs.outgoingCallbackExemptionDays.first()

                val activeSimIsos = PhoneNumberUtils.getActiveSimCountryIsos(applicationContext)
                val primaryCountryIso = activeSimIsos.firstOrNull() ?: "TW"

                var shouldBlock = false
                var blockReason = BlockReason.NONE

                if (rawNumber.isNullOrBlank()) {
                    if (blockUnknownSetting) {
                        shouldBlock = true
                        blockReason = BlockReason.NO_NUMBER
                    }
                } else {
                    val e164Number = PhoneNumberUtils.formatToE164(rawNumber, primaryCountryIso)

                    // 0. Highest Priority Exemption: Recent Outgoing Call Callback Exemption
                    val isOutgoingExempt = if (outgoingExemptionDays > 0) {
                        OutgoingCallHelper.hasRecentOutgoingCall(applicationContext, e164Number, outgoingExemptionDays)
                    } else {
                        false
                    }

                    if (isOutgoingExempt) {
                        shouldBlock = false
                    } else {
                        // 1. Check Non-Contacts
                        if (blockNonContactsSetting) {
                            val inContacts = ContactPickerHelper.isNumberInContacts(applicationContext, e164Number)
                            if (!inContacts) {
                                shouldBlock = true
                                blockReason = BlockReason.NON_CONTACT
                            }
                        }

                        // 2. Check International
                        if (!shouldBlock && blockInternationalSetting) {
                            val isInternational = PhoneNumberUtils.isInternational(e164Number, activeSimIsos)
                            if (isInternational) {
                                shouldBlock = true
                                blockReason = BlockReason.INTERNATIONAL
                            }
                        }

                        // 3. Check Block List
                        if (!shouldBlock) {
                            val blockedList = db.blockedNumberDao().getAllList()
                            val matched = blockedList.any { blockedItem ->
                                PhoneNumberUtils.matchesPattern(e164Number, blockedItem.numberPattern, primaryCountryIso)
                            }
                            if (matched) {
                                shouldBlock = true
                                blockReason = BlockReason.BLACK_LIST
                            }
                        }

                        // Auto-add to Block List
                        if (shouldBlock && autoBlockRejectedSetting) {
                            val existing = db.blockedNumberDao().getByPattern(e164Number)
                            if (existing == null) {
                                db.blockedNumberDao().insert(
                                    BlockedNumber(
                                        numberPattern = e164Number,
                                        note = applicationContext.getString(R.string.note_auto_blocked)
                                    )
                                )
                            }
                        }
                    }
                }

                val savedNumber = if (rawNumber.isNullOrBlank()) "UNKNOWN" else PhoneNumberUtils.formatToE164(rawNumber, primaryCountryIso)

                if (shouldBlock) {
                    val now = System.currentTimeMillis()
                    // Save history record for blocked call ONLY
                    db.callHistoryDao().insert(
                        CallHistory(
                            incomingNumber = savedNumber,
                            callTime = now,
                            isBlocked = true,
                            blockReason = blockReason
                        )
                    )
                    // Save persistent analytics event
                    db.blockAnalyticsDao().insert(
                        BlockAnalytics(
                            timestamp = now,
                            blockReason = blockReason
                        )
                    )
                    NotificationHelper.showBlockedCallNotification(applicationContext, savedNumber)
                }

                // Respond to Android Telecom Framework based on block action
                response = CallResponse.Builder().apply {
                    if (shouldBlock) {
                        setDisallowCall(true)
                        setSkipCallLog(false)
                        setSkipNotification(true)

                        if (blockAction == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            setRejectCall(false)
                            setSilenceCall(true)
                        } else {
                            setRejectCall(true)
                        }
                    } else {
                        setDisallowCall(false)
                        setRejectCall(false)
                    }
                }.build()

            } catch (_: Exception) {
                // Fallback to allow call on error
            } finally {
                val finalResponse = response ?: CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .build()
                try {
                    respondToCall(callDetails, finalResponse)
                } catch (_: Exception) {
                    // Safe response fallback
                }
            }
        }
    }
}
