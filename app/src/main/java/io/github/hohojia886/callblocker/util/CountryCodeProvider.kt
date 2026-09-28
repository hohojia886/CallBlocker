/**
 * Utility providing ITU-T country codes and dynamic impact analysis for wildcard rules.
 */
package io.github.hohojia886.callblocker.util

data class CountryInfo(
    val code: String,
    val countryName: String,
    val flagEmoji: String
)

object CountryCodeProvider {
    val countryList = listOf(
        CountryInfo("+886", "Taiwan", "🇹🇼"),
        CountryInfo("+86", "China", "🇨🇳"),
        CountryInfo("+81", "Japan", "🇯🇵"),
        CountryInfo("+82", "South Korea", "🇰🇷"),
        CountryInfo("+852", "Hong Kong", "🇭🇰"),
        CountryInfo("+853", "Macau", "🇲🇴"),
        CountryInfo("+1", "United States / Canada", "🇺🇸"),
        CountryInfo("+44", "United Kingdom", "🇬🇧"),
        CountryInfo("+49", "Germany", "🇩🇪"),
        CountryInfo("+33", "France", "🇫🇷"),
        CountryInfo("+39", "Italy", "🇮🇹"),
        CountryInfo("+34", "Spain", "🇪🇸"),
        CountryInfo("+61", "Australia", "🇦🇺"),
        CountryInfo("+64", "New Zealand", "🇳🇿"),
        CountryInfo("+65", "Singapore", "🇸🇬"),
        CountryInfo("+60", "Malaysia", "🇲🇾"),
        CountryInfo("+66", "Thailand", "🇹🇭"),
        CountryInfo("+62", "Indonesia", "🇮🇩"),
        CountryInfo("+63", "Philippines", "🇵🇭"),
        CountryInfo("+84", "Vietnam", "🇻🇳"),
        CountryInfo("+91", "India", "🇮🇳"),
        CountryInfo("+92", "Pakistan", "🇵🇰"),
        CountryInfo("+7", "Russia / Kazakhstan", "🇷🇺"),
        CountryInfo("+55", "Brazil", "🇧🇷"),
        CountryInfo("+52", "Mexico", "🇲🇽"),
        CountryInfo("+20", "Egypt", "🇪🇬"),
        CountryInfo("+27", "South Africa", "🇿🇦"),
        CountryInfo("+971", "United Arab Emirates", "🇦🇪"),
        CountryInfo("+966", "Saudi Arabia", "🇸🇦"),
        CountryInfo("+90", "Turkey", "🇹🇷")
    )

    fun getMatchingCountries(inputPattern: String): List<CountryInfo> {
        val trimmed = inputPattern.trim()
        if (!trimmed.startsWith("+") || trimmed.length < 2) {
            return emptyList()
        }

        val parts = trimmed.split("*")
        val regexString = "^" + parts.map { Regex.escape(it) }.joinToString(".*") + ".*$"
        val regex = try {
            Regex(regexString)
        } catch (_: Exception) {
            return emptyList()
        }

        return countryList.filter { country ->
            regex.matches(country.code) || country.code.startsWith(parts.first())
        }
    }
}
