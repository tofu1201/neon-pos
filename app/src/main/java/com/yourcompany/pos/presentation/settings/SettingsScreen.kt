package com.yourcompany.pos.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourcompany.pos.presentation.pos.PosEvent
import com.yourcompany.pos.presentation.pos.PosUiState
import com.yourcompany.pos.presentation.pos.components.AddProductDialog
import com.yourcompany.pos.presentation.pos.components.NeonButton
import com.yourcompany.pos.presentation.theme.Background
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.NeonMint
import com.yourcompany.pos.presentation.theme.SurfaceBorder
import com.yourcompany.pos.presentation.theme.SurfaceElevated
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: PosUiState,
    onEvent: (PosEvent) -> Unit,
    onBack: () -> Unit
) {
    var showAddProductDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("系統設定", color = TextPrimary) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Printer Settings Section
            SettingsSection("硬體與印表機設定") {
                Text("印表機狀態：${state.printerStatus}", color = if (state.printerConnected) NeonMint else TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeonButton(
                        text = "連接印表機",
                        glowColor = NeonCyan,
                        enabled = !state.printerConnected && !state.printerConnecting
                    ) {
                        onEvent(PosEvent.PrinterStatusChanged("CONNECT_PRINTER")) // Handled by ViewModel or auto-connected
                    }
                    NeonButton(
                        text = "測試列印",
                        glowColor = NeonMint,
                        enabled = state.printerConnected
                    ) {
                        onEvent(PosEvent.PrintDemoReceipt)
                    }
                    NeonButton(
                        text = "列印交班報表 (Z-Report)",
                        glowColor = NeonMint,
                        enabled = state.printerConnected
                    ) {
                        onEvent(PosEvent.PrintZReport)
                    }
                }
            }

            // Product Management Section
            SettingsSection("商品管理") {
                Text("快速建立新的商品項目。", color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                NeonButton(
                    text = "新增商品",
                    glowColor = NeonCyan
                ) {
                    showAddProductDialog = true
                }
            }

            // Web Server Section
            SettingsSection("內網 Web 伺服器") {
                val ip = com.yourcompany.pos.data.remote.PosWebServer.getLocalIpAddress()
                if (ip != null) {
                    Text("本機網頁管理後台已啟動", color = NeonMint)
                    Text("請在同網域的瀏覽器輸入：", color = TextSecondary)
                    Text("http://$ip:8080", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                } else {
                    Text("無法取得 IP 位址，請確認設備已連線至 Wi-Fi", color = com.yourcompany.pos.presentation.theme.NeonPink)
                }
            }

            // NFC Diagnostics Section
            SettingsSection("NFC 感應狀態") {
                Text("NFC 支援狀態：${if (state.nfcAvailable) "支援" else "不支援"}", color = TextPrimary)
                Text("NFC 讀取狀態：${state.nfcStatus}", color = TextSecondary)
                if (state.nfcDetails.isNotBlank()) {
                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text(
                            text = state.nfcDetails,
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    if (showAddProductDialog) {
        AddProductDialog(
            onDismissRequest = { showAddProductDialog = false },
            onConfirm = { sku, name, price, tax ->
                onEvent(PosEvent.AddProduct(sku, name, price, tax))
                showAddProductDialog = false
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}
