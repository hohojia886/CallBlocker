/**
 * Data Access Object (DAO) for persistent block analytics aggregation.
 */
package io.github.hohojia886.callblocker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockAnalyticsDao {

    /** Inserts a new blocked call analytics event. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analytics: BlockAnalytics): Long

    /** Counts blocked call events since a specified timestamp. */
    @Query("SELECT COUNT(*) FROM block_analytics WHERE timestamp >= :startTime")
    fun getBlockedCountSince(startTime: Long): Flow<Int>

    /** Counts total lifetime blocked call events. */
    @Query("SELECT COUNT(*) FROM block_analytics")
    fun getLifetimeBlockedCount(): Flow<Int>

    /** Groups and counts blocked call events by interception reason. */
    @Query("SELECT block_reason AS reason, COUNT(*) AS count FROM block_analytics GROUP BY block_reason")
    fun getBlockedCountByReason(): Flow<List<BlockReasonCount>>

    /** Returns all analytics event timestamps and reasons. */
    @Query("SELECT * FROM block_analytics ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BlockAnalytics>>

    /** Clears all analytics events from the database during full reset. */
    @Query("DELETE FROM block_analytics")
    suspend fun deleteAll()
}
