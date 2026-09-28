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

        assertTrue(PhoneNumberUtils.matchesPattern(incoming, exactPattern))
        assertTrue(PhoneNumberUtils.matchesPattern(incoming, wildcardPattern))
        assertFalse(PhoneNumberUtils.matchesPattern(incoming, nonMatchPattern))
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
}
