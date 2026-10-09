package com.vpnmanager.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnmanager.android.data.model.Tunnel
import com.vpnmanager.android.ui.theme.*

@Composable
fun TunnelItem(
    tunnel: Tunnel,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical indicator bar if selected/active
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .background(if (isSelected && tunnel.isEnabled) AccentBlue else BgDark)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Color-coded tunnel dot ●
        Text(
            text = "●",
            color = if (tunnel.isEnabled) tunnel.type.color else TextDim,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Tunnel name
        Text(
            text = tunnel.name,
            color = if (tunnel.isEnabled) TextPrimary else TextDim,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(80.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Endpoint description
        Text(
            text = tunnel.endpoint,
            color = if (tunnel.isEnabled) TextMuted else TextDim,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        // Right side: Latency in ms or IP
        Text(
            text = tunnel.latencyMs?.let { "$it ms" } ?: "---",
            color = if (tunnel.isEnabled) {
                when {
                    (tunnel.latencyMs ?: 9999) < 150 -> AccentDirect
                    (tunnel.latencyMs ?: 9999) < 600 -> AccentWireguard
                    else -> AccentVray
                }
            } else TextDim,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
