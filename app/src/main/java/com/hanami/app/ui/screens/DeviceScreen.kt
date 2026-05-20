package com.hanami.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanami.app.data.ApiResult
import com.hanami.app.data.HanamiApi
import com.hanami.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DeviceScreen(token: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UgphoneStatusSection(token)
        BuyDeviceCloudSection(token)
    }
}

// ── Section 1: UGPhone Status ─────────────────────────────────────────────────

@Composable
private fun UgphoneStatusSection(token: String) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var statusMap by remember { mutableStateOf<Map<String, Boolean>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    SectionCard(title = "UGPhone Status", icon = Icons.Default.PhoneAndroid) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            statusMap?.let { map ->
                map.entries.forEach { (region, available) ->
                    RegionRow(region = region, available = available)
                }
            } ?: run {
                if (!loading) {
                    Text(
                        "Nhấn để kiểm tra trạng thái UGPhone theo khu vực.",
                        color = TextMuted, fontSize = 12.sp
                    )
                }
            }

            error?.let {
                Text("Lỗi: $it", color = Red, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    loading = true
                    error = null
                    scope.launch {
                        when (val res = HanamiApi.checkUgphoneStatus(token)) {
                            is ApiResult.Success -> statusMap = res.data
                            is ApiResult.Error -> error = res.message
                        }
                        loading = false
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = PurpleActive),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Kiểm tra")
                }
            }
        }
    }
}

@Composable
private fun RegionRow(region: String, available: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NavPillBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(region, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (available) Green else Red)
            )
            Text(
                text = if (available) "Available" else "Unavailable",
                color = if (available) Green else Red,
                fontSize = 12.sp
            )
        }
    }
}

// ── Section 2: Buy Device Cloud ───────────────────────────────────────────────

private val CLOUD_OPTIONS = mapOf(
    "UG" to "Ugphone",
    "VS" to "Vsphone",
    "VM" to "VMOS"
)

private val UG_SERVERS = listOf("SG", "HK", "JP", "DE", "US")

@Composable
private fun BuyDeviceCloudSection(token: String) {
    val scope = rememberCoroutineScope()
    var selectedCloud by remember { mutableStateOf("UG") }
    var selectedServer by remember { mutableStateOf("SG") }
    var inputData by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Pair<Boolean, String>?>(null) } // (success, msg)

    SectionCard(title = "Buy Device Cloud", icon = Icons.Default.Cloud) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Cloud type selector
            Text("Cloud Type", color = TextMuted, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CLOUD_OPTIONS.forEach { (id, label) ->
                    val active = selectedCloud == id
                    FilterChip(
                        selected = active,
                        onClick = { selectedCloud = id },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurpleActive,
                            selectedLabelColor = Color.White,
                            containerColor = NavPillBg,
                            labelColor = TextMuted
                        ),
                        border = null
                    )
                }
            }

            // Server selector (UG only)
            if (selectedCloud == "UG") {
                Text("Server", color = TextMuted, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UG_SERVERS.forEach { srv ->
                        val active = selectedServer == srv
                        FilterChip(
                            selected = active,
                            onClick = { selectedServer = srv },
                            label = { Text(srv, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurpleActive,
                                selectedLabelColor = Color.White,
                                containerColor = NavPillBg,
                                labelColor = TextMuted
                            ),
                            border = null
                        )
                    }
                }
            }

            // Input data
            val inputHint = when (selectedCloud) {
                "UG" -> "LocalStorage string"
                else -> "username|password"
            }
            OutlinedTextField(
                value = inputData,
                onValueChange = { inputData = it },
                label = { Text(inputHint, color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpleActive,
                    unfocusedBorderColor = NavPillBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PurpleActive
                ),
                minLines = 2
            )

            // Result
            result?.let { (success, msg) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (success) Green.copy(alpha = 0.15f) else Red.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = msg,
                        color = if (success) Green else Red,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = {
                    loading = true
                    result = null
                    scope.launch {
                        val server = if (selectedCloud == "UG") selectedServer else null
                        when (val res = HanamiApi.buyDeviceCloud(
                            token, selectedCloud, server, inputData.ifBlank { null }
                        )) {
                            is ApiResult.Success -> result = Pair(true, res.data)
                            is ApiResult.Error -> result = Pair(false, res.message)
                        }
                        loading = false
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = PurpleActive),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Mua ngay")
                }
            }
        }
    }
}

// ── Shared card ───────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(18.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        HorizontalDivider(color = NavPillBg)
        content()
    }
}
