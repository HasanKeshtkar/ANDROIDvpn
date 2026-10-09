package com.vpnmanager.android.data.generator

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vpnmanager.android.data.model.RouteRule
import com.vpnmanager.android.data.model.RuleKind
import com.vpnmanager.android.data.model.Tunnel
import com.vpnmanager.android.data.model.TunnelType

class SingBoxConfigGenerator {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun generateConfig(
        tunnels: List<Tunnel>,
        rules: List<RouteRule>,
        defaultTunnel: TunnelType = TunnelType.WIREGUARD
    ): String {
        val root = JsonObject()

        // 1. Log configuration
        val log = JsonObject().apply {
            addProperty("level", "warn")
            addProperty("timestamp", true)
        }
        root.add("log", log)

        // 2. DNS configuration
        val dns = JsonObject().apply {
            val servers = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("tag", "remote-dns")
                    addProperty("address", "tls://1.1.1.1")
                })
                add(JsonObject().apply {
                    addProperty("tag", "direct-dns")
                    addProperty("address", "https://1.1.1.1/dns-query")
                    addProperty("detour", "direct")
                })
            }
            add("servers", servers)
            add("rules", JsonArray())
        }
        root.add("dns", dns)

        // 3. Inbounds: Single TUN interface capturing Android traffic with domain sniffing
        val inbounds = JsonArray().apply {
            add(JsonObject().apply {
                addProperty("type", "tun")
                addProperty("tag", "tun-in")
                addProperty("interface_name", "tun0")
                add("inet4_address", JsonArray().apply { add("172.19.0.1/30") })
                addProperty("auto_route", true)
                addProperty("strict_route", true)
                addProperty("sniff", true)
                addProperty("sniff_override_destination", true)
            })
        }
        root.add("inbounds", inbounds)

        // 4. Outbounds: Direct, Block, DNS, and User Tunnels
        val outbounds = JsonArray().apply {
            // Built-in basic outbounds
            add(JsonObject().apply {
                addProperty("type", "direct")
                addProperty("tag", "direct")
            })
            add(JsonObject().apply {
                addProperty("type", "block")
                addProperty("tag", "block")
            })
            add(JsonObject().apply {
                addProperty("type", "dns")
                addProperty("tag", "dns-out")
            })

            // Tunnels configured by user
            tunnels.filter { it.isEnabled }.forEach { tunnel ->
                val outbound = when (tunnel.type) {
                    TunnelType.WIREGUARD -> createWireGuardOutbound(tunnel)
                    TunnelType.VRAY -> createV2RayOutbound(tunnel)
                    TunnelType.OPENVPN -> createOpenVpnOutbound(tunnel)
                    else -> null
                }
                outbound?.let { add(it) }
            }
        }
        root.add("outbounds", outbounds)

        // 5. Routing rules: Per-App and Per-Site
        val route = JsonObject().apply {
            addProperty("auto_detect_interface", true)
            addProperty("final", defaultTunnel.id)

            val routingRules = JsonArray().apply {
                // Route DNS queries to dns-out
                add(JsonObject().apply {
                    addProperty("protocol", "dns")
                    addProperty("outbound", "dns-out")
                })
                // Route local LAN directly
                add(JsonObject().apply {
                    addProperty("ip_is_private", true)
                    addProperty("outbound", "direct")
                })

                // User rules
                rules.forEach { rule ->
                    val ruleObj = JsonObject().apply {
                        val targetsArray = JsonArray().apply {
                            rule.targets.forEach { add(it) }
                        }
                        when (rule.kind) {
                            RuleKind.APP -> add("package_name", targetsArray)
                            RuleKind.SITE -> add("domain_suffix", targetsArray)
                        }
                        addProperty("outbound", rule.via.id)
                    }
                    add(ruleObj)
                }
            }
            add("rules", routingRules)
        }
        root.add("route", route)

        // 6. Experimental: Clash API for live stats & recent connection monitoring
        val experimental = JsonObject().apply {
            val clashApi = JsonObject().apply {
                addProperty("external_controller", "127.0.0.1:9090")
                addProperty("secret", "vpn_local_secret")
            }
            add("clash_api", clashApi)
        }
        root.add("experimental", experimental)

        return gson.toJson(root)
    }

    private fun createWireGuardOutbound(tunnel: Tunnel): JsonObject {
        return JsonObject().apply {
            addProperty("type", "wireguard")
            addProperty("tag", tunnel.type.id)
            val parts = tunnel.endpoint.split(":")
            val host = parts.getOrNull(0) ?: "127.0.0.1"
            val port = parts.getOrNull(1)?.toIntOrNull() ?: 51820
            addProperty("server", host)
            addProperty("server_port", port)
            add("local_address", JsonArray().apply { add("10.0.0.2/32") })
            addProperty("mtu", 1420)
            // Real keys can be parsed from tunnel.configData
            addProperty("private_key", "sample_or_parsed_private_key")
            addProperty("peer_public_key", "sample_or_parsed_public_key")
        }
    }

    private fun createV2RayOutbound(tunnel: Tunnel): JsonObject {
        return JsonObject().apply {
            addProperty("type", "vless")
            addProperty("tag", tunnel.type.id)
            val parts = tunnel.endpoint.split(":")
            val host = parts.getOrNull(0) ?: "127.0.0.1"
            val port = parts.getOrNull(1)?.toIntOrNull() ?: 443
            addProperty("server", host)
            addProperty("server_port", port)
            addProperty("uuid", "sample-vless-uuid")
            addProperty("flow", "xtls-rprx-vision")
            val tls = JsonObject().apply {
                addProperty("enabled", true)
                addProperty("server_name", host)
            }
            add("tls", tls)
        }
    }

    private fun createOpenVpnOutbound(tunnel: Tunnel): JsonObject {
        return JsonObject().apply {
            addProperty("type", "openvpn")
            addProperty("tag", tunnel.type.id)
            val parts = tunnel.endpoint.split(":")
            val host = parts.getOrNull(0) ?: "127.0.0.1"
            val port = parts.getOrNull(1)?.toIntOrNull() ?: 1194
            addProperty("server", host)
            addProperty("server_port", port)
        }
    }
}
