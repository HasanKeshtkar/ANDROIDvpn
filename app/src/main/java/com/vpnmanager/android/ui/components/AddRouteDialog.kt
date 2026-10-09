package com.vpnmanager.android.ui.components

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vpnmanager.android.data.model.RouteRule
import com.vpnmanager.android.data.model.RuleKind
import com.vpnmanager.android.data.model.TunnelType
import com.vpnmanager.android.ui.theme.*

data class AppItem(
    val label: String,
    val packageName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRouteDialog(
    onDismiss: () -> Unit,
    onAddRule: (RouteRule) -> Unit
) {
    val context = LocalContext.current
    var selectedKind by remember { mutableStateOf(RuleKind.APP) }
    var selectedTunnel by remember { mutableStateOf(TunnelType.WIREGUARD) }
    var domainInput by remember { mutableStateOf("") }
    var ruleName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // Query installed apps
    val installedApps = remember {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packages
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName.contains("chrome") }
            .map {
                AppItem(
                    label = pm.getApplicationLabel(it).toString(),
                    packageName = it.packageName
                )
            }
            .sortedBy { it.label }
    }

    var selectedApp by remember { mutableStateOf<AppItem?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(12.dp),
            color = BgCard,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "◆ ADD ROUTE",
                    color = AccentBlue,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle App vs Site
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedKind = RuleKind.APP },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedKind == RuleKind.APP) AccentBlue else BorderDark
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("App Rule", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { selectedKind = RuleKind.SITE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedKind == RuleKind.SITE) AccentBlue else BorderDark
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Site Rule", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tunnel Selector
                Text("ROUTE VIA:", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(TunnelType.WIREGUARD, TunnelType.VRAY, TunnelType.OPENVPN, TunnelType.DIRECT).forEach { tunnel ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selectedTunnel == tunnel) tunnel.color.copy(alpha = 0.25f) else BorderDark)
                                .clickable { selectedTunnel = tunnel }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tunnel.displayName,
                                color = if (selectedTunnel == tunnel) tunnel.color else TextDim,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedKind == RuleKind.SITE) {
                    OutlinedTextField(
                        value = ruleName,
                        onValueChange = { ruleName = it },
                        label = { Text("Rule Name (e.g. YouTube)", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = domainInput,
                        onValueChange = { domainInput = it },
                        label = { Text("Domains (comma-separated)", fontFamily = FontFamily.Monospace) },
                        placeholder = { Text("youtube.com, googlevideo.com", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search installed apps...", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val filtered = installedApps.filter {
                            it.label.contains(searchQuery, ignoreCase = true) ||
                            it.packageName.contains(searchQuery, ignoreCase = true)
                        }
                        items(filtered) { app ->
                            val isChosen = selectedApp?.packageName == app.packageName
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isChosen) AccentBlue.copy(alpha = 0.2f) else BgCard)
                                    .clickable {
                                        selectedApp = app
                                        ruleName = app.label
                                    }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.label, color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                    Text(app.packageName, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                                if (isChosen) {
                                    Text("✓", color = AccentBlue, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = TextMuted, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedKind == RuleKind.SITE && domainInput.isNotBlank()) {
                                val domains = domainInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                val finalName = if (ruleName.isNotBlank()) ruleName else domains.first()
                                onAddRule(
                                    RouteRule(
                                        id = java.util.UUID.randomUUID().toString(),
                                        name = finalName,
                                        kind = RuleKind.SITE,
                                        targets = domains,
                                        via = selectedTunnel
                                    )
                                )
                                onDismiss()
                            } else if (selectedKind == RuleKind.APP && selectedApp != null) {
                                onAddRule(
                                    RouteRule(
                                        id = java.util.UUID.randomUUID().toString(),
                                        name = selectedApp!!.label,
                                        kind = RuleKind.APP,
                                        targets = listOf(selectedApp!!.packageName),
                                        via = selectedTunnel
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("SAVE RULE", fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
