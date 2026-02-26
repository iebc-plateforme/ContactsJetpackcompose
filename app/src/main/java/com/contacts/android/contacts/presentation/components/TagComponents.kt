package com.contacts.android.contacts.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.contacts.android.contacts.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagChipsRow(
    tags: List<String>,
    onRemoveTag: (String) -> Unit,
    onAddTagClick: () -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            InputChip(
                selected = false,
                onClick = {},
                label = { Text(tag) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Label,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = if (!readOnly) {
                    {
                        IconButton(
                            onClick = { onRemoveTag(tag) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else null
            )
        }

        if (!readOnly) {
            AssistChip(
                onClick = onAddTagClick,
                label = { Text(stringResource(R.string.add_tag)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagInputDialog(
    availableTags: List<String>,
    currentTags: List<String>,
    onAddTag: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var tagInput by remember { mutableStateOf("") }
    val suggestions = remember(tagInput, availableTags, currentTags) {
        if (tagInput.isBlank()) emptyList()
        else availableTags
            .filter { it !in currentTags && it.contains(tagInput, ignoreCase = true) }
            .take(5)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_tag)) },
        text = {
            Column {
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    label = { Text(stringResource(R.string.tag_name)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Label, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (tagInput.isNotBlank()) {
                                onAddTag(tagInput.trim())
                                onDismiss()
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = suggestions.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = stringResource(R.string.tag_suggestions),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        suggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    onAddTag(suggestion)
                                    onDismiss()
                                },
                                label = { Text(suggestion) },
                                icon = {
                                    Icon(
                                        Icons.Default.Label,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (tagInput.isNotBlank()) {
                        onAddTag(tagInput.trim())
                        onDismiss()
                    }
                },
                enabled = tagInput.isNotBlank()
            ) {
                Text(stringResource(R.string.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}
