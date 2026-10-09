package com.vpnmanager.android.data.model

enum class RuleKind {
    APP,
    SITE
}

data class RouteRule(
    val id: String,
    val name: String,
    val kind: RuleKind,
    val targets: List<String>, // Package names (e.g. "org.telegram.messenger") or domains (e.g. "claude.ai")
    val via: TunnelType
)
