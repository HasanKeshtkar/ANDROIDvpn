package com.vpnmanager.android.data.model

import androidx.compose.ui.graphics.Color

enum class TunnelType(
    val id: String,
    val displayName: String,
    val color: Color
) {
    VRAY("vray", "vray", Color(0xFFD183E8)),           // Magenta / Purple
    OPENVPN("openvpn", "openvpn", Color(0xFF4EC9B0)),    // Teal / Green
    WIREGUARD("wireguard", "wireguard", Color(0xFFE5C07B)), // Orange / Yellow
    DIRECT("direct", "direct", Color(0xFF98C379)),       // Light Green
    BLOCK("block", "block", Color(0xFFE06C75));          // Red

    companion object {
        fun fromId(id: String): TunnelType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: DIRECT
        }
    }
}
