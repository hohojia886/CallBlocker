/**
 * Entity representing a blocked phone number or wildcard pattern rule.
 */
package io.github.hohojia886.callblocker.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_number")
data class BlockedNumber(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "number_pattern")
    val numberPattern: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "note")
    val note: String? = null
)
