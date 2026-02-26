package com.contacts.android.contacts.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class WhatsNewFeature(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val isPremium: Boolean = false
)

/**
 * Current version's new features.
 * Update this list when releasing a new version.
 */
val WHATS_NEW_VERSION_CODE = 160 // Must match versionCode in build.gradle.kts

val WHATS_NEW_FEATURES = listOf(
    WhatsNewFeature(
        icon = Icons.Default.PictureAsPdf,
        title = "PDF Export",
        description = "Export contacts as styled PDF documents for printing or sharing.",
        isPremium = true
    ),
    WhatsNewFeature(
        icon = Icons.Default.Label,
        title = "Tags",
        description = "Add tags to contacts for better organization and filtering.",
        isPremium = true
    ),
    WhatsNewFeature(
        icon = Icons.Default.Lock,
        title = "Private Contacts",
        description = "Hide sensitive contacts behind PIN or biometric authentication.",
        isPremium = true
    ),
    WhatsNewFeature(
        icon = Icons.Default.Sync,
        title = "Improved Group Sync",
        description = "Groups now sync properly with other contact apps on your device."
    ),
    WhatsNewFeature(
        icon = Icons.Default.Star,
        title = "Favorites Redesign",
        description = "New grid view, long-press actions, and smoother experience."
    )
)

@Composable
fun WhatsNewDialog(
    onDismiss: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.NewReleases,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "What's New",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WHATS_NEW_FEATURES.forEach { feature ->
                    WhatsNewItem(feature = feature)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got it!")
            }
        },
        dismissButton = {
            val hasPremiumFeatures = WHATS_NEW_FEATURES.any { it.isPremium }
            if (hasPremiumFeatures) {
                TextButton(onClick = {
                    onDismiss()
                    onNavigateToPremium()
                }) {
                    Icon(
                        Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("See Premium")
                }
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
private fun WhatsNewItem(feature: WhatsNewFeature) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (feature.isPremium)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (feature.isPremium)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (feature.isPremium) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "PREMIUM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
