package com.contacts.android.contacts.presentation.screens.support

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.contacts.android.contacts.R

/**
 * Premium Support Screen
 * Allows premium users to contact support via multiple channels
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSupportScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium Support") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Premium Support",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Priority Email Assistance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Text(
                text = "How can we help you?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Email Support
            SupportCard(
                icon = Icons.Default.Email,
                title = "Email Support",
                description = "Send us an email and we'll respond as soon as possible",
                onClick = {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:ism4company@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Premium Support Request - Contacts App")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Please describe your issue or question:\n\n" +
                                    "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n" +
                                    "Android Version: ${android.os.Build.VERSION.RELEASE}\n\n" +
                                    "Description:\n\n"
                        )
                    }
                    context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                }
            )

            // Report Bug
            SupportCard(
                icon = Icons.Default.BugReport,
                title = "Report a Bug",
                description = "Help us improve by reporting issues",
                onClick = {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:ism4company@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Bug Report - Contacts App")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n" +
                                    "Android Version: ${android.os.Build.VERSION.RELEASE}\n\n" +
                                    "Bug Description:\n\n" +
                                    "Steps to reproduce:\n1. \n2. \n3. \n\n" +
                                    "Expected behavior:\n\n" +
                                    "Actual behavior:\n\n"
                        )
                    }
                    context.startActivity(Intent.createChooser(emailIntent, "Send Bug Report"))
                }
            )

            // Feature Request
            SupportCard(
                icon = Icons.Default.Lightbulb,
                title = "Request a Feature",
                description = "Share your ideas with us",
                onClick = {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:ism4company@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Feature Request - Contacts App")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Feature Request:\n\n" +
                                    "Description of the feature:\n\n" +
                                    "How would this feature benefit you:\n\n"
                        )
                    }
                    context.startActivity(Intent.createChooser(emailIntent, "Send Feature Request"))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Contact Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "• Support Email: ism4company@gmail.com\n" +
                                "• All requests are answered as soon as possible\n" +
                                "• Premium users get priority support",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
