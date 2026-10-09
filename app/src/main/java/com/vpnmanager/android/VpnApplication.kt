package com.vpnmanager.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.vpnmanager.android.data.repository.VpnRepository

class VpnApplication : Application() {

    companion object {
        const val CHANNEL_ID = "vpn_channel"
        lateinit var instance: VpnApplication
            private set
    }

    lateinit var repository: VpnRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        repository = VpnRepository(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active VPN tunnel and routing status"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
