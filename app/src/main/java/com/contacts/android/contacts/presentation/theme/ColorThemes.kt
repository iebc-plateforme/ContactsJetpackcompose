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

// Teal Theme
private val TealLightPrimary = Color(0xFF00796B)
private val TealLightOnPrimary = Color(0xFFFFFFFF)
private val TealLightPrimaryContainer = Color(0xFFB2DFDB)
private val TealLightOnPrimaryContainer = Color(0xFF004D40)

private val TealDarkPrimary = Color(0xFF4DB6AC)
private val TealDarkOnPrimary = Color(0xFF004D40)
private val TealDarkPrimaryContainer = Color(0xFF00695C)
private val TealDarkOnPrimaryContainer = Color(0xFFE0F2F1)

// Indigo Theme
private val IndigoLightPrimary = Color(0xFF303F9F)
private val IndigoLightOnPrimary = Color(0xFFFFFFFF)
private val IndigoLightPrimaryContainer = Color(0xFFC5CAE9)
private val IndigoLightOnPrimaryContainer = Color(0xFF1A237E)

private val IndigoDarkPrimary = Color(0xFF7986CB)
private val IndigoDarkOnPrimary = Color(0xFF1A237E)
private val IndigoDarkPrimaryContainer = Color(0xFF3949AB)
private val IndigoDarkOnPrimaryContainer = Color(0xFFE8EAF6)

// Brown Theme
private val BrownLightPrimary = Color(0xFF5D4037)
private val BrownLightOnPrimary = Color(0xFFFFFFFF)
private val BrownLightPrimaryContainer = Color(0xFFD7CCC8)
private val BrownLightOnPrimaryContainer = Color(0xFF3E2723)

private val BrownDarkPrimary = Color(0xFFBCAAA4)
private val BrownDarkOnPrimary = Color(0xFF3E2723)
private val BrownDarkPrimaryContainer = Color(0xFF6D4C41)
private val BrownDarkOnPrimaryContainer = Color(0xFFEFEBE9)

// Cyan Theme
private val CyanLightPrimary = Color(0xFF0097A7)
private val CyanLightOnPrimary = Color(0xFFFFFFFF)
private val CyanLightPrimaryContainer = Color(0xFFB2EBF2)
private val CyanLightOnPrimaryContainer = Color(0xFF006064)

private val CyanDarkPrimary = Color(0xFF4DD0E1)
private val CyanDarkOnPrimary = Color(0xFF006064)
private val CyanDarkPrimaryContainer = Color(0xFF00838F)
private val CyanDarkOnPrimaryContainer = Color(0xFFE0F7FA)

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

    ColorTheme.TEAL -> if (isDark) {
        darkColorScheme(
            primary = TealDarkPrimary,
            onPrimary = TealDarkOnPrimary,
            primaryContainer = TealDarkPrimaryContainer,
            onPrimaryContainer = TealDarkOnPrimaryContainer
        )
    } else {
        lightColorScheme(
            primary = TealLightPrimary,
            onPrimary = TealLightOnPrimary,
            primaryContainer = TealLightPrimaryContainer,
            onPrimaryContainer = TealLightOnPrimaryContainer
        )
    }

    ColorTheme.INDIGO -> if (isDark) {
        darkColorScheme(
            primary = IndigoDarkPrimary,
            onPrimary = IndigoDarkOnPrimary,
            primaryContainer = IndigoDarkPrimaryContainer,
            onPrimaryContainer = IndigoDarkOnPrimaryContainer
        )
    } else {
        lightColorScheme(
            primary = IndigoLightPrimary,
            onPrimary = IndigoLightOnPrimary,
            primaryContainer = IndigoLightPrimaryContainer,
            onPrimaryContainer = IndigoLightOnPrimaryContainer
        )
    }

    ColorTheme.BROWN -> if (isDark) {
        darkColorScheme(
            primary = BrownDarkPrimary,
            onPrimary = BrownDarkOnPrimary,
            primaryContainer = BrownDarkPrimaryContainer,
            onPrimaryContainer = BrownDarkOnPrimaryContainer
        )
    } else {
        lightColorScheme(
            primary = BrownLightPrimary,
            onPrimary = BrownLightOnPrimary,
            primaryContainer = BrownLightPrimaryContainer,
            onPrimaryContainer = BrownLightOnPrimaryContainer
        )
    }

    ColorTheme.CYAN -> if (isDark) {
        darkColorScheme(
            primary = CyanDarkPrimary,
            onPrimary = CyanDarkOnPrimary,
            primaryContainer = CyanDarkPrimaryContainer,
            onPrimaryContainer = CyanDarkOnPrimaryContainer
        )
    } else {
        lightColorScheme(
            primary = CyanLightPrimary,
            onPrimary = CyanLightOnPrimary,
            primaryContainer = CyanLightPrimaryContainer,
            onPrimaryContainer = CyanLightOnPrimaryContainer
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
    ColorTheme.TEAL -> TealLightPrimary
    ColorTheme.INDIGO -> IndigoLightPrimary
    ColorTheme.BROWN -> BrownLightPrimary
    ColorTheme.CYAN -> CyanLightPrimary
}
