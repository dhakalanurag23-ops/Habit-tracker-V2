package com.example.util

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.example.MainActivity

class ScanNotesTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.state = Tile.STATE_ACTIVE
        tile.label = "Scan Notes AI"
        tile.subtitle = "Gemini Vision"
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "Quick Settings Scan Notes tile clicked - launching Gemini Vision Camera")

        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_SCAN_NOTES
            putExtra(EXTRA_OPEN_AI_VISION, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    companion object {
        private const val TAG = "ScanNotesTileService"
        const val ACTION_SCAN_NOTES = "com.example.ACTION_SCAN_NOTES"
        const val EXTRA_OPEN_AI_VISION = "EXTRA_OPEN_AI_VISION"
    }
}
