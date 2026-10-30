package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.model.AppAccentColor
import com.example.data.model.AppThemeMode
import com.example.data.model.FontFamilyChoice
import com.example.data.repository.AppearanceRepository

private val DarkColorScheme = darkColorScheme(
  primary = TealPrimary,
  onPrimary = Color.White,
  primaryContainer = TealDarkContainer,
  onPrimaryContainer = Color(0xFFA7F3D0),
  secondary = AmberAccent,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF78350F),
  onSecondaryContainer = AmberContainer,
  tertiary = CheckmarkBlue,
  background = ChatBackgroundDark,
  onBackground = TextPrimaryDark,
  surface = SurfaceDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = TextSecondaryDark,
  outline = DividerDark
)

private val LightColorScheme = lightColorScheme(
  primary = TealDark,
  onPrimary = Color.White,
  primaryContainer = TealContainer,
  onPrimaryContainer = Color(0xFF00382E),
  secondary = AmberDark,
  onSecondary = Color.White,
  secondaryContainer = AmberContainer,
  onSecondaryContainer = Color(0xFF451A03),
  tertiary = CheckmarkBlue,
  background = SurfaceLight,
  onBackground = TextPrimaryLight,
  surface = SurfaceLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = TextSecondaryLight,
  outline = DividerLight
)

@Composable
fun ChatMitraTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val appearance by AppearanceRepository.appearanceSettings.collectAsState()
  val fontSettings by AppearanceRepository.fontSettings.collectAsState()

  val isDark = when (appearance.themeMode) {
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
    AppThemeMode.SYSTEM -> darkTheme
  }

  val accentColor = Color(appearance.accentColor.primaryHex)
  val accentDark = Color(appearance.accentColor.darkHex)

  val baseColorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    isDark -> DarkColorScheme.copy(
      primary = accentColor,
      primaryContainer = if (appearance.accentColor == AppAccentColor.TEAL_EMERALD) TealDarkContainer else accentColor.copy(alpha = 0.25f),
      outline = if (appearance.isHighContrast) Color.White.copy(alpha = 0.5f) else DividerDark
    )
    else -> LightColorScheme.copy(
      primary = accentDark,
      primaryContainer = if (appearance.accentColor == AppAccentColor.TEAL_EMERALD) TealContainer else accentColor.copy(alpha = 0.18f),
      outline = if (appearance.isHighContrast) Color.Black.copy(alpha = 0.6f) else DividerLight
    )
  }

  val selectedFontFamily = when (fontSettings.fontFamily) {
    FontFamilyChoice.SYSTEM_DEFAULT -> FontFamily.Default
    FontFamilyChoice.SANS_SERIF -> FontFamily.SansSerif
    FontFamilyChoice.SERIF -> FontFamily.Serif
    FontFamilyChoice.MONOSPACE -> FontFamily.Monospace
  }

  val customTypography = Typography.copy(
    bodyLarge = Typography.bodyLarge.copy(
      fontFamily = selectedFontFamily,
      fontWeight = if (fontSettings.isBoldText) FontWeight.Bold else FontWeight.Normal,
      fontSize = (fontSettings.chatListSizeSp).sp
    ),
    bodyMedium = Typography.bodyMedium.copy(
      fontFamily = selectedFontFamily,
      fontWeight = if (fontSettings.isBoldText) FontWeight.SemiBold else FontWeight.Normal,
      fontSize = (fontSettings.chatMessageSizeSp).sp
    ),
    bodySmall = Typography.bodySmall.copy(
      fontFamily = selectedFontFamily,
      fontSize = (fontSettings.chatMessageSizeSp - 2).coerceAtLeast(10).sp
    )
  )

  MaterialTheme(
    colorScheme = baseColorScheme,
    typography = customTypography,
    content = content
  )
}

