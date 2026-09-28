package io.github.hohojia886.callblocker

import io.github.hohojia886.callblocker.util.CountryCodeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryCodeProviderTest {

    @Test
    fun testGetMatchingCountries_Wildcard8() {
        val matches = CountryCodeProvider.getMatchingCountries("+8*")
        val codes = matches.map { it.code }

        assertTrue(codes.contains("+886"))
        assertTrue(codes.contains("+86"))
        assertTrue(codes.contains("+81"))
        assertTrue(codes.contains("+82"))
        assertTrue(codes.contains("+852"))
    }

    @Test
    fun testGetMatchingCountries_SpecificTaiwan() {
        val matches = CountryCodeProvider.getMatchingCountries("+886")
        assertEquals(1, matches.size)
        assertEquals("+886", matches.first().code)
        assertEquals("Taiwan", matches.first().countryName)
    }

    @Test
    fun testGetMatchingCountries_InvalidInput_ReturnsEmpty() {
        val matches = CountryCodeProvider.getMatchingCountries("abc")
        assertTrue(matches.isEmpty())
    }
}
