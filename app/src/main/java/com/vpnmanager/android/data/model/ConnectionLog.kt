package com.vpnmanager.android.data.model

data class ConnectionLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val destination: String, // e.g. "172.217.117.4" or "claude.ai"
    val outbound: TunnelType,
    val timestamp: Long = System.currentTimeMillis()
)
