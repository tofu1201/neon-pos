package com.yourcompany.pos.presentation.pos.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yourcompany.pos.domain.model.Product
import com.yourcompany.pos.presentation.theme.Background
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.NeonMint
import com.yourcompany.pos.presentation.theme.SurfaceElevated
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary

@Composable
fun ModifierDialog(
    product: Product,
    onDismissRequest: () -> Unit,
    onConfirm: (modifiers: List<String>, note: String?) -> Unit
) {
    var selectedIce by remember { mutableStateOf("正常冰") }
    var selectedSugar by remember { mutableStateOf("正常糖") }
    var note by remember { mutableStateOf("") }

    val iceOptions = listOf("正常冰", "少冰", "微冰", "去冰", "熱")
    val sugarOptions = listOf("正常糖", "少糖", "半糖", "微糖", "無糖")

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "客製化 - ${product.name}", style = MaterialTheme.typography.titleLarge, color = TextPrimary)

                if (product.category == "飲品") {
                    Text("冰塊選擇", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    OptionRow(options = iceOptions, selected = selectedIce) { selectedIce = it }

                    Text("甜度選擇", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    OptionRow(options = sugarOptions, selected = selectedSugar) { selectedSugar = it }
                }

                Text("單品備註", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                PosTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "例如：不要蔥、加辣"
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onDismissRequest, modifier = Modifier.weight(1f)) {
                        Text("取消", color = TextSecondary)
                    }
                    NeonButton(
                        text = "加入購物車",
                        glowColor = NeonMint,
                        modifier = Modifier.weight(1f)
                    ) {
                        val modifiers = mutableListOf<String>()
                        if (product.category == "飲品") {
                            modifiers.add(selectedIce)
                            modifiers.add(selectedSugar)
                        }
                        onConfirm(modifiers, note.ifBlank { null })
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Surface(
                color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else SurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                onClick = { onSelect(option) },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option,
                    color = if (isSelected) NeonCyan else TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
