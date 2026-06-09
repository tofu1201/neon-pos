package com.yourcompany.pos.presentation.pos.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yourcompany.pos.domain.model.CartItem
import com.yourcompany.pos.presentation.theme.Background
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.NeonMint
import com.yourcompany.pos.presentation.theme.Surface
import com.yourcompany.pos.presentation.theme.SurfaceBorder
import com.yourcompany.pos.presentation.theme.SurfaceElevated
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartPanel(
    items: List<CartItem>,
    subtotal: Double,
    taxAmount: Double,
    discountAmount: Double,
    totalAmount: Double,
    cashReceivedInput: String,
    changeAmount: Double,
    itemCount: Int,
    compact: Boolean,
    onIncrease: (Long, Int) -> Unit,
    onDecrease: (Long, Int) -> Unit,
    onRemove: (Long) -> Unit,
    onCheckout: () -> Unit,
    onClear: () -> Unit,
    // Advanced features
    orderType: String = "外帶",
    tableNumber: String? = null,
    onSetOrderType: (String, String?) -> Unit = { _, _ -> },
    onHoldOrder: () -> Unit = {},
    onRetrieveOrder: () -> Unit = {},
    onAddDiscount: (Double) -> Unit = {}
) {
    val shape = RoundedCornerShape(24.dp)
    var showDiscountDialog by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!compact) Modifier.fillMaxHeight() else Modifier)
            .shadow(24.dp, shape, ambientColor = NeonMint.copy(alpha = 0.1f), spotColor = NeonMint.copy(alpha = 0.2f))
            .background(SurfaceElevated.copy(alpha = 0.95f), shape)
            .border(1.dp, Brush.linearGradient(listOf(SurfaceBorder, NeonMint.copy(alpha = 0.3f))), shape)
            .padding(20.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = "購物車", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(text = "$itemCount 項商品", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onRetrieveOrder) {
                    Text("取單", color = NeonCyan)
                }
                androidx.compose.material3.Surface(
                    color = Surface,
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        if (orderType == "內用") onSetOrderType("外帶", null)
                        else showTableDialog = true
                    }
                ) {
                    Text(
                        text = if (orderType == "內用") "內用 ${tableNumber ?: ""}" else "外帶",
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            onRemove(item.id)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFE57373).copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                                .padding(end = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF5350))
                        }
                    }
                ) {
                    CartItemRow(
                        item = item,
                        onIncrease = { onIncrease(item.id, item.quantity + 1) },
                        onDecrease = { onDecrease(item.id, item.quantity - 1) }
                    )
                }
            }
            if (items.isEmpty()) {
                item {
                    Text(
                        text = "尚未加入商品",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        SummaryRow(label = "小計", value = subtotal)
        SummaryRow(label = "稅金", value = taxAmount)
        if (discountAmount > 0) {
            SummaryRow(label = "折扣", value = -discountAmount, emphasis = true)
        }
        SummaryRow(label = "總計", value = totalAmount, emphasis = true)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { showDiscountDialog = true }, modifier = Modifier.weight(1f)) {
                Text("新增折扣", color = NeonCyan)
            }
            TextButton(onClick = onHoldOrder, modifier = Modifier.weight(1f)) {
                Text("掛單", color = TextPrimary)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            NeonButton(
                text = "清空",
                modifier = Modifier.weight(1f),
                glowColor = NeonMint,
                onClick = onClear
            )
            NeonButton(
                text = "前往結帳",
                modifier = Modifier.weight(2f),
                glowColor = NeonCyan,
                onClick = onCheckout
            )
        }
    }

    if (showTableDialog) {
        // Simple dialog to enter table number
        var tableInput by remember { mutableStateOf("") }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showTableDialog = false }) {
            androidx.compose.material3.Surface(shape = RoundedCornerShape(16.dp), color = Background) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("輸入桌號", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    PosTextField(value = tableInput, onValueChange = { tableInput = it }, label = "桌號")
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showTableDialog = false }) { Text("取消", color = TextSecondary) }
                        NeonButton(text = "確認", glowColor = NeonCyan) {
                            onSetOrderType("內用", tableInput)
                            showTableDialog = false
                        }
                    }
                }
            }
        }
    }

    if (showDiscountDialog) {
        var discountInput by remember { mutableStateOf("") }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDiscountDialog = false }) {
            androidx.compose.material3.Surface(shape = RoundedCornerShape(16.dp), color = Background) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("整單折抵", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    PosTextField(value = discountInput, onValueChange = { discountInput = it }, label = "折抵金額", keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showDiscountDialog = false }) { Text("取消", color = TextSecondary) }
                        NeonButton(text = "確認", glowColor = NeonCyan) {
                            val amount = discountInput.toDoubleOrNull() ?: 0.0
                            onAddDiscount(amount)
                            showDiscountDialog = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(16.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = item.productName, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                if (item.modifiers.isNotEmpty()) {
                    Text(text = item.modifiers.joinToString(", "), color = NeonCyan, style = MaterialTheme.typography.bodySmall)
                }
                if (!item.note.isNullOrBlank()) {
                    Text(text = "備註: ${item.note}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                if (item.customDiscount > 0) {
                    Text(text = "單品折扣: -${item.customDiscount}", color = NeonMint, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = String.format(Locale.getDefault(), "NT$ %.2f", item.total), color = NeonCyan)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Remove, contentDescription = "decrease", tint = TextPrimary)
                }
                Text(text = item.quantity.toString(), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Add, contentDescription = "increase", tint = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Double, emphasis: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            color = if (emphasis) TextPrimary else TextSecondary,
            style = if (emphasis) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = String.format(Locale.getDefault(), "NT$ %.2f", value),
            color = if (emphasis) NeonMint else TextPrimary,
            style = if (emphasis) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
    }
}
