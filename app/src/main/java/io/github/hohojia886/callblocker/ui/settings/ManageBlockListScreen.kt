/**
 * Block list management screen providing real-time search, wildcard rule entry, and country impact preview.
 */
package io.github.hohojia886.callblocker.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hohojia886.callblocker.R
import io.github.hohojia886.callblocker.data.db.BlockedNumber
import io.github.hohojia886.callblocker.util.ContactPickerHelper

/** Composable screen for displaying, searching, and adding rules to custom block list. */
@Composable
fun ManageBlockListScreen(
    viewModel: ManageBlockListViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val blockedNumbers by viewModel.filteredBlockedNumbers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val inputPattern by viewModel.inputPattern.collectAsStateWithLifecycle()
    val inputNote by viewModel.inputNote.collectAsStateWithLifecycle()
    val impactedCountries by viewModel.impactedCountries.collectAsStateWithLifecycle()

    var showAddNumberDialog by remember { mutableStateOf(false) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        if (contactUri != null) {
            val details = ContactPickerHelper.getContactDetails(context, contactUri)
            if (details != null && details.phoneNumber.isNotBlank()) {
                viewModel.setContactDetails(details.phoneNumber, details.name)
                Toast.makeText(context, details.name, Toast.LENGTH_SHORT).show()
                showAddNumberDialog = true
            } else {
                Toast.makeText(context, context.getString(R.string.toast_cannot_read_phone_number), Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onNavigateBack != null) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_cancel)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.block_list_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.blocked_numbers_count, blockedNumbers.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (blockedNumbers.isEmpty()) {
                Text(
                    text = if (searchQuery.isNotBlank()) stringResource(R.string.search_no_results, searchQuery) else stringResource(R.string.no_blocked_numbers),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 64.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = blockedNumbers,
                        key = { it.id }
                    ) { item ->
                        BlockedNumberItemCard(
                            item = item,
                            onDelete = { viewModel.deleteBlockedNumber(item) }
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddNumberDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
            text = { Text(stringResource(R.string.btn_add_blocked_number)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        )
    }

    if (showAddNumberDialog) {
        AlertDialog(
            onDismissRequest = { showAddNumberDialog = false },
            title = { Text(stringResource(R.string.add_number_section_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputPattern,
                        onValueChange = { viewModel.updateInputPattern(it) },
                        label = { Text(stringResource(R.string.input_number_label)) },
                        placeholder = { Text(stringResource(R.string.input_number_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputNote,
                        onValueChange = { viewModel.updateInputNote(it) },
                        label = { Text(stringResource(R.string.input_note_label)) },
                        placeholder = { Text(stringResource(R.string.input_note_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            try {
                                contactPickerLauncher.launch(null)
                            } catch (e: Exception) {
                                Toast.makeText(context, context.getString(R.string.toast_error, e.localizedMessage ?: ""), Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(stringResource(R.string.btn_pick_contact))
                    }

                    if (impactedCountries.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text(
                                        text = stringResource(R.string.dynamic_preview_warning),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }

                                impactedCountries.forEach { country ->
                                    Text(
                                        text = "${country.flagEmoji} ${country.code} ... (${country.countryName})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = inputPattern.isNotBlank(),
                    onClick = {
                        viewModel.addBlockedNumber()
                        showAddNumberDialog = false
                    }
                ) {
                    Text(stringResource(R.string.btn_add_to_block_list))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNumberDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

/** Card item representing an individual blocked number rule entry. */
@Composable
fun BlockedNumberItemCard(
    item: BlockedNumber,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.numberPattern,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!item.note.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.label_note_format, item.note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }
}
