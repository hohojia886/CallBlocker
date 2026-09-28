/**
 * Main Activity managing window content, notification channels, and onboarding flow.
 */
package io.github.hohojia886.callblocker.ui

import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.hohojia886.callblocker.data.pref.PreferencesManager
import io.github.hohojia886.callblocker.ui.onboarding.OnboardingScreen
import io.github.hohojia886.callblocker.ui.theme.CallBlockerTheme
import io.github.hohojia886.callblocker.util.NotificationHelper

class MainActivity : AppCompatActivity() {

    private val requestDialerRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    private val requestCallScreeningRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    /** Initializes the activity, notification channel, and Compose UI content tree. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferencesManager(applicationContext)

        try {
            NotificationHelper.createNotificationChannel(applicationContext)
        } catch (_: Exception) {
            // Notification channel initialization fallback
        }

        setContent {
            CallBlockerTheme {
                val isOnboardingCompleted by prefs.isOnboardingCompleted.collectAsState(initial = false)

                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        onComplete = {
                            // Onboarding completed
                        }
                    )
                } else {
                    MainScreen(
                        onRequestDialerRole = { requestDialerRole() },
                        onRequestCallScreeningRole = { requestCallScreeningRole() }
                    )
                }
            }
        }
    }

    /** Prompts system role manager to request default dialer app status on Android 10+. */
    private fun requestDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    requestDialerRoleLauncher.launch(intent)
                }
            } catch (_: Exception) {
                // Role request fallback
            }
        }
    }

    /** Prompts system role manager to request default call screening app status on Android 10+. */
    fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                    requestCallScreeningRoleLauncher.launch(intent)
                }
            } catch (_: Exception) {
                // Role request fallback
            }
        }
    }
}
