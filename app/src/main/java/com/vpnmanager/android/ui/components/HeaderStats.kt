package com.vpnmanager.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnmanager.android.data.model.TrafficStats
import com.vpnmanager.android.ui.theme.*

@Composable
fun HeaderStats(
    isConnected: Boolean,
    trafficStats: TrafficStats,
    onToggleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor by animateColorAsState(
        targetValue = if (isConnected) AccentBlue else TextDim,
        label = "indicatorColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Indicator and Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onToggleClick() }
        ) {
            Text(
                text = if (isConnected) "◆" else "◇",
                color = indicatorColor,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "vpn",
                color = if (isConnected) TextPrimary else TextMuted,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            if (isConnected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "active",
                    color = AccentDirect,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Right: Speeds ↓ / ↑
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "↓ ${trafficStats.formattedRxSpeed()}",
                color = if (isConnected) TextMuted else TextDim,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "↑ ${trafficStats.formattedTxSpeed()}",
                color = if (isConnected) TextMuted else TextDim,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
