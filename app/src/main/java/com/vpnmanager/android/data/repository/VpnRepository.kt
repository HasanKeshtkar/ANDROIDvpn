package com.vpnmanager.android.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vpnmanager.android.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class VpnRepository(private val context: Context) {

    private val gson = Gson()
    private val stateFile = File(context.filesDir, "vpn_state.json")

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _tunnels = MutableStateFlow<List<Tunnel>>(emptyList())
    val tunnels: StateFlow<List<Tunnel>> = _tunnels.asStateFlow()

    private val _routes = MutableStateFlow<List<RouteRule>>(emptyList())
    val routes: StateFlow<List<RouteRule>> = _routes.asStateFlow()

    private val _defaultTunnel = MutableStateFlow(TunnelType.WIREGUARD)
    val defaultTunnel: StateFlow<TunnelType> = _defaultTunnel.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<ConnectionLog>>(emptyList())
    val recentLogs: StateFlow<List<ConnectionLog>> = _recentLogs.asStateFlow()

    private val _trafficStats = MutableStateFlow(TrafficStats())
    val trafficStats: StateFlow<TrafficStats> = _trafficStats.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        if (stateFile.exists()) {
            try {
                val json = stateFile.readText()
                val state = gson.fromJson(json, PersistedState::class.java)
                _tunnels.value = state.tunnels
                _routes.value = state.routes
                _defaultTunnel.value = TunnelType.fromId(state.defaultTunnel)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Initial defaults mimicking the user's Linux setup
        _tunnels.value = listOf(
            Tunnel(
                id = "vray-1",
                type = TunnelType.VRAY,
                name = "vray",
                endpoint = "current",
                latencyMs = 1290,
                isEnabled = true
            ),
            Tunnel(
                id = "ovpn-1",
                type = TunnelType.OPENVPN,
                name = "openvpn",
                endpoint = "ut · vpn.ut.ac.ir",
                latencyMs = 38,
                isEnabled = true
            ),
            Tunnel(
                id = "wg-1",
                type = TunnelType.WIREGUARD,
                name = "wireguard",
                endpoint = "usa11978_11971 · usa81.picofile.online",
                latencyMs = 424,
                isEnabled = true
            )
        )

        _routes.value = listOf(
            RouteRule("r1", "YouTube", RuleKind.SITE, listOf("youtube.com", "googlevideo.com"), TunnelType.OPENVPN),
            RouteRule("r2", "Claude", RuleKind.SITE, listOf("claude.ai", "anthropic.com"), TunnelType.VRAY),
            RouteRule("r3", "WhatsApp", RuleKind.SITE, listOf("whatsapp.com", "wa.me"), TunnelType.WIREGUARD),
            RouteRule("r4", "gemini.google.com", RuleKind.SITE, listOf("gemini.google.com"), TunnelType.WIREGUARD),
            RouteRule("r5", "Telegram", RuleKind.APP, listOf("org.telegram.messenger", "org.telegram.plus"), TunnelType.WIREGUARD),
            RouteRule("r6", "Agent", RuleKind.APP, listOf("com.anthropic.claude"), TunnelType.VRAY),
            RouteRule("r7", "Chromium", RuleKind.APP, listOf("com.android.chrome", "org.chromium.chrome"), TunnelType.WIREGUARD)
        )

        _recentLogs.value = listOf(
            ConnectionLog(destination = "172.217.117.4", outbound = TunnelType.WIREGUARD),
            ConnectionLog(destination = "142.251.127.188", outbound = TunnelType.WIREGUARD),
            ConnectionLog(destination = "95.216.195.133", outbound = TunnelType.WIREGUARD),
            ConnectionLog(destination = "142.250.154.139", outbound = TunnelType.OPENVPN),
            ConnectionLog(destination = "85.220.190.246", outbound = TunnelType.WIREGUARD),
            ConnectionLog(destination = "172.217.113.4", outbound = TunnelType.WIREGUARD),
            ConnectionLog(destination = "142.250.202.163", outbound = TunnelType.WIREGUARD),
            ConnectionLog(destination = "142.251.156.119", outbound = TunnelType.WIREGUARD)
        )

        saveState()
    }

    fun saveState() {
        try {
            val state = PersistedState(
                tunnels = _tunnels.value,
                routes = _routes.value,
                defaultTunnel = _defaultTunnel.value.id
            )
            stateFile.writeText(gson.toJson(state))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    fun updateTrafficStats(rxSpeed: Long, txSpeed: Long) {
        val current = _trafficStats.value
        _trafficStats.value = current.copy(
            rxSpeedBps = rxSpeed,
            txSpeedBps = txSpeed,
            totalRxBytes = current.totalRxBytes + rxSpeed,
            totalTxBytes = current.totalTxBytes + txSpeed
        )
    }

    fun addConnectionLog(destination: String, outbound: TunnelType) {
        val updated = listOf(ConnectionLog(destination = destination, outbound = outbound)) + _recentLogs.value
        _recentLogs.value = updated.take(15) // Keep last 15
    }

    fun updateTunnelLatency(tunnelId: String, latency: Long?) {
        _tunnels.value = _tunnels.value.map {
            if (it.id == tunnelId) it.copy(latencyMs = latency) else it
        }
    }

    fun toggleTunnel(tunnelId: String) {
        _tunnels.value = _tunnels.value.map {
            if (it.id == tunnelId) it.copy(isEnabled = !it.isEnabled) else it
        }
        saveState()
    }

    fun updateRouteRule(ruleId: String, newVia: TunnelType) {
        _routes.value = _routes.value.map {
            if (it.id == ruleId) it.copy(via = newVia) else it
        }
        saveState()
    }

    fun addRouteRule(rule: RouteRule) {
        _routes.value = _routes.value + rule
        saveState()
    }

    fun removeRouteRule(ruleId: String) {
        _routes.value = _routes.value.filter { it.id != ruleId }
        saveState()
    }

    fun setDefaultTunnel(type: TunnelType) {
        _defaultTunnel.value = type
        saveState()
    }

    private data class PersistedState(
        val tunnels: List<Tunnel>,
        val routes: List<RouteRule>,
        val defaultTunnel: String
    )
}
