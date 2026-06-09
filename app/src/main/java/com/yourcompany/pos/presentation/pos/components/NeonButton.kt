package com.yourcompany.pos.presentation.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.SurfaceElevated
import com.yourcompany.pos.presentation.theme.TextPrimary

@Composable
fun NeonButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glowColor: Color = NeonCyan,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = shape,
                ambientColor = glowColor.copy(alpha = 0.25f),
                spotColor = glowColor.copy(alpha = 0.35f)
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(glowColor.copy(alpha = 0.95f), glowColor.copy(alpha = 0.45f))
                    )
                ),
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .background(if (enabled) SurfaceElevated else SurfaceElevated.copy(alpha = 0.5f), shape)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextPrimary.copy(alpha = 0.4f),
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge
        )
    }
}
