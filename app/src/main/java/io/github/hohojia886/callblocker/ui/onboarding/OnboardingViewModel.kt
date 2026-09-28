/**
 * ViewModel managing onboarding completion status persistence.
 */
package io.github.hohojia886.callblocker.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.hohojia886.callblocker.data.pref.PreferencesManager
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)

    /** Marks the onboarding setup flow as completed in DataStore. */
    fun completeOnboarding(onCompleted: () -> Unit) {
        viewModelScope.launch {
            prefs.setOnboardingCompleted(true)
            onCompleted()
        }
    }
}
