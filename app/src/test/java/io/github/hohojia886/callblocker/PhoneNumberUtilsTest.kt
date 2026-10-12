package io.github.hohojia886.callblocker

import io.github.hohojia886.callblocker.util.PhoneNumberUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberUtilsTest {

    @Test
    fun testFormatToE164_TaiwanLocalNumber() {
        val raw = "0912345678"
        val formatted = PhoneNumberUtils.formatToE164(raw, "TW")
        assertEquals("+886912345678", formatted)
    }

    @Test
    fun testFormatToE164_WildcardPattern() {
        val raw = "+886912*"
        val formatted = PhoneNumberUtils.formatToE164(raw, "TW")
        assertEquals("+886912*", formatted)
    }

    @Test
    fun testFormatToE164_NullAndBlank() {
        assertEquals("UNKNOWN", PhoneNumberUtils.formatToE164(null))
        assertEquals("UNKNOWN", PhoneNumberUtils.formatToE164(""))
        assertEquals("UNKNOWN", PhoneNumberUtils.formatToE164("   "))
    }

    @Test
    fun testMatchesPattern_ExactAndWildcard() {
        val incoming = "0912345678"
        val exactPattern = "+886912345678"
        val wildcardPattern = "+886912*"
        val nonMatchPattern = "+886911*"

        assertTrue(PhoneNumberUtils.matchesPattern(incoming, exactPattern, "TW"))
        assertTrue(PhoneNumberUtils.matchesPattern(incoming, wildcardPattern, "TW"))
        assertFalse(PhoneNumberUtils.matchesPattern(incoming, nonMatchPattern, "TW"))
    }

    @Test
    fun testIsInternational() {
        val twNumber = "+886912345678"
        val usNumber = "+12125550199"

        assertFalse(PhoneNumberUtils.isInternational(twNumber, "TW"))
        assertTrue(PhoneNumberUtils.isInternational(usNumber, "TW"))
    }

    @Test
    fun testIsInternational_MultiSim() {
        val twNumber = "+886912345678"
        val usNumber = "+12125550199"
        val jpNumber = "+819012345678"

        val activeSimIsos = setOf("TW", "US")

        assertFalse(PhoneNumberUtils.isInternational(twNumber, activeSimIsos))
        assertFalse(PhoneNumberUtils.isInternational(usNumber, activeSimIsos))
        assertTrue(PhoneNumberUtils.isInternational(jpNumber, activeSimIsos))
    }

    @Test
    fun testFormatToDisplay_DomesticAndInternational() {
        val twE164 = "+886912345678"
        val usE164 = "+12125550199"
        val activeSimIsos = setOf("TW")

        // Domestic TW number on TW SIM with hideDomesticCountryCode = true -> NATIONAL format with hyphens
        val twDomestic = PhoneNumberUtils.formatToDisplay(twE164, activeSimIsos, hideDomesticCountryCode = true, defaultRegion = "TW")
        assertEquals("0912-345-678", twDomestic)

        // Domestic TW number on TW SIM with hideDomesticCountryCode = false -> INTERNATIONAL format with hyphens
        val twInternational = PhoneNumberUtils.formatToDisplay(twE164, activeSimIsos, hideDomesticCountryCode = false, defaultRegion = "TW")
        assertEquals("+886 912-345-678", twInternational)

        // Foreign US number on TW SIM -> INTERNATIONAL format with hyphens
        val usOnTwSim = PhoneNumberUtils.formatToDisplay(usE164, activeSimIsos, hideDomesticCountryCode = true, defaultRegion = "TW")
        assertEquals("+1 212-555-0199", usOnTwSim)

        // Foreign US number on US SIM -> NATIONAL format with hyphens
        val usOnUsSim = PhoneNumberUtils.formatToDisplay(usE164, setOf("US"), hideDomesticCountryCode = true, defaultRegion = "US")
        assertEquals("212-555-0199", usOnUsSim)
    }

    @Test
    fun testFormatToDisplay_WildcardPattern() {
        val twWildcard = "+886912*"
        val usWildcard = "+1212*"
        val activeSimIsos = setOf("TW")

        val formattedTwWildcard = PhoneNumberUtils.formatToDisplay(twWildcard, activeSimIsos, hideDomesticCountryCode = true, defaultRegion = "TW")
        assertEquals("0912-*", formattedTwWildcard)

        val formattedUsWildcard = PhoneNumberUtils.formatToDisplay(usWildcard, activeSimIsos, hideDomesticCountryCode = true, defaultRegion = "TW")
        assertEquals("+1 212-*", formattedUsWildcard)
    }

    @Test
    fun testFormatToDisplay_OtherCountries() {
        val jpE164 = "+819012345678"
        val deE164 = "+4930123456"
        val gbE164 = "+442079460912"

        // Japan domestic on JP SIM
        val jpDomestic = PhoneNumberUtils.formatToDisplay(jpE164, setOf("JP"), hideDomesticCountryCode = true, defaultRegion = "JP")
        assertEquals("090-1234-5678", jpDomestic)

        // Germany domestic on DE SIM
        val deDomestic = PhoneNumberUtils.formatToDisplay(deE164, setOf("DE"), hideDomesticCountryCode = true, defaultRegion = "DE")
        assertEquals("030-123456", deDomestic)

        // UK domestic on GB SIM
        val gbDomestic = PhoneNumberUtils.formatToDisplay(gbE164, setOf("GB"), hideDomesticCountryCode = true, defaultRegion = "GB")
        assertEquals("020-7946-0912", gbDomestic)
    }

    @Test
    fun testFormatToDisplay_NullAndUnknown() {
        assertEquals("UNKNOWN", PhoneNumberUtils.formatToDisplay(null))
        assertEquals("UNKNOWN", PhoneNumberUtils.formatToDisplay(""))
        assertEquals("UNKNOWN", PhoneNumberUtils.formatToDisplay("   "))
        assertEquals("UNKNOWN", PhoneNumberUtils.formatToDisplay("UNKNOWN"))
    }

    @Test
    fun testGetDefaultSystemRegion() {
        val region = PhoneNumberUtils.getDefaultSystemRegion()
        assertTrue(region.isNotBlank())
        assertEquals(region, region.uppercase())
    }
}
