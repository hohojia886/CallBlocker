/**
 * Main application screen wrapper featuring Material 3 Scaffold, bottom navigation tabs, and role warning banner.
 */
package io.github.hohojia886.callblocker.ui

import android.app.role.RoleManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.hohojia886.callblocker.R
import io.github.hohojia886.callblocker.ui.dashboard.DashboardScreen
import io.github.hohojia886.callblocker.ui.history.HistoryScreen
import io.github.hohojia886.callblocker.ui.settings.ManageBlockListScreen
import io.github.hohojia886.callblocker.ui.settings.SettingsScreen

/** Enum representing top-level navigation tabs. */
enum class MainTab(val titleResId: Int, val icon: ImageVector) {
    DASHBOARD(R.string.tab_dashboard, Icons.Default.Dashboard),
    HISTORY(R.string.tab_history, Icons.Default.History),
    BLOCK_LIST(R.string.tab_block_list, Icons.Default.FormatListNumbered),
    SETTINGS(R.string.tab_settings, Icons.Default.Settings)
}

/** Composable function hosting bottom navigation bar, active tab content, and role warning banner. */
@Composable
fun MainScreen(
    onRequestDialerRole: () -> Unit = {},
    onRequestCallScreeningRole: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.DASHBOARD) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isCallScreeningRoleHeld by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = context.getSystemService(RoleManager::class.java)
                    isCallScreeningRoleHeld = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
                } else {
                    isCallScreeningRoleHeld = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    val title = stringResource(tab.titleResId)
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = title
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isCallScreeningRoleHeld) {
                CallScreeningRoleBanner(onRequestRole = onRequestCallScreeningRole)
            }

            Column(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    MainTab.DASHBOARD -> DashboardScreen()
                    MainTab.HISTORY -> HistoryScreen()
                    MainTab.BLOCK_LIST -> ManageBlockListScreen(onNavigateBack = null)
                    MainTab.SETTINGS -> SettingsScreen(
                        onNavigateToManageBlockList = { selectedTab = MainTab.BLOCK_LIST },
                        onRequestDialerRole = onRequestDialerRole
                    )
                }
            }
        }
    }
}

/** Warning banner composable encouraging users to grant default Call Screening role. */
@Composable
fun CallScreeningRoleBanner(
    onRequestRole: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.banner_role_warning_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Text(
                text = stringResource(R.string.banner_role_warning_sub),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Button(
                onClick = onRequestRole,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Text(stringResource(R.string.btn_set_default_role))
            }
        }
    }
}
