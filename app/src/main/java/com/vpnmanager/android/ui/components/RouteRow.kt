package com.vpnmanager.android.ui.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnmanager.android.data.model.RouteRule
import com.vpnmanager.android.data.model.RuleKind
import com.vpnmanager.android.data.model.TunnelType
import com.vpnmanager.android.ui.theme.*

@Composable
fun RouteRow(
    name: String,
    kind: RuleKind?,
    detail: String,
    via: TunnelType,
    isFallback: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name (e.g. YouTube, Telegram, everything else)
        Text(
            text = name,
            color = if (isFallback) TextMuted else TextPrimary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isFallback) FontWeight.Normal else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(110.dp)
        )

        // Kind tag (site / app)
        if (kind != null) {
            Text(
                text = kind.name.lowercase(),
                color = TextDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(40.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }

        // Target detail
        Text(
            text = detail,
            color = TextMuted,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Arrow →
        Text(
            text = "→",
            color = TextDim,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Destination dot and name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(95.dp)
        ) {
            Text(
                text = "●",
                color = via.color,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = via.displayName,
                color = via.color,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
