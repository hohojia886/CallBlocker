/**
 * ViewModel managing block list search queries, rule additions, deduplication, and deletions.
 */
package io.github.hohojia886.callblocker.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.hohojia886.callblocker.data.db.AppDatabase
import io.github.hohojia886.callblocker.data.db.BlockedNumber
import io.github.hohojia886.callblocker.util.CountryCodeProvider
import io.github.hohojia886.callblocker.util.CountryInfo
import io.github.hohojia886.callblocker.util.PhoneNumberUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManageBlockListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.blockedNumberDao()

    private val _searchQuery = MutableStateFlow("")
    /** Current search query string state flow. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Flow emitting blocked numbers filtered by SQLite query in real-time. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredBlockedNumbers: StateFlow<List<BlockedNumber>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                dao.getAll()
            } else {
                dao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _inputPattern = MutableStateFlow("")
    /** State flow holding input pattern text. */
    val inputPattern: StateFlow<String> = _inputPattern.asStateFlow()

    private val _inputNote = MutableStateFlow("")
    /** State flow holding input note text. */
    val inputNote: StateFlow<String> = _inputNote.asStateFlow()

    private val _impactedCountries = MutableStateFlow<List<CountryInfo>>(emptyList())
    /** State flow holding countries affected by current input pattern. */
    val impactedCountries: StateFlow<List<CountryInfo>> = _impactedCountries.asStateFlow()

    /** Updates current search query filter string. */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Updates input number pattern and re-calculates impacted countries. */
    fun updateInputPattern(pattern: String) {
        _inputPattern.value = pattern
        _impactedCountries.value = CountryCodeProvider.getMatchingCountries(pattern)
    }

    /** Updates input note description. */
    fun updateInputNote(note: String) {
        _inputNote.value = note
    }

    /** Fills input fields with phone number and contact name from contact picker. */
    fun setContactDetails(phoneNumber: String, contactName: String) {
        _inputPattern.value = phoneNumber
        _inputNote.value = contactName
        _impactedCountries.value = CountryCodeProvider.getMatchingCountries(phoneNumber)
    }

    /** Adds new rule or updates existing entry note with deduplication handling. */
    fun addBlockedNumber() {
        val pattern = _inputPattern.value.trim()
        if (pattern.isBlank()) return

        val formattedPattern = PhoneNumberUtils.formatToE164(pattern)
        val note = _inputNote.value.trim().ifBlank { null }

        viewModelScope.launch {
            val existingItem = dao.getByPattern(formattedPattern)
            if (existingItem != null) {
                dao.update(existingItem.copy(note = note))
            } else {
                dao.insert(
                    BlockedNumber(
                        numberPattern = formattedPattern,
                        note = note
                    )
                )
            }
            _inputPattern.value = ""
            _inputNote.value = ""
            _impactedCountries.value = emptyList()
        }
    }

    /** Deletes specified blocked number rule entry. */
    fun deleteBlockedNumber(item: BlockedNumber) {
        viewModelScope.launch {
            dao.delete(item)
        }
    }
}
