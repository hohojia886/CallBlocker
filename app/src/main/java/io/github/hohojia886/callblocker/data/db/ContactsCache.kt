/**
 * Entity representing a cached contact number for fast local lookup during call screening.
 */
package io.github.hohojia886.callblocker.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts_cache")
data class ContactsCache(
    @PrimaryKey
    @ColumnInfo(name = "normalized_number")
    val normalizedNumber: String,
    
    @ColumnInfo(name = "display_name")
    val displayName: String? = null
)
