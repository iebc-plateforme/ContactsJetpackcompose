package com.contacts.android.contacts.presentation.screens.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.contacts.android.contacts.R
import com.contacts.android.contacts.data.preferences.ColorTheme
import com.contacts.android.contacts.data.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    currentTheme: ColorTheme,
    currentThemeMode: ThemeMode,
    onThemeSelected: (ColorTheme) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPremium: Boolean = false
) {
    // Premium-only feature: redirect if not premium
    if (!isPremium) {
        // Show a message that this feature requires premium
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.theme_customization),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Premium Feature",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Custom themes are only available for Premium users.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.theme_customization),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Mode Section
            ThemeModeSection(
                currentMode = currentThemeMode,
                onModeSelected = onThemeModeSelected
            )

            HorizontalDivider()

            // Color Theme Section
            Text(
                text = stringResource(R.string.color_theme),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.choose_app_colors),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Color Themes Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ColorTheme.values().toList()) { theme ->
                    ColorThemeCard(
                        theme = theme,
                        isSelected = theme == currentTheme,
                        onClick = { onThemeSelected(theme) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeSection(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.theme_mode),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeModeChip(
                icon = Icons.Default.LightMode,
                label = stringResource(R.string.theme_mode_light),
                isSelected = currentMode == ThemeMode.LIGHT,
                onClick = { onModeSelected(ThemeMode.LIGHT) },
                modifier = Modifier.weight(1f)
            )

            ThemeModeChip(
                icon = Icons.Default.DarkMode,
                label = stringResource(R.string.theme_mode_dark),
                isSelected = currentMode == ThemeMode.DARK,
                onClick = { onModeSelected(ThemeMode.DARK) },
                modifier = Modifier.weight(1f)
            )

            ThemeModeChip(
                icon = Icons.Default.Brightness6,
                label = stringResource(R.string.theme_mode_system),
                isSelected = currentMode == ThemeMode.SYSTEM,
                onClick = { onModeSelected(ThemeMode.SYSTEM) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeModeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bg_color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "content_color"
    )

    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ColorThemeCard(
    theme: ColorTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(theme.primaryColor) else Color.Transparent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Color Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Color Circle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(Color(theme.primaryColor))
                )

                // Secondary & Tertiary Colors
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(theme.secondaryColor))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(theme.tertiaryColor))
                    )
                }
            }

            // Theme Name
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Color(theme.primaryColor),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
