package com.vpnmanager.android.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.vpnmanager.android.VpnApplication

@RequiresApi(Build.VERSION_CODES.N)
class VpnTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val repo = (application as VpnApplication).repository
        val isConnected = repo.isConnected.value

        val intent = Intent(this, AppVpnService::class.java).apply {
            action = if (isConnected) AppVpnService.ACTION_STOP else AppVpnService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        updateTileState(!isConnected)
    }

    private fun updateTileState(overrideState: Boolean? = null) {
        val repo = (application as? VpnApplication)?.repository ?: return
        val isConnected = overrideState ?: repo.isConnected.value
        qsTile?.apply {
            state = if (isConnected) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = "VPN"
            updateTile()
        }
    }
}
