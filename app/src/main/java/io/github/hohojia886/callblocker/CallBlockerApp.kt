/**
 * Application class for CallBlocker, initializing global components and background sync workers.
 */
package io.github.hohojia886.callblocker

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.hohojia886.callblocker.util.ContactsSyncWorker
import java.util.concurrent.TimeUnit

class CallBlockerApp : Application() {
    /** Initializes application components and schedules periodic background contact synchronization. */
    override fun onCreate() {
        super.onCreate()

        try {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<ContactsSyncWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "ContactsSyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        } catch (_: Exception) {
            // WorkManager initialization fallback
        }
    }
}
