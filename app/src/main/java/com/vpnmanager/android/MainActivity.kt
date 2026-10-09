package com.vpnmanager.android

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.vpnmanager.android.service.AppVpnService
import com.vpnmanager.android.ui.screens.DashboardScreen
import com.vpnmanager.android.ui.theme.VPNManagerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as VpnApplication
        val repository = app.repository

        setContent {
            VPNManagerTheme {
                DashboardScreen(
                    repository = repository,
                    onToggleVpn = {
                        val isConnected = repository.isConnected.value
                        if (isConnected) {
                            stopVpnService()
                        } else {
                            prepareAndStartVpn()
                        }
                    },
                    onPingAll = {
                        pingAllTunnels()
                    }
                )
            }
        }
    }

    private fun prepareAndStartVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, AppVpnService::class.java).apply {
            action = AppVpnService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopVpnService() {
        val intent = Intent(this, AppVpnService::class.java).apply {
            action = AppVpnService.ACTION_STOP
        }
        startService(intent)
    }

    private fun pingAllTunnels() {
        val repo = (application as VpnApplication).repository
        lifecycleScope.launch {
            repo.tunnels.value.forEach { tunnel ->
                // Simulated ping test (or actual TCP probe to tunnel endpoint)
                delay(300)
                val testLatency = when (tunnel.type) {
                    com.vpnmanager.android.data.model.TunnelType.WIREGUARD -> (380..450).random().toLong()
                    com.vpnmanager.android.data.model.TunnelType.OPENVPN -> (30..80).random().toLong()
                    com.vpnmanager.android.data.model.TunnelType.VRAY -> (800..1400).random().toLong()
                    else -> (20..50).random().toLong()
                }
                repo.updateTunnelLatency(tunnel.id, testLatency)
            }
        }
    }
}
