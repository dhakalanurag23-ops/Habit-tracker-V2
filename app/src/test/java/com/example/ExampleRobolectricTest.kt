package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.AppThemePreset
import com.example.util.IconSwitcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HabitPulse", appName)
  }

  @Test
  fun `test icon switcher component aliases`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = IconSwitcher.switchLauncherIcon(context, AppThemePreset.NOTHING_UI)
    assertNotNull(result)
  }

  @Test
  fun `test localization dictionary mappings for all variants`() {
    val usStrings = com.example.util.getAppStrings(com.example.util.AppLanguage.ENGLISH_US)
    val indiaStrings = com.example.util.getAppStrings(com.example.util.AppLanguage.ENGLISH_INDIA)
    val onlineStrings = com.example.util.getAppStrings(com.example.util.AppLanguage.CHRONICALLY_ONLINE)

    // US Mappings
    assertEquals("Settings & Customization", usStrings.settingsTitle)
    assertEquals("Gemini API Key", usStrings.geminiApiKeyParam)
    assertEquals("Run Prompt", usStrings.geminiRunPromptParam)
    assertEquals("Temperature", usStrings.geminiTemperatureParam)
    assertEquals("Top-K", usStrings.geminiTopKParam)

    // India Mappings
    assertEquals("Settings (Pura Control)", indiaStrings.settingsTitle)
    assertEquals("Gemini API Key (Keep it safe da)", indiaStrings.geminiApiKeyParam)
    assertEquals("Chalao", indiaStrings.geminiRunPromptParam)
    assertEquals("Creativity Level", indiaStrings.geminiTemperatureParam)

    // Chronically Online Mappings
    assertEquals("Side Quest Config", onlineStrings.settingsTitle)
    assertEquals("Secret Lore / Token Drop", onlineStrings.geminiApiKeyParam)
    assertEquals("Let Him Cook", onlineStrings.geminiRunPromptParam)
    assertEquals("Cooking Index", onlineStrings.geminiTemperatureParam)
    assertEquals("Vibe Check / Randomness", onlineStrings.geminiTopKParam)
  }

  @Test
  fun `test vision scanner tile service persistent toggle state and constants`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Set toggle state to true
    com.example.util.VisionScannerTileService.setToggleActive(context, true)
    assertEquals(true, com.example.util.VisionScannerTileService.isToggleActive(context))

    // Set toggle state to false
    com.example.util.VisionScannerTileService.setToggleActive(context, false)
    assertEquals(false, com.example.util.VisionScannerTileService.isToggleActive(context))

    // Toggle persistent state
    val toggled = com.example.util.VisionScannerTileService.togglePersistentState(context)
    assertEquals(true, toggled)
    assertEquals(true, com.example.util.VisionScannerTileService.isToggleActive(context))

    // Verify Action and Extra constants
    assertEquals("com.example.ACTION_GEMINI_VISION_SCAN", com.example.util.VisionScannerTileService.ACTION_GEMINI_VISION_SCAN)
    assertEquals("EXTRA_OPEN_AI_VISION", com.example.util.VisionScannerTileService.EXTRA_OPEN_AI_VISION)
    assertEquals("EXTRA_LAUNCH_CAMERA", com.example.util.VisionScannerTileService.EXTRA_LAUNCH_CAMERA)
  }
}
