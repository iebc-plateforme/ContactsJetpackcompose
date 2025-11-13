package com.contacts.android.contactsjetpackcompose.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.contacts.android.contactsjetpackcompose.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberField(
    number: String,
    type: PhoneType,
    onNumberChange: (String) -> Unit,
    onTypeChange: (PhoneType) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showTypeMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = number,
                onValueChange = onNumberChange,
                label = { Text("Phone number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = showTypeMenu,
                onExpandedChange = { showTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = type.toDisplayString(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    PhoneType.values().forEach { phoneType ->
                        DropdownMenuItem(
                            text = { Text(phoneType.toDisplayString()) },
                            onClick = {
                                onTypeChange(phoneType)
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }
        }

        if (canRemove) {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailField(
    email: String,
    type: EmailType,
    onEmailChange: (String) -> Unit,
    onTypeChange: (EmailType) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showTypeMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = showTypeMenu,
                onExpandedChange = { showTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = type.toDisplayString(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    EmailType.values().forEach { emailType ->
                        DropdownMenuItem(
                            text = { Text(emailType.toDisplayString()) },
                            onClick = {
                                onTypeChange(emailType)
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }
        }

        if (canRemove) {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressField(
    street: String,
    city: String,
    state: String,
    postalCode: String,
    country: String,
    type: AddressType,
    onStreetChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onTypeChange: (AddressType) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showTypeMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Address",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )

            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = street,
            onValueChange = onStreetChange,
            label = { Text("Street") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                label = { Text("City") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state,
                onValueChange = onStateChange,
                label = { Text("State") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = postalCode,
                onValueChange = onPostalCodeChange,
                label = { Text("Postal Code") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = country,
                onValueChange = onCountryChange,
                label = { Text("Country") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = showTypeMenu,
            onExpandedChange = { showTypeMenu = it }
        ) {
            OutlinedTextField(
                value = type.toDisplayString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors()
            )

            ExposedDropdownMenu(
                expanded = showTypeMenu,
                onDismissRequest = { showTypeMenu = false }
            ) {
                AddressType.values().forEach { addressType ->
                    DropdownMenuItem(
                        text = { Text(addressType.toDisplayString()) },
                        onClick = {
                            onTypeChange(addressType)
                            showTypeMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddFieldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}
