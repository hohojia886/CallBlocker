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
     * Obtains default region ISO from system locale, defaulting to "US" if unavailable.
     */
    fun getDefaultSystemRegion(): String {
        val country = Locale.getDefault().country
        return if (!country.isNullOrBlank()) country.uppercase(Locale.ROOT) else "US"
    }

    /**
     * Standardize input phone number to E.164 format.
     * Supports wildcard '*' characters (e.g. "+886912*" -> "+886912*").
     */
    fun formatToE164(rawNumber: String?, defaultRegion: String = getDefaultSystemRegion()): String {
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
     * Formats input number or wildcard pattern for user-friendly UI display.
     * Separates country code prefix with space and normalizes national digit blocks with hyphens.
     * If [hideDomesticCountryCode] is true and number region matches active SIM ISO [userRegionIsos],
     * formats in NATIONAL style (e.g. "0912-345-678" or "415-555-2671").
     * Otherwise formats in INTERNATIONAL style (e.g. "+886 912-345-678" or "+1 415-555-2671").
     */
    fun formatToDisplay(
        rawNumber: String?,
        userRegionIsos: Set<String> = emptySet(),
        hideDomesticCountryCode: Boolean = true,
        defaultRegion: String = getDefaultSystemRegion()
    ): String {
        if (rawNumber.isNullOrBlank()) return "UNKNOWN"

        val trimmed = rawNumber.trim()
        if (trimmed.equals("UNKNOWN", ignoreCase = true)) return "UNKNOWN"
        val region = defaultRegion.uppercase(Locale.ROOT)

        if (trimmed.contains("*")) {
            val starIndex = trimmed.indexOf("*")
            val prefixPart = trimmed.substring(0, starIndex)
            val suffixWildcard = trimmed.substring(starIndex)

            if (prefixPart.isBlank()) return "*"

            val formattedPrefix = tryFormatDisplay(prefixPart, userRegionIsos, hideDomesticCountryCode, region)
            val separator = if (formattedPrefix.isNotEmpty() && formattedPrefix.last().isDigit()) "-" else ""
            return formattedPrefix + separator + suffixWildcard
        }

        return tryFormatDisplay(trimmed, userRegionIsos, hideDomesticCountryCode, region)
    }

    /** Overload for single region string. */
    fun formatToDisplay(
        rawNumber: String?,
        userRegionIso: String,
        hideDomesticCountryCode: Boolean = true
    ): String {
        return formatToDisplay(rawNumber, setOf(userRegionIso), hideDomesticCountryCode, userRegionIso)
    }

    private fun tryFormatDisplay(
        numberStr: String,
        userRegionIsos: Set<String>,
        hideDomesticCountryCode: Boolean,
        defaultRegion: String
    ): String {
        return try {
            val proto: Phonenumber.PhoneNumber = phoneUtil.parse(numberStr, defaultRegion)
            val numberRegion = phoneUtil.getRegionCodeForNumber(proto)

            val effectiveUserIsos = if (userRegionIsos.isNotEmpty()) {
                userRegionIsos.map { it.uppercase(Locale.ROOT) }.toSet()
            } else {
                setOf(defaultRegion.uppercase(Locale.ROOT))
            }

            val isDomestic = numberRegion != null && effectiveUserIsos.contains(numberRegion.uppercase(Locale.ROOT))

            var rawFormatted = if (isDomestic && hideDomesticCountryCode) {
                phoneUtil.format(proto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
            } else {
                phoneUtil.format(proto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            }

            // Universal national trunk prefix handling for partial/wildcard domestic numbers across all regions
            if (isDomestic && hideDomesticCountryCode) {
                // Ensure national trunk prefix if needed for partial numbers
                val exampleNumber = phoneUtil.getExampleNumber(numberRegion)
                if (exampleNumber != null) {
                    val exampleNational = phoneUtil.format(exampleNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
                    val digitIndex = exampleNational.indexOfFirst { it.isDigit() }
                    if (digitIndex >= 0 && exampleNational[digitIndex] == '0' && !rawFormatted.startsWith("0")) {
                        rawFormatted = "0$rawFormatted"
                    }
                }
            }

            formatWithHyphens(rawFormatted)
        } catch (_: Exception) {
            numberStr
        }
    }

    /**
     * Converts formatted number so country code (e.g. "+886" / "+1") is followed by a space,
     * and space separators within the national number part are converted to hyphens "-".
     * Also strips surrounding parentheses from national format like "(415) 555-2671" -> "415-555-2671".
     */
    private fun formatWithHyphens(formattedStr: String): String {
        val str = formattedStr.trim()
        if (str.startsWith("+")) {
            val spaceIndex = str.indexOf(" ")
            if (spaceIndex > 0) {
                val countryCodePart = str.substring(0, spaceIndex)
                val nationalPart = str.substring(spaceIndex + 1)
                    .replace("(", "")
                    .replace(")", "")
                    .replace(" ", "-")
                    .replace("--", "-")
                    .trim('-')
                return "$countryCodePart $nationalPart"
            }
        }

        return str
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "-")
            .replace("--", "-")
            .trim('-')
    }

    /**
     * Check if an incoming E.164 number matches a stored pattern.
     * Stored pattern can be an exact E.164 number or contain wildcard '*' (e.g. "+886912*").
     */
    fun matchesPattern(incomingNumber: String, pattern: String, defaultRegion: String = getDefaultSystemRegion()): Boolean {
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

        val defaultRegion = userRegionIsos.firstOrNull() ?: getDefaultSystemRegion()
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
                else -> countryIsos.add(getDefaultSystemRegion())
            }
        }

        return countryIsos
    }

    /** Helper retrieving primary active SIM country ISO, defaulting to system locale region if unavailable. */
    fun getPrimarySimCountryIso(context: Context): String {
        return getActiveSimCountryIsos(context).firstOrNull() ?: getDefaultSystemRegion()
    }
}
