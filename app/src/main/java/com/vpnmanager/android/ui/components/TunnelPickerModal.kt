package com.vpnmanager.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vpnmanager.android.data.model.RouteRule
import com.vpnmanager.android.data.model.TunnelType
import com.vpnmanager.android.ui.theme.*

@Composable
fun TunnelPickerModal(
    rule: RouteRule,
    onDismiss: () -> Unit,
    onSelectTunnel: (TunnelType) -> Unit,
    onDeleteRule: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = BgCard,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "ROUTE: ${rule.name}",
                    color = AccentBlue,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Text(
                    text = rule.targets.joinToString(", "),
                    color = TextDim,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SELECT DESTINATION TUNNEL:",
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    TunnelType.WIREGUARD,
                    TunnelType.VRAY,
                    TunnelType.OPENVPN,
                    TunnelType.DIRECT,
                    TunnelType.BLOCK
                ).forEach { tunnel ->
                    val isCurrent = rule.via == tunnel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCurrent) BorderDark else BgCard)
                            .clickable {
                                onSelectTunnel(tunnel)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "●",
                            color = tunnel.color,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tunnel.displayName,
                            color = if (isCurrent) TextPrimary else TextMuted,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (isCurrent) {
                            Text(
                                text = "ACTIVE",
                                color = AccentDirect,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        onDeleteRule()
                        onDismiss()
                    }) {
                        Text("DELETE RULE", color = AccentBlock, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                    TextButton(onClick = onDismiss) {
                        Text("CLOSE", color = TextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
