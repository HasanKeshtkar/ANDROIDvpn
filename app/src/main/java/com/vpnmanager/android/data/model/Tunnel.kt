package com.vpnmanager.android.data.model

data class Tunnel(
    val id: String,
    val type: TunnelType,
    val name: String,
    val endpoint: String,
    val isEnabled: Boolean = true,
    val isConnected: Boolean = false,
    val latencyMs: Long? = null,
    val configData: String = "" // Raw link, ovpn, or wireguard conf
)
