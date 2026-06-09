package com.yourcompany.pos.presentation.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yourcompany.pos.domain.model.Order
import com.yourcompany.pos.domain.model.OrderStatus
import com.yourcompany.pos.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecentOrdersDialog(
    orders: List<Order>,
    onDismiss: () -> Unit,
    onCancelOrder: (Long) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(24.dp)),
            color = SurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "歷史訂單",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                
                Divider(color = SurfaceBorder)

                if (orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "尚無訂單記錄", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders, key = { it.id }) { order ->
                            OrderItemRow(
                                order = order,
                                onCancelOrder = { onCancelOrder(order.id) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("關閉", color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(
    order: Order,
    onCancelOrder: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val timeString = formatter.format(Date(order.createdAt))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = order.orderNo,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(text = timeString, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "付款方式: ${order.paymentMethod}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "NT$ ${order.totalAmount}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            if (order.status == OrderStatus.CANCELLED) {
                Text(text = "已取消", color = NeonPink, style = MaterialTheme.typography.labelLarge)
            } else {
                TextButton(onClick = onCancelOrder) {
                    Text("取消訂單", color = NeonPink)
                }
            }
        }
    }
}
