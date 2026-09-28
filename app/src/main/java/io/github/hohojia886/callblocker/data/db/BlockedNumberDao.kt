/**
 * Data Access Object (DAO) for blocked numbers table operations.
 */
package io.github.hohojia886.callblocker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNumberDao {
    /** Returns a Flow emitting the complete list of blocked numbers. */
    @Query("SELECT * FROM blocked_number ORDER BY created_at DESC")
    fun getAll(): Flow<List<BlockedNumber>>

    /** Filters blocked numbers by matching pattern or note using SQLite LIKE operator. */
    @Query("SELECT * FROM blocked_number WHERE number_pattern LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun search(query: String): Flow<List<BlockedNumber>>

    /** Retrieves all blocked numbers as a list. */
    @Query("SELECT * FROM blocked_number ORDER BY created_at DESC")
    suspend fun getAllList(): List<BlockedNumber>

    /** Finds a blocked number entry matching the specified pattern string. */
    @Query("SELECT * FROM blocked_number WHERE number_pattern = :pattern LIMIT 1")
    suspend fun getByPattern(pattern: String): BlockedNumber?

    /** Inserts a new blocked number into the database. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedNumber: BlockedNumber): Long

    /** Inserts a list of blocked numbers in bulk. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(blockedNumbers: List<BlockedNumber>)

    /** Updates an existing blocked number entry. */
    @Update
    suspend fun update(blockedNumber: BlockedNumber)

    /** Updates a list of blocked numbers in bulk. */
    @Update
    suspend fun updateAll(blockedNumbers: List<BlockedNumber>)

    /** Deletes a blocked number from the database. */
    @Delete
    suspend fun delete(blockedNumber: BlockedNumber)

    /** Deletes a blocked number by its unique primary key ID. */
    @Query("DELETE FROM blocked_number WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Clears all blocked numbers from the database. */
    @Query("DELETE FROM blocked_number")
    suspend fun deleteAll()
}
