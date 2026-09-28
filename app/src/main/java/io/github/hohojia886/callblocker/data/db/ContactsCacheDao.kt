/**
 * Data Access Object (DAO) for local contacts cache operations.
 */
package io.github.hohojia886.callblocker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ContactsCacheDao {
    /** Checks whether a normalized number exists in the cached contacts. */
    @Query("SELECT EXISTS(SELECT 1 FROM contacts_cache WHERE normalized_number = :number LIMIT 1)")
    suspend fun containsNumber(number: String): Boolean

    /** Retrieves contact cache details for a normalized number. */
    @Query("SELECT * FROM contacts_cache WHERE normalized_number = :number LIMIT 1")
    suspend fun getContact(number: String): ContactsCache?

    /** Inserts a list of contacts into the cache. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactsCache>)

    /** Clears all cached contact entries. */
    @Query("DELETE FROM contacts_cache")
    suspend fun deleteAll()

    /** Atomically replaces the entire contact cache in a single transaction. */
    @Transaction
    suspend fun replaceAll(contacts: List<ContactsCache>) {
        deleteAll()
        insertAll(contacts)
    }
}
