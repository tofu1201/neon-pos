package com.yourcompany.pos.presentation.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yourcompany.pos.presentation.theme.Background
import com.yourcompany.pos.presentation.theme.NeonMint
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary
import java.util.Locale

@Composable
fun MemberTopUpDialog(
    memberName: String,
    currentBalance: Double,
    onDismissRequest: () -> Unit,
    onTopUp: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "會員儲值 - $memberName", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text(
                    text = "目前餘額：NT$ ${String.format(Locale.getDefault(), "%.2f", currentBalance)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )

                PosTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "儲值金額",
                    keyboardType = KeyboardType.Number
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeonButton(
                        text = "確認儲值",
                        glowColor = NeonMint,
                        modifier = Modifier.weight(1f),
                        enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
                    ) {
                        amount.toDoubleOrNull()?.let { onTopUp(it) }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest) {
                        Text("取消", color = TextSecondary)
                    }
                }
            }
        }
    }
}
