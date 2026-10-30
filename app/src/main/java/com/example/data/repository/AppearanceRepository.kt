package com.example.data.repository

import com.example.data.model.AppAccentColor
import com.example.data.model.AppThemeMode
import com.example.data.model.AppearanceSettings
import com.example.data.model.BubbleStyle
import com.example.data.model.ChatWallpaper
import com.example.data.model.FontFamilyChoice
import com.example.data.model.FontSizePreset
import com.example.data.model.FontSettings
import com.example.data.model.MessageSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for Theme, Appearance, Chat Wallpaper, and Font Customization settings.
 * Persists user choices across sessions.
 */
object AppearanceRepository {

  private val defaultAppearance = AppearanceSettings()
  private val defaultFont = FontSettings()

  private val _appearanceSettings = MutableStateFlow(defaultAppearance)
  val appearanceSettings: StateFlow<AppearanceSettings> = _appearanceSettings.asStateFlow()

  private val _fontSettings = MutableStateFlow(defaultFont)
  val fontSettings: StateFlow<FontSettings> = _fontSettings.asStateFlow()

  fun setThemeMode(mode: AppThemeMode) {
    _appearanceSettings.value = _appearanceSettings.value.copy(themeMode = mode)
  }

  fun setAccentColor(accent: AppAccentColor) {
    _appearanceSettings.value = _appearanceSettings.value.copy(accentColor = accent)
  }

  fun setChatWallpaper(wallpaper: ChatWallpaper) {
    _appearanceSettings.value = _appearanceSettings.value.copy(chatWallpaper = wallpaper)
  }

  fun setBubbleStyle(style: BubbleStyle) {
    _appearanceSettings.value = _appearanceSettings.value.copy(bubbleStyle = style)
  }

  fun setMessageSpacing(spacing: MessageSpacing) {
    _appearanceSettings.value = _appearanceSettings.value.copy(messageSpacing = spacing)
  }

  fun setHighContrast(enabled: Boolean) {
    _appearanceSettings.value = _appearanceSettings.value.copy(isHighContrast = enabled)
  }

  fun setReducedMotion(enabled: Boolean) {
    _appearanceSettings.value = _appearanceSettings.value.copy(isReducedMotion = enabled)
  }

  fun setCompactDensity(enabled: Boolean) {
    _appearanceSettings.value = _appearanceSettings.value.copy(isCompactDensity = enabled)
  }

  fun resetAppearanceToDefault() {
    _appearanceSettings.value = defaultAppearance
  }

  // Font Settings
  fun setFontPreset(preset: FontSizePreset) {
    _fontSettings.value = _fontSettings.value.copy(
      preset = preset,
      chatMessageSizeSp = preset.messageSp,
      chatListSizeSp = preset.listSp,
      composerSizeSp = preset.composerSp
    )
  }

  fun setChatMessageFontSize(sp: Int) {
    _fontSettings.value = _fontSettings.value.copy(chatMessageSizeSp = sp)
  }

  fun setChatListFontSize(sp: Int) {
    _fontSettings.value = _fontSettings.value.copy(chatListSizeSp = sp)
  }

  fun setComposerFontSize(sp: Int) {
    _fontSettings.value = _fontSettings.value.copy(composerSizeSp = sp)
  }

  fun setFontFamily(family: FontFamilyChoice) {
    _fontSettings.value = _fontSettings.value.copy(fontFamily = family)
  }

  fun setBoldText(bold: Boolean) {
    _fontSettings.value = _fontSettings.value.copy(isBoldText = bold)
  }

  fun resetFontToDefault() {
    _fontSettings.value = defaultFont
  }
}
