package com.yourcompany.pos.presentation.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BluetoothConnected
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.WarningAmber
import com.yourcompany.pos.presentation.theme.Surface
import com.yourcompany.pos.presentation.theme.SurfaceBorder
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary

@Composable
fun StatusBanner(
    title: String,
    message: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, shape)
            .border(1.dp, SurfaceBorder.copy(alpha = 0.7f), shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = when {
                message.contains("缺紙") || message.contains("失敗") -> Icons.Outlined.WarningAmber
                title.contains("NFC") -> Icons.Outlined.NearMe
                else -> Icons.Outlined.BluetoothConnected
            },
            contentDescription = null,
            tint = color
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(text = message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
