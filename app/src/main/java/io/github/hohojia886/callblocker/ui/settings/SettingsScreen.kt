/**
 * Protection and system settings screen providing rule toggles, i18n language picker, and backup/restore controls.
 */
package io.github.hohojia886.callblocker.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hohojia886.callblocker.R
import kotlin.math.roundToInt

/** List of supported international language options and display labels. */
val ALL_LANGUAGE_OPTIONS = listOf(
    "" to "System Default",
    "zh-TW" to "繁體中文 (Traditional Chinese)",
    "zh-CN" to "简体中文 (Simplified Chinese)",
    "en" to "English",
    "ja" to "日本語",
    "ko" to "한국어",
    "es" to "Español",
    "fr" to "Français",
    "de" to "Deutsch",
    "it" to "Italiano",
    "pt" to "Português",
    "ru" to "Русский",
    "vi" to "Tiếng Việt",
    "th" to "ไทย",
    "in" to "Bahasa Indonesia"
)

/** Main settings composable screen displaying configuration categories and dialogs. */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateToManageBlockList: () -> Unit,
    onRequestDialerRole: () -> Unit = {}
) {
    val context = LocalContext.current

    val blockUnknown by viewModel.blockUnknown.collectAsStateWithLifecycle()
    val blockNonContacts by viewModel.blockNonContacts.collectAsStateWithLifecycle()
    val blockInternational by viewModel.blockInternational.collectAsStateWithLifecycle()
    val autoBlockRejected by viewModel.autoBlockRejected.collectAsStateWithLifecycle()
    val outgoingExemptionDays by viewModel.outgoingCallbackExemptionDays.collectAsStateWithLifecycle()
    val blockAction by viewModel.blockAction.collectAsStateWithLifecycle()

    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var showResetDashboardDialog by remember { mutableStateOf(false) }
    var showResetHistoryDialog by remember { mutableStateOf(false) }
    var showResetDatabaseDialog by remember { mutableStateOf(false) }
    var showDialerRoleDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var lastImportedCount by remember { mutableIntStateOf(0) }

    var exportPassword by remember { mutableStateOf("") }
    var restorePassword by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            viewModel.exportEncryptedBackup(uri, exportPassword) { success, errorMsg ->
                if (success) {
                    Toast.makeText(context, context.getString(R.string.toast_backup_created), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_error, errorMsg ?: ""), Toast.LENGTH_LONG).show()
                }
                exportPassword = ""
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreEncryptedBackup(uri, restorePassword) { success, errorMsg ->
                if (success) {
                    Toast.makeText(context, context.getString(R.string.toast_restore_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_error, errorMsg ?: ""), Toast.LENGTH_LONG).show()
                }
                restorePassword = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. App Language Picker (At the Very Top)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.section_language_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val currentTag = getCurrentLanguageTag()
                val currentLabel = ALL_LANGUAGE_OPTIONS.firstOrNull { (tag, _) ->
                    when {
                        tag.isEmpty() && currentTag.isEmpty() -> true
                        tag.isNotEmpty() && currentTag.startsWith(tag, ignoreCase = true) -> true
                        tag == "zh-TW" && (currentTag.startsWith("zh-HK", ignoreCase = true) || currentTag.startsWith("zh-Hant", ignoreCase = true)) -> true
                        else -> false
                    }
                }?.second ?: stringResource(R.string.label_system_default)

                OutlinedButton(
                    onClick = { showLanguageDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.label_app_language))
                        Text(
                            text = currentLabel,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Blocking Rules
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.section_basic_rules),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingSwitchRow(
                    title = stringResource(R.string.rule_block_unknown_title),
                    subtitle = stringResource(R.string.rule_block_unknown_sub),
                    checked = blockUnknown,
                    onCheckedChange = { viewModel.setBlockUnknown(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SettingSwitchRow(
                    title = stringResource(R.string.rule_block_non_contacts_title),
                    subtitle = stringResource(R.string.rule_block_non_contacts_sub),
                    checked = blockNonContacts,
                    onCheckedChange = { viewModel.setBlockNonContacts(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SettingSwitchRow(
                    title = stringResource(R.string.rule_block_international_title),
                    subtitle = stringResource(R.string.rule_block_international_sub),
                    checked = blockInternational,
                    onCheckedChange = { viewModel.setBlockInternational(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SettingSwitchRow(
                    title = stringResource(R.string.rule_auto_block_rejected_title),
                    subtitle = stringResource(R.string.rule_auto_block_rejected_sub),
                    checked = autoBlockRejected,
                    onCheckedChange = { viewModel.setAutoBlockRejected(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Outgoing Callback Exemption Slider
                val currentSliderPosition = when (outgoingExemptionDays) {
                    1 -> 1f
                    3 -> 2f
                    7 -> 3f
                    else -> 0f
                }

                val currentLabel = when (outgoingExemptionDays) {
                    1 -> stringResource(R.string.option_exemption_1_day)
                    3 -> stringResource(R.string.option_exemption_3_days)
                    7 -> stringResource(R.string.option_exemption_1_week)
                    else -> stringResource(R.string.option_exemption_off)
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.rule_outgoing_exemption_title),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.rule_outgoing_exemption_sub),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = currentLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = currentSliderPosition,
                        onValueChange = { floatVal ->
                            val index = floatVal.roundToInt().coerceIn(0, 3)
                            val days = when (index) {
                                1 -> 1
                                2 -> 3
                                3 -> 7
                                else -> 0
                            }
                            viewModel.setOutgoingCallbackExemptionDays(days)
                        },
                        valueRange = 0f..3f,
                        steps = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringResource(R.string.option_exemption_off), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = stringResource(R.string.option_exemption_1_day), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = stringResource(R.string.option_exemption_3_days), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = stringResource(R.string.option_exemption_1_week), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 3. Interception Action
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.section_interception_action),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setBlockAction(0) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    RadioButton(
                        selected = blockAction == 0,
                        onClick = { viewModel.setBlockAction(0) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.action_reject_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.action_reject_sub),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setBlockAction(1) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    RadioButton(
                        selected = blockAction == 1,
                        onClick = { viewModel.setBlockAction(1) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.action_silence_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.action_silence_sub),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. Block List Management
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.section_block_list_management),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onNavigateToManageBlockList,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_manage_block_list))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.importSystemBlockList { result ->
                            lastImportedCount = result.count
                            if (result.isBlockedNumberContractRestricted) {
                                showDialerRoleDialog = true
                            } else if (result.errorMessage != null) {
                                Toast.makeText(context, result.errorMessage, Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, result.displaySummary, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_import_system_block_list))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.exportToSystemBlockList { result ->
                            if (result.isRoleRequired) {
                                showDialerRoleDialog = true
                            } else if (result.errorMessage != null) {
                                Toast.makeText(context, result.errorMessage, Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, result.displaySummary, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_export_system_block_list))
                }
            }
        }

        // 5. Data Management
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.section_data_management),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showExportPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_create_backup))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showRestorePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_restore_database))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showResetDashboardDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_reset_dashboard))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showResetHistoryDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_reset_history))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showResetDatabaseDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_reset_database))
                }
            }
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.section_language_settings)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val currentTag = getCurrentLanguageTag()

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
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    setAppLanguage(tag)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showDialerRoleDialog) {
        AlertDialog(
            onDismissRequest = { showDialerRoleDialog = false },
            title = { Text(stringResource(R.string.dialog_dialer_role_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.dialog_dialer_role_processed_count, lastImportedCount))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.dialog_dialer_role_unlock_header), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.dialog_dialer_role_unlock_msg))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(stringResource(R.string.dialog_dialer_role_tip), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialerRoleDialog = false
                        onRequestDialerRole()
                    }
                ) {
                    Text(stringResource(R.string.dialog_dialer_role_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialerRoleDialog = false }) {
                    Text(stringResource(R.string.btn_keep_as_is))
                }
            }
        )
    }

    if (showResetDashboardDialog) {
        AlertDialog(
            onDismissRequest = { showResetDashboardDialog = false },
            title = { Text(stringResource(R.string.dialog_reset_dashboard_title)) },
            text = {
                Text(stringResource(R.string.dialog_reset_dashboard_message))
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        showResetDashboardDialog = false
                        viewModel.resetDashboardStatistics {
                            Toast.makeText(context, context.getString(R.string.toast_dashboard_reset), Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDashboardDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showResetHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showResetHistoryDialog = false },
            title = { Text(stringResource(R.string.dialog_reset_history_title)) },
            text = {
                Text(stringResource(R.string.dialog_reset_history_message))
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        showResetHistoryDialog = false
                        viewModel.resetInterceptionHistory {
                            Toast.makeText(context, context.getString(R.string.toast_history_reset), Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetHistoryDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showResetDatabaseDialog) {
        AlertDialog(
            onDismissRequest = { showResetDatabaseDialog = false },
            title = { Text(stringResource(R.string.dialog_reset_title)) },
            text = {
                Text(stringResource(R.string.dialog_reset_message))
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        showResetDatabaseDialog = false
                        viewModel.resetDatabase {
                            Toast.makeText(context, context.getString(R.string.toast_reset_success), Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(stringResource(R.string.dialog_confirm_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDatabaseDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showExportPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showExportPasswordDialog = false },
            title = { Text(stringResource(R.string.dialog_backup_pwd_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.dialog_backup_pwd_msg))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportPassword,
                        onValueChange = { exportPassword = it },
                        label = { Text(stringResource(R.string.label_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = exportPassword.isNotBlank(),
                    onClick = {
                        showExportPasswordDialog = false
                        exportLauncher.launch("callblocker_backup.spamdb")
                    }
                ) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportPasswordDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showRestorePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showRestorePasswordDialog = false },
            title = { Text(stringResource(R.string.dialog_restore_pwd_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.dialog_restore_pwd_msg))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restorePassword,
                        onValueChange = { restorePassword = it },
                        label = { Text(stringResource(R.string.label_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = restorePassword.isNotBlank(),
                    onClick = {
                        showRestorePasswordDialog = false
                        restoreLauncher.launch(arrayOf("*/*"))
                    }
                ) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestorePasswordDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

/** Updates application locales for in-app language switching via AppCompatDelegate. */
private fun setAppLanguage(languageTag: String) {
    val appLocales = if (languageTag.isBlank()) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(languageTag)
    }
    AppCompatDelegate.setApplicationLocales(appLocales)
}

/** Returns the active application language tag. */
private fun getCurrentLanguageTag(): String {
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    return if (!currentLocales.isEmpty) {
        currentLocales.toLanguageTags()
    } else {
        ""
    }
}

/** Setting row component displaying title, subtitle, and a toggle switch. */
@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
