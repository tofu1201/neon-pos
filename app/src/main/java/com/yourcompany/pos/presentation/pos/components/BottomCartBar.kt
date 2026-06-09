package com.yourcompany.pos.presentation.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.TextPrimary
import java.util.Locale

@Composable
fun BottomCartBar(
    itemCount: Int,
    totalAmount: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (itemCount == 0) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NeonCyan.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.ShoppingCart, contentDescription = "購物車", tint = NeonCyan)
            Text(
                text = "查看購物車 ($itemCount) ｜ 前往結帳",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }
        
        Text(
            text = "NT$ ${String.format(Locale.getDefault(), "%.2f", totalAmount)}",
            style = MaterialTheme.typography.titleLarge,
            color = NeonCyan
        )
    }
}
