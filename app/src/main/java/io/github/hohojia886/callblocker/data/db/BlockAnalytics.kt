/**
 * Entity representing an immutable blocked call analytics event for persistent dashboard metrics.
 */
package io.github.hohojia886.callblocker.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_analytics")
data class BlockAnalytics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "block_reason")
    val blockReason: BlockReason
)
