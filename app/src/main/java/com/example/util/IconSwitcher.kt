package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.DesignAesthetic

/**
 * 2. Dynamic App Icon Swapping (Theme-Matching Launcher Icons)
 *
 * Implements launcher icon switching via Android's `PackageManager.setComponentEnabledSetting()`
 * paired with `<activity-alias>` declarations configured in `AndroidManifest.xml`.
 */
object IconSwitcher {

    private const val TAG = "IconSwitcher"

    // List of all activity aliases defined in AndroidManifest.xml
    private val ALL_ALIASES = listOf(
        "com.example.MainActivityAliasLiquid",
        "com.example.MainActivityAliasNothing",
        "com.example.MainActivityAliasMaterial",
        "com.example.MainActivityAliasMinimal",
        "com.example.MainActivityAliasMaximal"
    )

    /**
     * Map each DesignAesthetic to its corresponding Manifest activity-alias component name.
     */
    fun getAliasForAesthetic(aesthetic: DesignAesthetic): String {
        return aesthetic.aliasName
    }

    /**
     * Map each AppThemePreset to its corresponding Manifest activity-alias component name.
     */
    private fun getAliasForTheme(preset: AppThemePreset): String {
        return when (preset) {
            AppThemePreset.LIQUID_GLASS -> "com.example.MainActivityAliasLiquid"
            AppThemePreset.NOTHING_UI -> "com.example.MainActivityAliasNothing"
            AppThemePreset.MATERIAL_YOU -> "com.example.MainActivityAliasMaterial"
            AppThemePreset.MINIMALISM -> "com.example.MainActivityAliasMinimal"
            AppThemePreset.MAXIMALISM -> "com.example.MainActivityAliasMaximal"
        }
    }

    fun switchLauncherIconForAesthetic(context: Context, aesthetic: DesignAesthetic): Boolean {
        return try {
            val packageManager = context.packageManager
            val targetAlias = getAliasForAesthetic(aesthetic)

            Log.d(TAG, "Switching launcher icon to: $targetAlias")

            val targetComponent = ComponentName(context.packageName, targetAlias)
            packageManager.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            for (alias in ALL_ALIASES) {
                if (alias != targetAlias) {
                    val component = ComponentName(context.packageName, alias)
                    packageManager.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }

            Log.i(TAG, "Successfully activated launcher icon for ${aesthetic.title}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch launcher icon: ${e.message}", e)
            false
        }
    }

    /**
     * Switch the active launcher icon to match the selected theme preset.
     *
     * @param context Application context
     * @param targetPreset The theme preset whose icon should be activated
     * @return true if the switch succeeded, false otherwise
     */
    fun switchLauncherIcon(context: Context, targetPreset: AppThemePreset): Boolean {
        return try {
            val packageManager = context.packageManager
            val targetAlias = getAliasForTheme(targetPreset)

            Log.d(TAG, "Switching launcher icon to: $targetAlias")

            // Enable the target activity-alias first to avoid the launcher icon temporarily disappearing
            val targetComponent = ComponentName(context.packageName, targetAlias)
            packageManager.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // Disable all other activity-aliases
            for (alias in ALL_ALIASES) {
                if (alias != targetAlias) {
                    val component = ComponentName(context.packageName, alias)
                    packageManager.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }

            Log.i(TAG, "Successfully activated launcher icon for ${targetPreset.title}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch launcher icon: ${e.message}", e)
            false
        }
    }

    /**
     * Determine which launcher icon is currently enabled in the system.
     */
    fun getCurrentActiveAlias(context: Context): String {
        val packageManager = context.packageManager
        for (alias in ALL_ALIASES) {
            val component = ComponentName(context.packageName, alias)
            val state = packageManager.getComponentEnabledSetting(component)
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return alias
            }
        }
        return "com.example.MainActivityAliasLiquid" // Default fallback
    }
}
