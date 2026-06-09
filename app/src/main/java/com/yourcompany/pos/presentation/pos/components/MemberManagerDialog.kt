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
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.NeonMint
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary
import com.yourcompany.pos.presentation.pos.PosUiState
import com.yourcompany.pos.presentation.pos.PosEvent

@Composable
fun MemberManagerDialog(
    state: PosUiState,
    onEvent: (PosEvent) -> Unit,
    onDismissRequest: () -> Unit
) {
    var phone by remember { mutableStateOf(state.memberId ?: "") }
    var name by remember { mutableStateOf(state.memberName ?: "") }

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
                Text(text = "會員管理", style = MaterialTheme.typography.titleLarge, color = TextPrimary)

                PosTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "手機號碼",
                    keyboardType = KeyboardType.Phone
                )

                PosTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "會員姓名 (註冊時必填)"
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeonButton(
                        text = "搜尋/登入",
                        glowColor = NeonCyan,
                        modifier = Modifier.weight(1f),
                        enabled = phone.isNotBlank()
                    ) {
                        onEvent(PosEvent.SearchMember(phone))
                    }

                    NeonButton(
                        text = "註冊新會員",
                        glowColor = NeonMint,
                        modifier = Modifier.weight(1f),
                        enabled = phone.isNotBlank() && name.isNotBlank()
                    ) {
                        onEvent(PosEvent.RegisterMember(phone, name))
                    }
                }

                if (state.memberId != null) {
                    NeonButton(
                        text = "綁定實體儲值卡 (NFC)",
                        glowColor = NeonCyan,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        onEvent(PosEvent.BindNfcCard(state.memberId))
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
