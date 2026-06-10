package com.yourcompany.pos.presentation.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.yourcompany.pos.domain.model.Product
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.NeonPink
import com.yourcompany.pos.presentation.theme.SurfaceElevated
import com.yourcompany.pos.presentation.theme.SurfaceBorder
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary
import java.util.Locale

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "scale")
    
    val isLowStock = product.stockQuantity in 0..product.lowStockThreshold
    val isOutOfStock = product.stockQuantity == 0
    val cardBorderColor = if (isLowStock || isOutOfStock) NeonPink else NeonCyan

    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 16.dp, 
                shape = shape, 
                ambientColor = cardBorderColor.copy(alpha = 0.15f), 
                spotColor = cardBorderColor.copy(alpha = 0.25f)
            )
            .border(if (isLowStock) 2.dp else 1.dp, Brush.linearGradient(listOf(SurfaceBorder, cardBorderColor.copy(alpha = if (isLowStock) 0.6f else 0.35f))), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material.ripple.rememberRipple(color = NeonCyan),
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated.copy(alpha = 0.85f)),
        shape = shape
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = product.name, color = TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Text(text = if (product.isActive) "ONLINE" else "OFF", color = if (product.isActive) NeonCyan else TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            }
            Text(text = product.sku, color = TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            Text(
                text = String.format(Locale.getDefault(), "NT$ %.2f", product.price),
                color = if (isLowStock) NeonPink else NeonCyan,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "稅率 ${(product.taxRate * 100).toInt()}%",
                    color = TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
                if (product.stockQuantity != -1) {
                    Text(
                        text = if (isOutOfStock) "已售完" else "庫存: ${product.stockQuantity}",
                        color = if (isLowStock) NeonPink else TextSecondary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = if (isLowStock) androidx.compose.ui.text.font.FontWeight.Bold else null
                    )
                }
            }
        }
    }
}
