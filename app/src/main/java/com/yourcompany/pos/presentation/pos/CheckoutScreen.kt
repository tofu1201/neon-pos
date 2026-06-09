package com.yourcompany.pos.presentation.pos

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourcompany.pos.domain.model.PaymentMethod
import com.yourcompany.pos.presentation.pos.components.NeonButton
import com.yourcompany.pos.presentation.pos.components.PosAlertDialog
import com.yourcompany.pos.presentation.theme.Background
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.NeonMint
import com.yourcompany.pos.presentation.theme.SurfaceElevated
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    state: PosUiState,
    onEvent: (PosEvent) -> Unit,
    onBack: () -> Unit,
    onCheckoutComplete: () -> Unit
) {
    var localPaymentMethod by remember { mutableStateOf(state.selectedPaymentMethod) }

    DisposableEffect(Unit) {
        onEvent(PosEvent.SetCheckoutScreenActive(true))
        onDispose {
            onEvent(PosEvent.SetCheckoutScreenActive(false))
        }
    }

    LaunchedEffect(localPaymentMethod) {
        onEvent(PosEvent.SelectPaymentMethod(localPaymentMethod))
    }

    LaunchedEffect(state.isCheckingOut, state.errorMessage, state.snackbarMessage) {
        if (!state.isCheckingOut && (state.snackbarMessage?.contains("訂單已建立") == true || state.snackbarMessage?.contains("結帳成功") == true)) {
            onCheckoutComplete()
        }
    }

    if (state.alertTitle != null && state.alertMessage != null) {
        PosAlertDialog(
            title = state.alertTitle,
            message = state.alertMessage,
            onDismissRequest = { onEvent(PosEvent.DismissAlert) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("結帳", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left: Payment Methods
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("選擇付款方式", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                PaymentMethodButton("現金結帳", localPaymentMethod == PaymentMethod.CASH) {
                    localPaymentMethod = PaymentMethod.CASH
                }
                PaymentMethodButton("信用卡", localPaymentMethod == PaymentMethod.CARD) {
                    localPaymentMethod = PaymentMethod.CARD
                }
                PaymentMethodButton("自家感應卡 (NFC)", localPaymentMethod == PaymentMethod.NFC) {
                    localPaymentMethod = PaymentMethod.NFC
                }
                PaymentMethodButton("會員餘額", localPaymentMethod == PaymentMethod.MEMBER_BALANCE) {
                    localPaymentMethod = PaymentMethod.MEMBER_BALANCE
                }

                Spacer(modifier = Modifier.weight(1f))

                if (localPaymentMethod == PaymentMethod.NFC) {
                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Nfc,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("請感應自家的實體儲值卡 (NFC)", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("等待感應中...", color = TextSecondary)
                        }
                    }
                }
            }

            // Right: Order Summary & Confirm
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("訂單總計", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    SummaryRow("小計", state.checkoutSummary.subtotal)
                    SummaryRow("稅金", state.checkoutSummary.taxAmount)
                    if (state.checkoutSummary.discountAmount > 0) {
                        SummaryRow("折扣", -state.checkoutSummary.discountAmount, color = NeonMint)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("總金額", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Text(
                            "NT$ ${String.format(Locale.getDefault(), "%.2f", state.checkoutSummary.totalAmount)}",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = NeonMint
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (state.isCheckingOut) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NeonMint)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Checkbox(
                                checked = state.printReceipt,
                                onCheckedChange = { onEvent(PosEvent.TogglePrintReceipt(it)) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NeonCyan,
                                    uncheckedColor = TextSecondary
                                )
                            )
                            Text("列印明細表", color = TextPrimary)
                        }

                        NeonButton(
                            text = if (localPaymentMethod == PaymentMethod.NFC) "等待感應自動扣款" else "確認結帳",
                            glowColor = NeonMint,
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            enabled = localPaymentMethod != PaymentMethod.NFC // NFC requires card tap
                        ) {
                            onEvent(PosEvent.Checkout(localPaymentMethod, state.printReceipt))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodButton(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) NeonCyan.copy(alpha = 0.2f) else SurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            color = if (selected) NeonCyan else TextPrimary,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        )
    }
}

@Composable
private fun SummaryRow(title: String, amount: Double, color: androidx.compose.ui.graphics.Color = TextPrimary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = title, color = TextSecondary)
        Text(text = "NT$ ${String.format(Locale.getDefault(), "%.2f", amount)}", color = color)
    }
}
