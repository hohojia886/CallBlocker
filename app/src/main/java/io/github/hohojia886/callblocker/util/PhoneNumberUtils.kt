/**
 * Phone number parsing and matching utility supporting E.164 formatting and wildcard pattern rules.
 */
package io.github.hohojia886.callblocker.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.util.Locale

object PhoneNumberUtils {
    private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

    /**
     * Standardize input phone number to E.164 format.
     * Supports wildcard '*' characters (e.g. "+886912*" -> "+886912*").
     */
    fun formatToE164(rawNumber: String?, defaultRegion: String = "TW"): String {
        if (rawNumber.isNullOrBlank()) return "UNKNOWN"

        val trimmed = rawNumber.trim()
        val region = defaultRegion.uppercase(Locale.ROOT)

        if (trimmed.contains("*")) {
            val starIndex = trimmed.indexOf("*")
            val prefixPart = trimmed.substring(0, starIndex)
            val suffixWildcard = trimmed.substring(starIndex)

            if (prefixPart.isBlank()) return "*"

            val formattedPrefix = tryFormatE164(prefixPart, region)
            return formattedPrefix + suffixWildcard
        }

        return tryFormatE164(trimmed, region)
    }

    private fun tryFormatE164(numberStr: String, region: String): String {
        return try {
            val proto: Phonenumber.PhoneNumber = phoneUtil.parse(numberStr, region)
            if (phoneUtil.isValidNumber(proto)) {
                phoneUtil.format(proto, PhoneNumberUtil.PhoneNumberFormat.E164)
            } else {
                cleanFallbackNumber(numberStr)
            }
        } catch (_: Exception) {
            cleanFallbackNumber(numberStr)
        }
    }

    private fun cleanFallbackNumber(numberStr: String): String {
        val cleaned = numberStr.replace("[^0-9+]".toRegex(), "")
        return if (cleaned.startsWith("+")) cleaned else "+$cleaned"
    }

    /**
     * Check if an incoming E.164 number matches a stored pattern.
     * Stored pattern can be an exact E.164 number or contain wildcard '*' (e.g. "+886912*").
     */
    fun matchesPattern(incomingNumber: String, pattern: String, defaultRegion: String = "TW"): Boolean {
        if (pattern.isBlank()) return false
        val normalizedIncoming = formatToE164(incomingNumber, defaultRegion)
        val normalizedPattern = formatToE164(pattern, defaultRegion)

        if (normalizedPattern.contains("*")) {
            val parts = normalizedPattern.split("*")
            val regexString = "^" + parts.map { Regex.escape(it) }.joinToString(".*") + "$"
            val regex = Regex(regexString)
            return normalizedIncoming.matches(regex)
        }

        return normalizedIncoming.equals(normalizedPattern, ignoreCase = true)
    }

    /**
     * Check if a number is international relative to the set of active SIM country ISOs.
     * Returns true if the number's region matches NONE of the active SIM country ISOs.
     */
    fun isInternational(incomingNumber: String, userRegionIsos: Set<String>): Boolean {
        if (userRegionIsos.isEmpty()) return false

        val defaultRegion = userRegionIsos.firstOrNull() ?: "TW"
        val formatted = formatToE164(incomingNumber, defaultRegion)
        if (!formatted.startsWith("+")) return false

        return try {
            val proto = phoneUtil.parse(formatted, defaultRegion.uppercase(Locale.ROOT))
            val numberRegion = phoneUtil.getRegionCodeForNumber(proto)
            if (numberRegion != null) {
                val isLocalToAnySim = userRegionIsos.any { simIso ->
                    numberRegion.equals(simIso, ignoreCase = true)
                }
                !isLocalToAnySim
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /** Overload for single region string. */
    fun isInternational(incomingNumber: String, userRegionIso: String): Boolean {
        return isInternational(incomingNumber, setOf(userRegionIso))
    }

    /** Queries active subscription info to obtain country ISO codes for all installed SIM cards. */
    fun getActiveSimCountryIsos(context: Context): Set<String> {
        val countryIsos = mutableSetOf<String>()

        val hasReadPhoneState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasReadPhoneState) {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            if (subscriptionManager != null) {
                try {
                    val activeList = subscriptionManager.activeSubscriptionInfoList
                    if (!activeList.isNullOrEmpty()) {
                        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                        for (info in activeList) {
                            val iso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                info.countryIso
                            } else {
                                val subTelephony = telephonyManager?.createForSubscriptionId(info.subscriptionId)
                                subTelephony?.simCountryIso ?: telephonyManager?.simCountryIso
                            }
                            if (!iso.isNullOrBlank()) {
                                countryIsos.add(iso.uppercase(Locale.ROOT))
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Subscription query fallback
                }
            }
        }

        if (countryIsos.isEmpty()) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val simIso = telephonyManager?.simCountryIso
            val networkIso = telephonyManager?.networkCountryIso
            when {
                !simIso.isNullOrBlank() -> countryIsos.add(simIso.uppercase(Locale.ROOT))
                !networkIso.isNullOrBlank() -> countryIsos.add(networkIso.uppercase(Locale.ROOT))
                else -> countryIsos.add("TW")
            }
        }

        return countryIsos
    }

    /** Helper retrieving primary active SIM country ISO, defaulting to "TW" if unavailable. */
    fun getPrimarySimCountryIso(context: Context): String {
        return getActiveSimCountryIsos(context).firstOrNull() ?: "TW"
    }
}
