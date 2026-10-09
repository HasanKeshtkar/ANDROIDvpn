package com.vpnmanager.android.data.model

data class TrafficStats(
    val rxSpeedBps: Long = 0,
    val txSpeedBps: Long = 0,
    val totalRxBytes: Long = 0,
    val totalTxBytes: Long = 0
) {
    fun formattedRxSpeed(): String = formatBytes(rxSpeedBps) + "/s"
    fun formattedTxSpeed(): String = formatBytes(txSpeedBps) + "/s"

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> String.format(java.util.Locale.US, "%.0f KB", bytes.toDouble() / 1024)
            else -> "$bytes B"
        }
    }
}
