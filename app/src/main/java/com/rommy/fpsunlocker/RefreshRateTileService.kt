package com.rommy.fpsunlocker

import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class RefreshRateTileService : TileService() {
    private val key = "gamecube_frame_interpolation_for_sr"
    private val enabledValue = "1:1:1:72:144"
    private val disabledValue = "0:-1:0:0:0"

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()

        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(android.app.PendingIntent.getActivity(this, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE))
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        val enable = !isEnabled()
        val value = if (enable) enabledValue else disabledValue
        val written = Settings.System.putString(contentResolver, key, value)
        val actual = Settings.System.getString(contentResolver, key)
        if (!written || actual != value) {
            updateTile()
            return
        }
        updateTile()
    }

    private fun isEnabled(): Boolean =
        Settings.System.getString(contentResolver, key) == enabledValue

    private fun updateTile() {
        val tile = qsTile ?: return
        tile.icon = Icon.createWithResource(this, R.drawable.ic_144hz_tile)
        tile.state = if (isEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isEnabled()) "144 Hz enabled" else "144 Hz disabled"
        }
        tile.updateTile()
    }
}
