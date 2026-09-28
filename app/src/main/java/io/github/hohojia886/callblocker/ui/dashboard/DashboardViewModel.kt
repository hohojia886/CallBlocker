/**
 * ViewModel aggregating call blocking statistics for today, month, year, lifetime, and reasons from persistent analytics table.
 */
package io.github.hohojia886.callblocker.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.hohojia886.callblocker.data.db.AppDatabase
import io.github.hohojia886.callblocker.data.db.BlockReasonCount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val analyticsDao = db.blockAnalyticsDao()

    /** Calculates the timestamp for the start of today. */
    private fun getStartOfDayTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** Calculates the timestamp for the start of the current month. */
    private fun getStartOfMonthTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** Calculates the timestamp for the start of the current year. */
    private fun getStartOfYearTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** Flow emitting today's blocked call count, dynamically calculating start-of-day. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val todayCount: StateFlow<Int> = analyticsDao.getAll()
        .flatMapLatest { analyticsDao.getBlockedCountSince(getStartOfDayTimestamp()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Flow emitting this month's blocked call count, dynamically calculating start-of-month. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val thisMonthCount: StateFlow<Int> = analyticsDao.getAll()
        .flatMapLatest { analyticsDao.getBlockedCountSince(getStartOfMonthTimestamp()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Flow emitting this year's blocked call count, dynamically calculating start-of-year. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val thisYearCount: StateFlow<Int> = analyticsDao.getAll()
        .flatMapLatest { analyticsDao.getBlockedCountSince(getStartOfYearTimestamp()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Flow emitting total lifetime blocked call count. */
    val lifetimeCount: StateFlow<Int> = analyticsDao.getLifetimeBlockedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Flow emitting counts categorized by block reason. */
    val reasonCounts: StateFlow<List<BlockReasonCount>> = analyticsDao.getBlockedCountByReason()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
