package com.yourcompany.pos.presentation.pos.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yourcompany.pos.domain.model.PaymentMethod
import com.yourcompany.pos.presentation.theme.Background
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.NeonMint
import com.yourcompany.pos.presentation.theme.NeonPink
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary
import java.util.Locale

@Composable
fun CheckoutDialog(
    totalAmount: Double,
    printReceipt: Boolean,
    onPrintReceiptChanged: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    onCheckout: (PaymentMethod, cashReceived: Double?) -> Unit
) {
    var expandedCash by remember { mutableStateOf(false) }
    var cashInput by remember { mutableStateOf(totalAmount.toString()) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "選擇付款方式", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text(
                        text = "結帳總額：NT$ ${String.format(Locale.getDefault(), "%.2f", totalAmount)}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NeonCyan
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    NeonButton(
                        text = "現金付款 (CASH)",
                        glowColor = NeonCyan,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        expandedCash = !expandedCash
                    }

                    AnimatedVisibility(visible = expandedCash) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            PosTextField(
                                value = cashInput,
                                onValueChange = { cashInput = it },
                                label = "實收金額",
                                keyboardType = KeyboardType.Number
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                FastCashButton("剛好", totalAmount, modifier = Modifier.weight(1f)) { cashInput = it.toString() }
                                FastCashButton("$100", 100.0, modifier = Modifier.weight(1f)) { cashInput = it.toString() }
                                FastCashButton("$500", 500.0, modifier = Modifier.weight(1f)) { cashInput = it.toString() }
                                FastCashButton("$1000", 1000.0, modifier = Modifier.weight(1f)) { cashInput = it.toString() }
                            }
                            NeonButton(
                                text = "確認收現並結帳",
                                glowColor = NeonCyan,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val received = cashInput.toDoubleOrNull() ?: totalAmount
                                onCheckout(PaymentMethod.CASH, received)
                            }
                        }
                    }

                    NeonButton(
                        text = "信用卡 (CARD)",
                        glowColor = NeonPink,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        onCheckout(PaymentMethod.CARD, null)
                    }
                    NeonButton(
                        text = "會員餘額扣款",
                        glowColor = NeonMint,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        onCheckout(PaymentMethod.MEMBER_BALANCE, null)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(
                            checked = printReceipt,
                            onCheckedChange = onPrintReceiptChanged,
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonCyan,
                                uncheckedColor = TextSecondary
                            )
                        )
                        Text("列印明細表", color = TextPrimary)
                    }
                    TextButton(onClick = onDismissRequest) {
                        Text("取消", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun FastCashButton(text: String, amount: Double, modifier: Modifier = Modifier, onClick: (Double) -> Unit) {
    TextButton(onClick = { onClick(amount) }, modifier = modifier) {
        Text(text, color = NeonCyan)
    }
}
