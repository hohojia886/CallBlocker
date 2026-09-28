/**
 * Entity representing an incoming call log entry and its interception status.
 */
package io.github.hohojia886.callblocker.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_history")
data class CallHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "incoming_number")
    val incomingNumber: String,

    @ColumnInfo(name = "call_time")
    val callTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_blocked")
    val isBlocked: Boolean,

    @ColumnInfo(name = "block_reason")
    val blockReason: BlockReason = BlockReason.NONE
)
