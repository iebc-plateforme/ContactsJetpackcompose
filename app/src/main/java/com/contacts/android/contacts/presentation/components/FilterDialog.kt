package com.contacts.android.contacts.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.contacts.android.contacts.R
import com.contacts.android.contacts.domain.model.*

/**
 * Filter dialog with improved UX following Material 3 design
 * Based on Fossify implementation with enhanced animations and accessibility
 */
@Composable
fun FilterDialog(
    currentFilter: ContactFilter,
    totalContactsCount: Int,
    favoritesCount: Int,
    withPhoneCount: Int,
    withEmailCount: Int,
    withAddressCount: Int,
    availableGroups: List<Group> = emptyList(),
    onDismiss: () -> Unit,
    onFilterSelected: (ContactFilter) -> Unit
) {
    var selectedType by remember { mutableStateOf(currentFilter.type) }
    var selectedGroupIds by remember { mutableStateOf(currentFilter.selectedGroupIds) }
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.filter_contacts),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterTypeOption(
                        label = stringResource(id = R.string.filter_all),
                        count = totalContactsCount,
                        icon = Icons.Default.People,
                        selected = selectedType == ContactFilterType.ALL,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedType = ContactFilterType.ALL
                        }
                    )

                    FilterTypeOption(
                        label = stringResource(id = R.string.favorites_title),
                        count = favoritesCount,
                        icon = Icons.Default.Star,
                        selected = selectedType == ContactFilterType.FAVORITES_ONLY,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedType = ContactFilterType.FAVORITES_ONLY
                        }
                    )

                    FilterTypeOption(
                        label = stringResource(id = R.string.filter_with_phone),
                        count = withPhoneCount,
                        icon = Icons.Default.Phone,
                        selected = selectedType == ContactFilterType.WITH_PHONE_ONLY,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedType = ContactFilterType.WITH_PHONE_ONLY
                        }
                    )

                    FilterTypeOption(
                        label = stringResource(id = R.string.filter_with_email),
                        count = withEmailCount,
                        icon = Icons.Default.Email,
                        selected = selectedType == ContactFilterType.WITH_EMAIL_ONLY,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedType = ContactFilterType.WITH_EMAIL_ONLY
                        }
                    )

                    FilterTypeOption(
                        label = stringResource(id = R.string.filter_with_address),
                        count = withAddressCount,
                        icon = Icons.Default.LocationOn,
                        selected = selectedType == ContactFilterType.WITH_ADDRESS_ONLY,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedType = ContactFilterType.WITH_ADDRESS_ONLY
                        }
                    )

                    if (availableGroups.isNotEmpty()) {
                        FilterTypeOption(
                            label = "By groups",
                            count = availableGroups.sumOf { it.contactCount },
                            icon = Icons.Default.Group,
                            selected = selectedType == ContactFilterType.GROUPS,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedType = ContactFilterType.GROUPS
                            }
                        )
                    }
                }

                // Group selection (shown when GROUPS is selected)
                AnimatedVisibility(
                    visible = selectedType == ContactFilterType.GROUPS && availableGroups.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider()

                        Text(
                            text = stringResource(id = R.string.select_groups),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        availableGroups.forEach { group ->
                            GroupCheckboxOption(
                                group = group,
                                checked = group.id in selectedGroupIds,
                                onCheckedChange = { checked ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedGroupIds = if (checked) {
                                        selectedGroupIds + group.id
                                    } else {
                                        selectedGroupIds - group.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFilterSelected(
                        ContactFilter(
                            type = selectedType,
                            selectedGroupIds = if (selectedType == ContactFilterType.GROUPS) selectedGroupIds else emptySet()
                        )
                    )
                    onDismiss()
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun FilterTypeOption(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null // handled by row
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GroupCheckboxOption(
    group: Group,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = stringResource(id = R.string.contacts_count, group.contactCount, if (group.contactCount != 1) "s" else ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
