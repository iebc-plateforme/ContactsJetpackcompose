package com.contacts.android.contacts.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.contacts.android.contacts.data.preferences.ColorTheme

// Blue Theme
private val BlueLightPrimary = Color(0xFF1976D2)
private val BlueLightOnPrimary = Color(0xFFFFFFFF)
private val BlueLightPrimaryContainer = Color(0xFFBBDEFB)
private val BlueLightOnPrimaryContainer = Color(0xFF0D47A1)

private val BlueDarkPrimary = Color(0xFF90CAF9)
private val BlueDarkOnPrimary = Color(0xFF0D47A1)
private val BlueDarkPrimaryContainer = Color(0xFF1565C0)
private val BlueDarkOnPrimaryContainer = Color(0xFFE3F2FD)

// Green Theme
private val GreenLightPrimary = Color(0xFF388E3C)
private val GreenLightOnPrimary = Color(0xFFFFFFFF)
private val GreenLightPrimaryContainer = Color(0xFFC8E6C9)
private val GreenLightOnPrimaryContainer = Color(0xFF1B5E20)

private val GreenDarkPrimary = Color(0xFF81C784)
private val GreenDarkOnPrimary = Color(0xFF1B5E20)
private val GreenDarkPrimaryContainer = Color(0xFF2E7D32)
private val GreenDarkOnPrimaryContainer = Color(0xFFE8F5E9)

// Purple Theme
private val PurpleLightPrimary = Color(0xFF7B1FA2)
private val PurpleLightOnPrimary = Color(0xFFFFFFFF)
private val PurpleLightPrimaryContainer = Color(0xFFE1BEE7)
private val PurpleLightOnPrimaryContainer = Color(0xFF4A148C)

private val PurpleDarkPrimary = Color(0xFFCE93D8)
private val PurpleDarkOnPrimary = Color(0xFF4A148C)
private val PurpleDarkPrimaryContainer = Color(0xFF8E24AA)
private val PurpleDarkOnPrimaryContainer = Color(0xFFF3E5F5)

// Orange Theme
private val OrangeLightPrimary = Color(0xFFF57C00)
private val OrangeLightOnPrimary = Color(0xFFFFFFFF)
private val OrangeLightPrimaryContainer = Color(0xFFFFE0B2)
private val OrangeLightOnPrimaryContainer = Color(0xFFE65100)

private val OrangeDarkPrimary = Color(0xFFFFB74D)
private val OrangeDarkOnPrimary = Color(0xFFE65100)
private val OrangeDarkPrimaryContainer = Color(0xFFFB8C00)
private val OrangeDarkOnPrimaryContainer = Color(0xFFFFF3E0)

// Red Theme
private val RedLightPrimary = Color(0xFFD32F2F)
private val RedLightOnPrimary = Color(0xFFFFFFFF)
private val RedLightPrimaryContainer = Color(0xFFFFCDD2)
private val RedLightOnPrimaryContainer = Color(0xFFB71C1C)

private val RedDarkPrimary = Color(0xFFE57373)
private val RedDarkOnPrimary = Color(0xFFB71C1C)
private val RedDarkPrimaryContainer = Color(0xFFC62828)
private val RedDarkOnPrimaryContainer = Color(0xFFFFEBEE)

// Pink Theme
private val PinkLightPrimary = Color(0xFFC2185B)
private val PinkLightOnPrimary = Color(0xFFFFFFFF)
private val PinkLightPrimaryContainer = Color(0xFFF8BBD0)
private val PinkLightOnPrimaryContainer = Color(0xFF880E4F)

private val PinkDarkPrimary = Color(0xFFF48FB1)
private val PinkDarkOnPrimary = Color(0xFF880E4F)
private val PinkDarkPrimaryContainer = Color(0xFFAD1457)
private val PinkDarkOnPrimaryContainer = Color(0xFFFCE4EC)

