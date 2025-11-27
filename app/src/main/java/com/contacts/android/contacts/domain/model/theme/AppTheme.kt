package com.contacts.android.contacts.domain.model.theme

import androidx.compose.ui.graphics.Color

/**
 * Represents a custom app theme with primary, secondary, and tertiary colors
 */
data class AppTheme(
    val id: String,
    val name: String,
    val primaryColor: Long, // Color as Long for serialization
    val secondaryColor: Long,
    val tertiaryColor: Long,
    val isCustom: Boolean = false
) {
    companion object {
        // Predefined themes
        val DEFAULT = AppTheme(
            id = "default",
            name = "Default Blue",
            primaryColor = 0xFF1976D2, // Blue
            secondaryColor = 0xFF388E3C, // Green
            tertiaryColor = 0xFFFFA000, // Amber
            isCustom = false
        )

        val PURPLE = AppTheme(
            id = "purple",
            name = "Purple Dream",
            primaryColor = 0xFF6A1B9A, // Purple
            secondaryColor = 0xFFE91E63, // Pink
            tertiaryColor = 0xFFFF6F00, // Orange
            isCustom = false
        )

        val GREEN = AppTheme(
            id = "green",
            name = "Nature Green",
            primaryColor = 0xFF2E7D32, // Dark Green
            secondaryColor = 0xFF689F38, // Light Green
            tertiaryColor = 0xFFFBC02D, // Yellow
            isCustom = false
        )

        val ORANGE = AppTheme(
            id = "orange",
            name = "Sunset Orange",
            primaryColor = 0xFFE64A19, // Deep Orange
            secondaryColor = 0xFFF57C00, // Orange
            tertiaryColor = 0xFFFFD54F, // Amber Light
            isCustom = false
        )

        val TEAL = AppTheme(
            id = "teal",
            name = "Ocean Teal",
            primaryColor = 0xFF00796B, // Teal
            secondaryColor = 0xFF0097A7, // Cyan
            tertiaryColor = 0xFF7CB342, // Light Green
            isCustom = false
        )

        val RED = AppTheme(
            id = "red",
            name = "Ruby Red",
            primaryColor = 0xFFC62828, // Red
            secondaryColor = 0xFFD32F2F, // Light Red
            tertiaryColor = 0xFFFF6F00, // Orange
            isCustom = false
        )

        val PINK = AppTheme(
            id = "pink",
            name = "Cherry Blossom",
            primaryColor = 0xFFC2185B, // Pink
            secondaryColor = 0xFFAD1457, // Dark Pink
            tertiaryColor = 0xFFE91E63, // Light Pink
            isCustom = false
        )

        val INDIGO = AppTheme(
            id = "indigo",
            name = "Deep Indigo",
            primaryColor = 0xFF303F9F, // Indigo
            secondaryColor = 0xFF3949AB, // Light Indigo
            tertiaryColor = 0xFF5C6BC0, // Very Light Indigo
            isCustom = false
        )

        // List of all predefined themes
        val PREDEFINED_THEMES = listOf(
            DEFAULT,
            PURPLE,
            GREEN,
            ORANGE,
            TEAL,
            RED,
            PINK,
            INDIGO
        )
    }

    fun getPrimaryColor() = Color(primaryColor)
    fun getSecondaryColor() = Color(secondaryColor)
    fun getTertiaryColor() = Color(tertiaryColor)
}
