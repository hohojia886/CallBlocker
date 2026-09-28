/**
 * ViewModel managing call history logs query, single deletion, and bulk clearing operations.
 */
package io.github.hohojia886.callblocker.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.hohojia886.callblocker.data.db.AppDatabase
import io.github.hohojia886.callblocker.data.db.CallHistory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.callHistoryDao()

    /** Flow emitting the current list of call history records. */
    val historyList: StateFlow<List<CallHistory>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Deletes a single call history item. */
    fun deleteItem(callHistory: CallHistory) {
        viewModelScope.launch {
            dao.delete(callHistory)
        }
    }

    /** Clears all call history entries from the database. */
    fun clearAllHistory() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }
}
