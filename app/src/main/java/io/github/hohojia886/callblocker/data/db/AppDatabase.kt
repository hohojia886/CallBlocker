/**
 * Room database class managing blocked numbers, call history, contacts cache, and persistent analytics entities.
 */
package io.github.hohojia886.callblocker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BlockedNumber::class, CallHistory::class, ContactsCache::class, BlockAnalytics::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /** Returns the DAO for blocked number operations. */
    abstract fun blockedNumberDao(): BlockedNumberDao

    /** Returns the DAO for call history operations. */
    abstract fun callHistoryDao(): CallHistoryDao

    /** Returns the DAO for contacts cache operations. */
    abstract fun contactsCacheDao(): ContactsCacheDao

    /** Returns the DAO for persistent block analytics operations. */
    abstract fun blockAnalyticsDao(): BlockAnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Retrieves or initializes the singleton instance of the AppDatabase. */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "call_blocker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
