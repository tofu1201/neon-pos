package com.yourcompany.pos.presentation.pos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourcompany.pos.domain.model.PaymentMethod
import com.yourcompany.pos.presentation.pos.components.CartPanel
import com.yourcompany.pos.presentation.pos.components.NeonButton
import com.yourcompany.pos.presentation.pos.components.PosAlertDialog
import com.yourcompany.pos.presentation.pos.components.MemberInfoBanner
import com.yourcompany.pos.presentation.pos.components.ProductCard
import com.yourcompany.pos.presentation.pos.components.StatusBanner
import com.yourcompany.pos.presentation.theme.Background
import com.yourcompany.pos.presentation.theme.NeonCyan
import com.yourcompany.pos.presentation.theme.NeonMint
import com.yourcompany.pos.presentation.theme.NeonPink
import com.yourcompany.pos.presentation.theme.SurfaceElevated
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary
import com.yourcompany.pos.presentation.pos.components.BottomCartBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.launch
import java.util.Locale
import com.yourcompany.pos.presentation.pos.components.AddProductDialog
import com.yourcompany.pos.presentation.pos.components.CheckoutDialog
import com.yourcompany.pos.presentation.pos.components.MemberManagerDialog
import com.yourcompany.pos.presentation.pos.components.MemberTopUpDialog
import com.yourcompany.pos.presentation.pos.components.ModifierDialog
import com.yourcompany.pos.presentation.pos.components.CategorySidebar
import com.yourcompany.pos.presentation.pos.components.RecentOrdersDialog
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PosScreen(
    state: PosUiState,
    onEvent: (PosEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var productForModifier by remember { mutableStateOf<com.yourcompany.pos.domain.model.Product?>(null) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showRecentOrders by remember { mutableStateOf(false) }

    if (showRecentOrders) {
        RecentOrdersDialog(
            orders = state.recentOrders,
            onDismiss = { showRecentOrders = false },
            onCancelOrder = { onEvent(PosEvent.CancelOrderAttempt(it)) }
        )
    }

    if (showCartSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = sheetState,
            containerColor = Background,
            contentColor = TextPrimary
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                CartPanel(
                    items = state.cartItems,
                    subtotal = state.checkoutSummary.subtotal,
                    taxAmount = state.checkoutSummary.taxAmount,
                    discountAmount = state.checkoutSummary.discountAmount,
                    totalAmount = state.checkoutSummary.totalAmount,
                    cashReceivedInput = state.cashReceivedInput,
                    changeAmount = state.checkoutSummary.changeAmount,
                    itemCount = state.checkoutSummary.itemCount,
                    compact = false,
                    onIncrease = { id, quantity -> onEvent(PosEvent.UpdateQuantity(id, quantity)) },
                    onDecrease = { id, quantity -> onEvent(PosEvent.UpdateQuantity(id, quantity)) },
                    onRemove = { onEvent(PosEvent.RemoveItem(it)) },
                    onCheckout = { 
                        showCartSheet = false
                        onEvent(PosEvent.NavigateToCheckout)
                    },
                    onClear = { onEvent(PosEvent.ClearCart) },
                    orderType = state.orderType,
                    tableNumber = state.tableNumber,
                    onSetOrderType = { type, table -> onEvent(PosEvent.SetOrderType(type, table)) },
                    onHoldOrder = { 
                        showCartSheet = false
                        onEvent(PosEvent.HoldOrder) 
                    },
                    onRetrieveOrder = { onEvent(PosEvent.RetrieveOrder) },
                    onAddDiscount = { onEvent(PosEvent.SetGlobalDiscount(it)) }
                )
            }
        }
    }

    productForModifier?.let { product ->
        ModifierDialog(
            product = product,
            onDismissRequest = { productForModifier = null },
            onConfirm = { modifiers, note ->
                onEvent(PosEvent.AddToCart(product.id, modifiers, note))
                productForModifier = null
            }
        )
    }


    if (state.showMemberManagerDialog) {
        MemberManagerDialog(
            state = state,
            onEvent = onEvent,
            onDismissRequest = { onEvent(PosEvent.ShowMemberManagerDialog(false)) }
        )
    }

    if (state.showMemberTopUpDialog && state.memberId != null) {
        MemberTopUpDialog(
            memberName = state.memberName ?: "未知會員",
            currentBalance = state.memberBalance,
            onDismissRequest = { onEvent(PosEvent.ShowMemberTopUpDialog(false)) },
            onTopUp = { onEvent(PosEvent.TopUpMember(it)) }
        )
    }

    LaunchedEffect(state.snackbarMessage, state.errorMessage) {
        state.snackbarMessage?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
            onEvent(PosEvent.DismissMessage)
        }
        state.errorMessage?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
            onEvent(PosEvent.DismissMessage)
        }
    }

    if (state.alertTitle != null && state.alertMessage != null) {
        PosAlertDialog(
            title = state.alertTitle,
            message = state.alertMessage,
            onDismissRequest = { onEvent(PosEvent.DismissAlert) }
        )
    }

    if (state.completedOrderNo != null && state.posIpAddress != null) {
        ReceiptQrDialog(
            orderNo = state.completedOrderNo,
            ipAddress = state.posIpAddress,
            onDismissRequest = { onEvent(PosEvent.ClearCompletedOrderNo) }
        )
    }

    if (state.showPinLoginDialog) {
        PinLoginDialog(
            errorMessage = state.errorMessage,
            onLogin = { onEvent(PosEvent.LoginEmployee(it)) }
        )
    }

    if (state.showAdminPinDialogForCancel) {
        AdminPinDialog(
            errorMessage = state.errorMessage,
            onVerify = { onEvent(PosEvent.VerifyAdminPinForCancel(it)) },
            onDismiss = { onEvent(PosEvent.DismissAdminPinDialog) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "NEON POS", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Offline-ready hand-held checkout",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(PosEvent.ShowMemberManagerDialog(true)) }) {
                        Icon(Icons.Outlined.Person, contentDescription = "會員管理", tint = NeonCyan)
                    }
                    if (state.memberId != null) {
                        TextButton(onClick = { onEvent(PosEvent.ShowMemberTopUpDialog(true)) }) {
                            Text("儲值", color = NeonMint)
                        }
                    }
                    IconButton(onClick = { onEvent(PosEvent.PrintZReport) }) {
                        Icon(Icons.Outlined.Print, contentDescription = "日結報表", tint = NeonPink)
                    }
                    IconButton(onClick = { showRecentOrders = true }) {
                        Icon(Icons.Outlined.List, contentDescription = "歷史訂單", tint = NeonCyan)
                    }
                    IconButton(onClick = { onEvent(PosEvent.NavigateToSettings) }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "設定", tint = NeonCyan)
                    }
                    if (state.loggedInEmployee != null) {
                        TextButton(onClick = { onEvent(PosEvent.LogoutEmployee) }) {
                            Text("登出", color = NeonPink)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val compact = maxWidth < 900.dp
            if (compact) {
                CompactLayout(
                    state = state,
                    onEvent = onEvent,
                    snackbarHostState = snackbarHostState,
                    onProductClick = { product -> productForModifier = product },
                    onShowCart = { showCartSheet = true }
                )
            } else {
                ExpandedLayout(
                    state = state,
                    onEvent = onEvent,
                    snackbarHostState = snackbarHostState,
                    onProductClick = { product -> productForModifier = product },
                    onShowCart = { showCartSheet = true }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpandedLayout(
    state: PosUiState,
    onEvent: (PosEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onProductClick: (com.yourcompany.pos.domain.model.Product) -> Unit,
    onShowCart: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category Sidebar
        CategorySidebar(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            onCategorySelected = { onEvent(PosEvent.SelectCategory(it)) },
            modifier = Modifier.width(100.dp).fillMaxHeight()
        )

        Column(
            modifier = Modifier.weight(1.6f).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { onEvent(PosEvent.SearchChanged(it)) }
            )
            MemberInfoSection(state = state)
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 220.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            onProductClick(product)
                        }
                    }
                }
            }
            
            BottomCartBar(
                itemCount = state.checkoutSummary.itemCount,
                totalAmount = state.checkoutSummary.totalAmount,
                onClick = onShowCart
            )
        }
    }
}

