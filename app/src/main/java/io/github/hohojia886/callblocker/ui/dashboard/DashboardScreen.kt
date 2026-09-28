/**
 * Dashboard Composable screen displaying blocking stat cards and reason distribution charts.
 */
package io.github.hohojia886.callblocker.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hohojia886.callblocker.R
import io.github.hohojia886.callblocker.data.db.BlockReason

/** Dashboard screen displaying aggregated interception statistics and reason breakdown. */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle()
    val thisMonthCount by viewModel.thisMonthCount.collectAsStateWithLifecycle()
    val thisYearCount by viewModel.thisYearCount.collectAsStateWithLifecycle()
    val lifetimeCount by viewModel.lifetimeCount.collectAsStateWithLifecycle()
    val reasonCounts by viewModel.reasonCounts.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = stringResource(R.string.stat_today),
                count = todayCount,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.stat_this_month),
                count = thisMonthCount,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = stringResource(R.string.stat_this_year),
                count = thisYearCount,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.stat_lifetime),
                count = lifetimeCount,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.reason_breakdown_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val total = reasonCounts.sumOf { it.count }.coerceAtLeast(1)

                val reasonLabels = mapOf(
                    BlockReason.NO_NUMBER to stringResource(R.string.reason_no_number),
                    BlockReason.NON_CONTACT to stringResource(R.string.reason_non_contact),
                    BlockReason.INTERNATIONAL to stringResource(R.string.reason_international),
                    BlockReason.BLACK_LIST to stringResource(R.string.reason_blacklist),
                    BlockReason.NONE to stringResource(R.string.reason_other)
                )

                if (reasonCounts.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_records_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    reasonCounts.forEach { item ->
                        val label = reasonLabels[item.reason] ?: item.reason.name
                        val rawProgress = item.count.toFloat() / total
                        val progress = rawProgress.coerceIn(0f, 1f)
                        val percent = (progress * 100).toInt()

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${item.count} ($percent%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Individual statistics card displaying metric title and count. */
@Composable
fun StatCard(
    title: String,
    count: Int,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
