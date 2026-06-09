package com.yourcompany.pos.presentation.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yourcompany.pos.presentation.theme.Background
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.SurfaceElevated
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary

@Composable
fun AddProductDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (sku: String, name: String, price: Double, taxRate: Double) -> Unit
) {
    var sku by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var taxRate by remember { mutableStateOf("0.05") }
    
    var errorText by remember { mutableStateOf<String?>(null) }

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
                Text(
                    text = "新增商品",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NeonCyan
                )
                
                errorText?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                PosTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = "SKU"
                )
                PosTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "商品名稱"
                )
                PosTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = "價格",
                    keyboardType = KeyboardType.Number
                )
                PosTextField(
                    value = taxRate,
                    onValueChange = { taxRate = it },
                    label = "稅率 (如 0.05)",
                    keyboardType = KeyboardType.Decimal
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("取消", color = TextSecondary)
                    }
                    NeonButton(
                        text = "確認新增",
                        onClick = {
                            val parsedPrice = price.toDoubleOrNull()
                            val parsedTax = taxRate.toDoubleOrNull()
                            if (sku.isBlank() || name.isBlank() || parsedPrice == null || parsedTax == null) {
                                errorText = "請填寫正確格式的資料"
                                return@NeonButton
                            }
                            onConfirm(sku, name, parsedPrice, parsedTax)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun PosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = SurfaceElevated,
            focusedLabelColor = NeonCyan,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = NeonCyan
        ),
        singleLine = true
    )
}
