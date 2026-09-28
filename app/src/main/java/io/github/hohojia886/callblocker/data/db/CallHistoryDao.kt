/**
 * Data Access Object (DAO) for call history logs and analytics aggregation.
 */
package io.github.hohojia886.callblocker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data class holding count statistics for a specific block reason. */
data class BlockReasonCount(
    val reason: BlockReason,
    val count: Int
)

@Dao
interface CallHistoryDao {
    /** Returns a Flow emitting the complete call history list. */
    @Query("SELECT * FROM call_history ORDER BY call_time DESC")
    fun getAll(): Flow<List<CallHistory>>

    /** Retrieves all call history records as a list. */
    @Query("SELECT * FROM call_history ORDER BY call_time DESC")
    suspend fun getAllList(): List<CallHistory>

    /** Inserts a new call history log entry. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callHistory: CallHistory): Long

    /** Inserts a list of call history entries in bulk. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(callHistories: List<CallHistory>)

    /** Deletes a call history record. */
    @Delete
    suspend fun delete(callHistory: CallHistory)

    /** Clears all call history entries from the database. */
    @Query("DELETE FROM call_history")
    suspend fun deleteAll()

    /** Counts total blocked calls since a specified timestamp. */
    @Query("SELECT COUNT(*) FROM call_history WHERE is_blocked != 0 AND is_blocked IS NOT NULL AND call_time >= :startTime")
    fun getBlockedCountSince(startTime: Long): Flow<Int>

    /** Counts the total lifetime blocked calls. */
    @Query("SELECT COUNT(*) FROM call_history WHERE is_blocked != 0 AND is_blocked IS NOT NULL")
    fun getLifetimeBlockedCount(): Flow<Int>

    /** Groups and counts blocked calls by their interception reason. */
    @Query("SELECT block_reason AS reason, COUNT(*) AS count FROM call_history WHERE is_blocked != 0 AND is_blocked IS NOT NULL AND block_reason != 'NONE' GROUP BY block_reason")
    fun getBlockedCountByReason(): Flow<List<BlockReasonCount>>
}
