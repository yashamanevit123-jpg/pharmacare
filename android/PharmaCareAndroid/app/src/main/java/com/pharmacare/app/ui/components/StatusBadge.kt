package com.pharmacare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pharmacare.app.ui.theme.*

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "in stock", "available", "completed", "paid", "good" -> StatusOkBg to StatusOk
        "low stock", "warning", "pending", "expiring_soon", "partial" -> StatusWarnBg to StatusWarn
        "out of stock", "critical", "expired", "failed", "cancelled" -> StatusBadBg to StatusBad
        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f) to MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = status,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
