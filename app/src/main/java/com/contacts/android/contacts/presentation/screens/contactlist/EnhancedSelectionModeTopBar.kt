package com.contacts.android.contacts.presentation.screens.contactlist
import androidx.compose.ui.res.stringResource
import com.contacts.android.contacts.R

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Enhanced selection mode top bar with all Fossify-style multi-select actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSelectionModeTopBar(
    selectedCount: Int,
    isAllSelected: Boolean,
    hasAnyFavorites: Boolean, // Whether any selected contacts are favorites
    onExitSelectionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onShareSelected: () -> Unit,
    onAddToFavorites: () -> Unit,
    onRemoveFromFavorites: () -> Unit,
    onMergeSelected: () -> Unit,
    onExportSelected: () -> Unit
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(stringResource(R.string.selected_count, selectedCount))
        },
        navigationIcon = {
            IconButton(onClick = onExitSelectionMode) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.exit_selection_mode)
                )
            }
        },
        actions = {
            // Select all / Deselect all
            IconButton(onClick = {
                if (isAllSelected) {
                    onDeselectAll()
                } else {
                    onSelectAll()
                }
            }) {
                Icon(
                    imageVector = if (isAllSelected) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                    contentDescription = if (isAllSelected) stringResource(R.string.deselect_all) else stringResource(R.string.select_all),
                    tint = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Add to favorites (visible if at least one selected)
            if (selectedCount > 0) {
                IconButton(onClick = {
                    if (hasAnyFavorites) {
                        onRemoveFromFavorites()
                    } else {
                        onAddToFavorites()
                    }
                }) {
                    Icon(
                        imageVector = if (hasAnyFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (hasAnyFavorites) stringResource(R.string.remove_from_favorites) else stringResource(R.string.favorites_add),
                        tint = if (hasAnyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Share (visible if at least one selected)
            if (selectedCount > 0) {
                IconButton(onClick = onShareSelected) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share_contacts)
                    )
                }
            }

            // Delete (always visible)
            IconButton(
                onClick = onDeleteSelected,
                enabled = selectedCount > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_selected_contacts)
                )
            }

            // More options menu
            Box {
                IconButton(
                    onClick = { showMoreMenu = true },
                    enabled = selectedCount > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false }
                ) {
                    // Export as VCF
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.export_as_vcf)) },
                        onClick = {
                            onExportSelected()
                            showMoreMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Upload, contentDescription = null)
                        },
                        enabled = selectedCount > 0
                    )

                    // Merge contacts (only if 2+ selected)
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.merge_contacts)) },
                        onClick = {
                            onMergeSelected()
                            showMoreMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.MergeType, contentDescription = null)
                        },
                        enabled = selectedCount >= 2
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