@Composable
private fun CompactLayout(
    state: PosUiState,
    onEvent: (PosEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onProductClick: (com.yourcompany.pos.domain.model.Product) -> Unit,
    onShowCart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { onEvent(PosEvent.SearchChanged(it)) }
        )
        MemberInfoSection(state = state)
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.products, key = { it.id }) { product ->
                    ProductCard(product = product) {
                        onProductClick(product)
                    }
                }
            }
        }

        BottomCartBar(
            itemCount = state.checkoutSummary.itemCount,
            totalAmount = state.checkoutSummary.totalAmount,
            onClick = onShowCart
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("搜尋商品、SKU") },
        singleLine = true,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceElevated,
            unfocusedContainerColor = SurfaceElevated,
            focusedIndicatorColor = NeonCyan,
            unfocusedIndicatorColor = TextSecondary.copy(alpha = 0.35f),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedPlaceholderColor = TextSecondary,
            unfocusedPlaceholderColor = TextSecondary
        )
    )
}



@Composable
private fun MemberInfoSection(state: PosUiState) {
    if (state.memberId.isNullOrBlank() || state.memberName.isNullOrBlank()) return
    MemberInfoBanner(
        memberId = state.memberId ?: return,
        memberName = state.memberName ?: return,
        points = state.memberPoints ?: 0,
        discountRate = state.memberDiscountRate,
        color = NeonMint
    )
}

@Composable
fun ReceiptQrDialog(
    orderNo: String,
    ipAddress: String,
    onDismissRequest: () -> Unit
) {
    val url = "http://$ipAddress:8080/receipt/$orderNo"
    val bitmap = remember(url) { com.yourcompany.pos.presentation.pos.components.generateQrCodeBitmap(url) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "電子收據 QR Code",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan
                )
                Text(
                    text = "顧客掃描以下條碼即可獲取電子收據",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(8.dp)
                            .background(androidx.compose.ui.graphics.Color.White)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("QR Code 產生失敗", color = NeonPink)
                }
                
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("完成", color = Background)
                }
            }
        }
    }
}

@Composable
fun PinLoginDialog(
    errorMessage: String?,
    onLogin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = { /* Cannot dismiss without login */ }) {
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("員工登入", style = MaterialTheme.typography.titleLarge, color = NeonCyan)
                
                TextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("請輸入 PIN 碼") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (errorMessage != null) {
                    Text(errorMessage, color = NeonPink, style = MaterialTheme.typography.bodySmall)
                }
                
                Button(
                    onClick = { onLogin(pin) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("登入", color = Background)
                }
            }
        }
    }
}

@Composable
fun AdminPinDialog(
    errorMessage: String?,
    onVerify: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("需要店長權限", style = MaterialTheme.typography.titleLarge, color = NeonPink)
                Text("取消訂單需要輸入店長 PIN 碼", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                
                TextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("請輸入店長 PIN 碼") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (errorMessage != null) {
                    Text(errorMessage, color = NeonPink, style = MaterialTheme.typography.bodySmall)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Background)
                    ) {
                        Text("取消", color = TextPrimary)
                    }
                    Button(
                        onClick = { onVerify(pin) },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Text("授權", color = Background)
                    }
                }
            }
        }
    }
}
