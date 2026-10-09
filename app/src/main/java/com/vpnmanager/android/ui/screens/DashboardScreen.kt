package com.vpnmanager.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnmanager.android.data.model.RouteRule
import com.vpnmanager.android.data.model.RuleKind
import com.vpnmanager.android.data.model.TunnelType
import com.vpnmanager.android.data.repository.VpnRepository
import com.vpnmanager.android.ui.components.*
import com.vpnmanager.android.ui.theme.*

@Composable
fun DashboardScreen(
    repository: VpnRepository,
    onToggleVpn: () -> Unit,
    onPingAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected by repository.isConnected.collectAsState()
    val trafficStats by repository.trafficStats.collectAsState()
    val tunnels by repository.tunnels.collectAsState()
    val routes by repository.routes.collectAsState()
    val recentLogs by repository.recentLogs.collectAsState()
    val defaultTunnel by repository.defaultTunnel.collectAsState()

    var editingRule by remember { mutableStateOf<RouteRule?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            // Authentic terminal command bar footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Connect / Disconnect action
                Row(
                    modifier = Modifier.clickable { onToggleVpn() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("↵", color = AccentBlue, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isConnected) "disconnect" else "connect",
                        color = if (isConnected) AccentVray else AccentDirect,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Add route action
                Row(
                    modifier = Modifier.clickable { showAddDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("+", color = AccentBlue, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("route", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }

                // Ping action
                Row(
                    modifier = Modifier.clickable { onPingAll() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("p", color = AccentBlue, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ping", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }

                // Default fallback picker
                Row(
                    modifier = Modifier.clickable {
                        // Cycle default fallback tunnel
                        val next = when (defaultTunnel) {
                            TunnelType.WIREGUARD -> TunnelType.VRAY
                            TunnelType.VRAY -> TunnelType.OPENVPN
                            TunnelType.OPENVPN -> TunnelType.DIRECT
                            else -> TunnelType.WIREGUARD
                        }
                        repository.setDefaultTunnel(next)
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("d", color = AccentBlue, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("default: ${defaultTunnel.displayName}", color = defaultTunnel.color, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Top Header with connection status and speeds
            item {
                HeaderStats(
                    isConnected = isConnected,
                    trafficStats = trafficStats,
                    onToggleClick = onToggleVpn
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 2. TUNNELS Section
            item {
                Text(
                    text = "TUNNELS",
                    color = TextDim,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                tunnels.forEachIndexed { index, tunnel ->
                    TunnelItem(
                        tunnel = tunnel,
                        isSelected = index == 0,
                        onToggle = { repository.toggleTunnel(tunnel.id) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. ROUTES Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROUTES",
                        color = TextDim,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+ add",
                        color = AccentBlue,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { showAddDialog = true }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                routes.forEach { rule ->
                    val detail = when (rule.kind) {
                        RuleKind.SITE -> {
                            val first = rule.targets.firstOrNull() ?: ""
                            val extra = rule.targets.size - 1
                            if (extra > 0) "$first +$extra" else first
                        }
                        RuleKind.APP -> {
                            val first = rule.targets.firstOrNull()?.substringAfterLast(".") ?: ""
                            val extra = rule.targets.size - 1
                            if (extra > 0) "$first +$extra" else first
                        }
                    }
                    RouteRow(
                        name = rule.name,
                        kind = rule.kind,
                        detail = detail,
                        via = rule.via,
                        onClick = { editingRule = rule }
                    )
                }

                // Fallback default rule
                RouteRow(
                    name = "everything else",
                    kind = null,
                    detail = "",
                    via = defaultTunnel,
                    isFallback = true,
                    onClick = {
                        val next = when (defaultTunnel) {
                            TunnelType.WIREGUARD -> TunnelType.VRAY
                            TunnelType.VRAY -> TunnelType.OPENVPN
                            else -> TunnelType.WIREGUARD
                        }
                        repository.setDefaultTunnel(next)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. RECENT Section
            item {
                Text(
                    text = "RECENT",
                    color = TextDim,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                RecentLogView(logs = recentLogs)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal to change route tunnel destination
    editingRule?.let { rule ->
        TunnelPickerModal(
            rule = rule,
            onDismiss = { editingRule = null },
            onSelectTunnel = { newTunnel ->
                repository.updateRouteRule(rule.id, newTunnel)
            },
            onDeleteRule = {
                repository.removeRouteRule(rule.id)
            }
        )
    }

    // Dialog to add new App or Site route
    if (showAddDialog) {
        AddRouteDialog(
            onDismiss = { showAddDialog = false },
            onAddRule = { newRule ->
                repository.addRouteRule(newRule)
            }
        )
    }
}
