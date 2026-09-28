/**
 * Onboarding screen displaying language selection, privacy agreement, permissions, and screening role setup.
 */
package io.github.hohojia886.callblocker.ui.onboarding

import android.Manifest
import android.app.role.RoleManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hohojia886.callblocker.R
import io.github.hohojia886.callblocker.ui.settings.ALL_LANGUAGE_OPTIONS

/** Main onboarding container screen orchestrating multi-step setup. */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val context = LocalContext.current

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.completeOnboarding(onComplete)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            currentStep = 3
        } else {
            Toast.makeText(context, context.getString(R.string.toast_permissions_required), Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            when (currentStep) {
                1 -> Step1WelcomeAndLanguage(onNext = { currentStep = 2 })
                2 -> Step2Permissions(
                    onRequestPermissions = {
                        val permissionsToRequest = mutableListOf(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_CALL_LOG
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                    },
                    onSkip = { currentStep = 3 }
                )
                3 -> Step3CallScreeningRole(
                    onRequestRole = {
                        try {
                            val roleManager = context.getSystemService(RoleManager::class.java)
                            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                                    roleLauncher.launch(intent)
                                } else {
                                    viewModel.completeOnboarding(onComplete)
                                }
                            } else {
                                viewModel.completeOnboarding(onComplete)
                            }
                        } catch (_: Exception) {
                            viewModel.completeOnboarding(onComplete)
                        }
                    },
                    onSkip = { viewModel.completeOnboarding(onComplete) }
                )
            }
        }
    }
}

/** Step 1: Welcome title, privacy statement, and language selection. */
@Composable
fun Step1WelcomeAndLanguage(onNext: () -> Unit) {
    Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = null,
        modifier = Modifier.size(80.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.onboarding_welcome_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_welcome_desc),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(32.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_privacy_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.onboarding_privacy_desc),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Language Selection Inline
    val currentTag = getCurrentLanguageTag()
    var isExpanded by remember { mutableStateOf(false) }

    val currentLabel = ALL_LANGUAGE_OPTIONS.firstOrNull { (tag, _) ->
        when {
            tag.isEmpty() && currentTag.isEmpty() -> true
            tag.isNotEmpty() && currentTag.startsWith(tag, ignoreCase = true) -> true
            tag == "zh-TW" && (currentTag.startsWith("zh-HK", ignoreCase = true) || currentTag.startsWith("zh-Hant", ignoreCase = true)) -> true
            else -> false
        }
    }?.second ?: stringResource(R.string.label_system_default)

    OutlinedButton(
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Language, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = currentLabel)
    }

    if (isExpanded) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ALL_LANGUAGE_OPTIONS.forEach { (tag, label) ->
                    val isSelected = when {
                        tag.isEmpty() && currentTag.isEmpty() -> true
                        tag.isNotEmpty() && currentTag.startsWith(tag, ignoreCase = true) -> true
                        tag == "zh-TW" && (currentTag.startsWith("zh-HK", ignoreCase = true) || currentTag.startsWith("zh-Hant", ignoreCase = true)) -> true
                        else -> false
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                setAppLanguage(tag)
                                isExpanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.onboarding_btn_next))
    }
}

/** Step 2: Runtime permissions request prompt. */
@Composable
fun Step2Permissions(onRequestPermissions: () -> Unit, onSkip: () -> Unit) {
    Icon(
        imageVector = Icons.Default.VpnKey,
        contentDescription = null,
        modifier = Modifier.size(80.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.onboarding_permission_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_permission_desc),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(48.dp))
    Button(
        onClick = onRequestPermissions,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.onboarding_btn_grant_permissions))
    }
    TextButton(onClick = onSkip) {
        Text(stringResource(R.string.btn_skip_for_now))
    }
}

/** Step 3: Call Screening role request prompt. */
@Composable
fun Step3CallScreeningRole(onRequestRole: () -> Unit, onSkip: () -> Unit) {
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        modifier = Modifier.size(80.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.onboarding_role_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_role_desc),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(48.dp))
    Button(
        onClick = onRequestRole,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.onboarding_btn_grant_role))
    }
    TextButton(onClick = onSkip) {
        Text(stringResource(R.string.btn_skip_for_now))
    }
}

/** Applies application locales for in-app language switching. */
private fun setAppLanguage(languageTag: String) {
    val appLocales = if (languageTag.isBlank()) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(languageTag)
    }
    AppCompatDelegate.setApplicationLocales(appLocales)
}

/** Retrieves current application language tag. */
private fun getCurrentLanguageTag(): String {
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    return if (!currentLocales.isEmpty) {
        currentLocales.toLanguageTags()
    } else {
        ""
    }
}
