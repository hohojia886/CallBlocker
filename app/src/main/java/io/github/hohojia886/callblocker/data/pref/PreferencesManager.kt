/**
 * DataStore preferences manager for app settings, rules, and onboarding state.
 */
package io.github.hohojia886.callblocker.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_pref")

class PreferencesManager(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        val BLOCK_UNKNOWN = booleanPreferencesKey("block_unknown_numbers")
        val BLOCK_NON_CONTACTS = booleanPreferencesKey("block_non_contacts")
        val BLOCK_INTERNATIONAL = booleanPreferencesKey("block_international_calls")
        val AUTO_BLOCK_REJECTED = booleanPreferencesKey("auto_block_rejected_calls")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val BLOCK_ACTION = intPreferencesKey("block_action")
        val OUTGOING_CALLBACK_EXEMPTION_DAYS = intPreferencesKey("outgoing_callback_exemption_days")
    }

    /** Flow emitting whether unknown/private number blocking is enabled. */
    val blockUnknown: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[BLOCK_UNKNOWN] ?: false
    }

    /** Flow emitting whether non-contact blocking is enabled. */
    val blockNonContacts: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[BLOCK_NON_CONTACTS] ?: false
    }

    /** Flow emitting whether international call blocking is enabled. */
    val blockInternational: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[BLOCK_INTERNATIONAL] ?: false
    }

    /** Flow emitting whether auto-adding rejected calls to block list is enabled. */
    val autoBlockRejected: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[AUTO_BLOCK_REJECTED] ?: false
    }

    /** Flow emitting whether the onboarding flow has been completed. */
    val isOnboardingCompleted: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[IS_ONBOARDING_COMPLETED] ?: false
    }

    /** Flow emitting the interception action mode (0 = Reject, 1 = Silence). */
    val blockAction: Flow<Int> = appContext.dataStore.data.map { preferences ->
        preferences[BLOCK_ACTION] ?: 0
    }

    /** Flow emitting the outgoing callback exemption window in days (0 = Off, 1 = 1 Day, 3 = 3 Days, 7 = 1 Week). */
    val outgoingCallbackExemptionDays: Flow<Int> = appContext.dataStore.data.map { preferences ->
        preferences[OUTGOING_CALLBACK_EXEMPTION_DAYS] ?: 0
    }

    /** Updates the unknown/private number blocking preference. */
    suspend fun setBlockUnknown(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[BLOCK_UNKNOWN] = enabled
        }
    }

    /** Updates the non-contact blocking preference. */
    suspend fun setBlockNonContacts(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[BLOCK_NON_CONTACTS] = enabled
        }
    }

    /** Updates the international call blocking preference. */
    suspend fun setBlockInternational(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[BLOCK_INTERNATIONAL] = enabled
        }
    }

    /** Updates the auto-add rejected calls preference. */
    suspend fun setAutoBlockRejected(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[AUTO_BLOCK_REJECTED] = enabled
        }
    }

    /** Updates the onboarding completion status. */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETED] = completed
        }
    }

    /** Updates the interception action mode (0 = Reject, 1 = Silence). */
    suspend fun setBlockAction(action: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[BLOCK_ACTION] = action
        }
    }

    /** Updates the outgoing callback exemption window in days. */
    suspend fun setOutgoingCallbackExemptionDays(days: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[OUTGOING_CALLBACK_EXEMPTION_DAYS] = days
        }
    }
}