fun getColorScheme(colorTheme: ColorTheme, isDark: Boolean) = when (colorTheme) {
    ColorTheme.BLUE -> if (isDark) {
        darkColorScheme(
            primary = BlueDarkPrimary,
            onPrimary = BlueDarkOnPrimary,
            primaryContainer = BlueDarkPrimaryContainer,
            onPrimaryContainer = BlueDarkOnPrimaryContainer
        )
    } else {
        lightColorScheme(
            primary = BlueLightPrimary,
            onPrimary = BlueLightOnPrimary,
            primaryContainer = BlueLightPrimaryContainer,
            onPrimaryContainer = BlueLightOnPrimaryContainer
        )
    }

    ColorTheme.GREEN -> if (isDark) {
        darkColorScheme(
            primary = GreenDarkPrimary,
            onPrimary = GreenDarkOnPrimary,
            primaryContainer = GreenDarkPrimaryContainer,
            onPrimaryContainer = GreenDarkOnPrimaryContainer
        )
    } else {
        lightColorScheme(
            primary = GreenLightPrimary,
            onPrimary = GreenLightOnPrimary,
            primaryContainer = GreenLightPrimaryContainer,
            onPrimaryContainer = GreenLightOnPrimaryContainer
        )
    }

    ColorTheme.PURPLE -> if (isDark) {
        darkColorScheme(
            primary = PurpleDarkPrimary,
            onPrimary = PurpleDarkOnPrimary,
            primaryContainer = PurpleDarkPrimaryContainer,
            onPrimaryContainer = PurpleDarkOnPrimaryContainer
        )
    } else {
        lightColorScheme(
            primary = PurpleLightPrimary,
            onPrimary = PurpleLightOnPrimary,
            primaryContainer = PurpleLightPrimaryContainer,
            onPrimaryContainer = PurpleLightOnPrimaryContainer
        )
    }

    ColorTheme.ORANGE -> if (isDark) {
        darkColorScheme(
            primary = OrangeDarkPrimary,
            onPrimary = OrangeDarkOnPrimary,
            primaryContainer = OrangeDarkPrimaryContainer,
            onPrimaryContainer = OrangeDarkOnPrimaryContainer
        )
    } else {
        lightColorScheme(
            primary = OrangeLightPrimary,
            onPrimary = OrangeLightOnPrimary,
            primaryContainer = OrangeLightPrimaryContainer,
            onPrimaryContainer = OrangeLightOnPrimaryContainer
        )
    }

    ColorTheme.RED -> if (isDark) {
        darkColorScheme(
            primary = RedDarkPrimary,
            onPrimary = RedDarkOnPrimary,
            primaryContainer = RedDarkPrimaryContainer,
            onPrimaryContainer = RedDarkOnPrimaryContainer
        )
    } else {
        lightColorScheme(
            primary = RedLightPrimary,
            onPrimary = RedLightOnPrimary,
            primaryContainer = RedLightPrimaryContainer,
            onPrimaryContainer = RedLightOnPrimaryContainer
        )
    }

    ColorTheme.PINK -> if (isDark) {
        darkColorScheme(
            primary = PinkDarkPrimary,
            onPrimary = PinkDarkOnPrimary,
            primaryContainer = PinkDarkPrimaryContainer,
            onPrimaryContainer = PinkDarkOnPrimaryContainer
        )
    } else {
        lightColorScheme(
            primary = PinkLightPrimary,
            onPrimary = PinkLightOnPrimary,
            primaryContainer = PinkLightPrimaryContainer,
            onPrimaryContainer = PinkLightOnPrimaryContainer
        )
    }
}

fun getThemePreviewColor(colorTheme: ColorTheme): Color = when (colorTheme) {
    ColorTheme.BLUE -> BlueLightPrimary
    ColorTheme.GREEN -> GreenLightPrimary
    ColorTheme.PURPLE -> PurpleLightPrimary
    ColorTheme.ORANGE -> OrangeLightPrimary
    ColorTheme.RED -> RedLightPrimary
    ColorTheme.PINK -> PinkLightPrimary
}
