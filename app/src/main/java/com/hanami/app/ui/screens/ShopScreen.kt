package com.hanami.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.hanami.app.data.HanamiApi
import com.hanami.app.data.PRODUCT_LIST
import com.hanami.app.data.Product
import com.hanami.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ShopScreen(token: String) {
    val scope = rememberCoroutineScope()
    val tags = remember { listOf("Tất cả") + PRODUCT_LIST.map { it.tag }.distinct() }
    var selectedTag by remember { mutableStateOf("Tất cả") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var orderResult by remember { mutableStateOf<String?>(null) }

    val filtered = if (selectedTag == "Tất cả") PRODUCT_LIST
    else PRODUCT_LIST.filter { it.tag == selectedTag }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tag filter row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tags) { tag ->
                val active = tag == selectedTag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (active) PurpleActive else NavPillBg)
                        .clickable { selectedTag = tag }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = tag,
                        color = if (active) Color.White else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // Product list
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { product ->
                ProductCard(product = product, onClick = { selectedProduct = product })
            }
        }
    }

    // Order dialog
    selectedProduct?.let { product ->
        OrderDialog(
            token = token,
            product = product,
            onDismiss = { selectedProduct = null },
            onResult = { msg ->
                selectedProduct = null
                orderResult = msg
            }
        )
    }

    // Result snackbar
    orderResult?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(4000)
            orderResult = null
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = NavPillBg
            ) { Text(msg, color = TextPrimary) }
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgSurface)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = product.iconUrl,
            contentDescription = product.name,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = product.description,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 2
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${product.hcoin}",
                color = PurpleLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "HCOIN", color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun OrderDialog(
    token: String,
    product: Product,
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var quantity by remember { mutableStateOf("1") }
    var inputData by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var orderId by remember { mutableStateOf<String?>(null) }
    var orderData by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BgSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AsyncImage(
                    model = product.iconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                )
                Column {
                    Text(product.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("${product.hcoin} HCOIN", color = PurpleLight, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = NavPillBg)

            // Quantity
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                label = { Text("Số lượng (1–10000)", color = TextMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = outlinedFieldColors()
            )

            // Optional input
            if (product.inputPlaceholder.isNotBlank()) {
                OutlinedTextField(
                    value = inputData,
                    onValueChange = { inputData = it },
                    label = { Text(product.inputPlaceholder, color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors()
                )
            }

            // Order ID display (after order placed)
            orderId?.let {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Order ID: $it", color = Green, fontSize = 12.sp)
                    orderData?.let { d -> Text(d, color = TextMuted, fontSize = 11.sp) }
                }
            }

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { if (!loading) onDismiss() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                ) { Text("Hủy") }

                if (orderId == null) {
                    Button(
                        onClick = {
                            val qty = quantity.toIntOrNull()?.coerceIn(1, 10000) ?: 1
                            loading = true
                            scope.launch {
                                when (val res = HanamiApi.createOrder(
                                    token, product.id, qty,
                                    inputData.ifBlank { null }, product.shopId
                                )) {
                                    is com.hanami.app.data.ApiResult.Success -> {
                                        orderId = res.data
                                        // Poll order status
                                        val check = HanamiApi.checkOrder(token, res.data)
                                        if (check is com.hanami.app.data.ApiResult.Success) {
                                            val d = check.data
                                            val status = d.optString("status", "unknown")
                                            val accounts = buildString {
                                                val arr = d.optJSONArray("accounts")
                                                if (arr != null) for (i in 0 until arr.length()) appendLine(arr.getString(i))
                                            }
                                            orderData = "Status: $status\n$accounts".trim()
                                        }
                                    }
                                    is com.hanami.app.data.ApiResult.Error -> onResult("Lỗi: ${res.message}")
                                }
                                loading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleActive),
                        enabled = !loading
                    ) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Đặt hàng")
                    }
                } else {
                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Green)
                    ) { Text("Xong") }
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PurpleActive,
    unfocusedBorderColor = NavPillBg,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = PurpleActive
)
