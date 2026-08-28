package com.example.util

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.example.MainActivity
import com.example.R

/**
 * Quick Settings Tile Service for Gemini Vision Camera Scanning.
 * Operates as a persistent toggle in Android Quick Settings shade to instantly trigger
 * multimodal camera scanning for notes, schedules, whiteboards, and meals.
 */
class VisionScannerTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(TAG, "VisionScannerTileService added to Quick Settings panel")
        setToggleActive(this, true)
        refreshTileUi()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d(TAG, "VisionScannerTileService removed from Quick Settings panel")
        setToggleActive(this, false)
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "VisionScannerTileService started listening")
        refreshTileUi()
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "VisionScannerTileService stopped listening")
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "VisionScannerTile clicked - initiating Gemini Vision camera scanner")

        // Ensure persistent toggle remains active
        setToggleActive(this, true)

        val tile = qsTile
        if (tile != null) {
            tile.state = Tile.STATE_ACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = getString(R.string.tile_vision_scanner_subtitle_active)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                tile.stateDescription = "Active"
            }
            tile.updateTile()
        }

        launchVisionScannerActivity()
    }

    /**
     * Refreshes the Tile UI based on persisted toggle state.
     */
    private fun refreshTileUi() {
        val tile = qsTile ?: return
        val isActive = isToggleActive(this)

        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_vision_scanner_label)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isActive) {
                getString(R.string.tile_vision_scanner_subtitle_active)
            } else {
                getString(R.string.tile_vision_scanner_subtitle_inactive)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            tile.stateDescription = if (isActive) "Active" else "Inactive"
        }

        tile.contentDescription = getString(R.string.tile_vision_scanner_description)

        try {
            tile.icon = Icon.createWithResource(this, R.drawable.ic_vision_scanner)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load custom icon, using default", e)
        }

        tile.updateTile()
    }

    /**
     * Launches MainActivity with flags and extras to directly start Gemini Vision camera scanning.
     * Safely handles lock screen state and Android 14+ PendingIntent requirements.
     */
    private fun launchVisionScannerActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_GEMINI_VISION_SCAN
            putExtra(EXTRA_OPEN_AI_VISION, true)
            putExtra(EXTRA_LAUNCH_CAMERA, true)
            putExtra(EXTRA_FROM_QS_TILE, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val launchAction = Runnable {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val pendingIntent = PendingIntent.getActivity(
                        this,
                        REQUEST_CODE_SCAN,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    startActivityAndCollapse(pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting Activity from Quick Settings Tile", e)
            }
        }

        if (isLocked) {
            unlockAndRun(launchAction)
        } else {
            launchAction.run()
        }
    }

    companion object {
        private const val TAG = "VisionScannerTile"
        private const val REQUEST_CODE_SCAN = 4040

        const val ACTION_GEMINI_VISION_SCAN = "com.example.ACTION_GEMINI_VISION_SCAN"
        const val EXTRA_OPEN_AI_VISION = "EXTRA_OPEN_AI_VISION"
        const val EXTRA_LAUNCH_CAMERA = "EXTRA_LAUNCH_CAMERA"
        const val EXTRA_FROM_QS_TILE = "EXTRA_FROM_QS_TILE"

        private const val PREFS_NAME = "vision_scanner_tile_prefs"
        private const val KEY_TOGGLE_ACTIVE = "key_vision_scanner_toggle_active"

        /**
         * Checks if the Quick Settings toggle is persisted as active.
         */
        fun isToggleActive(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_TOGGLE_ACTIVE, true)
        }

        /**
         * Persists the toggle state and requests SystemUI to update the tile if bound.
         */
        fun setToggleActive(context: Context, active: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_TOGGLE_ACTIVE, active).apply()
            requestTileUpdate(context)
        }

        /**
         * Toggles the persisted state.
         */
        fun togglePersistentState(context: Context): Boolean {
            val newState = !isToggleActive(context)
            setToggleActive(context, newState)
            return newState
        }

        /**
         * Requests the system to re-read the tile's state.
         */
        fun requestTileUpdate(context: Context) {
            try {
                requestListeningState(
                    context,
                    ComponentName(context, VisionScannerTileService::class.java)
                )
            } catch (e: Exception) {
                Log.w(TAG, "Cannot request listening state (Service might not be registered or active yet)", e)
            }
        }
    }
}
