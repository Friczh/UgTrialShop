package com.hanami.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hanami.app.data.TokenStore
import kotlinx.coroutines.launch
import com.hanami.app.ui.theme.*

@Composable
fun SettingsDialog(
    tokenStore: TokenStore,
    currentToken: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var tokenInput by remember { mutableStateOf(currentToken) }
    var showToken by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BgSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(20.dp))
                Text("Settings", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            HorizontalDivider(color = NavPillBg)

            // Token section
            Text("API Token", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)

            OutlinedTextField(
                value = tokenInput,
                onValueChange = {
                    tokenInput = it
                    saved = false
                },
                label = { Text("Nhập token của bạn", color = TextMuted) },
                singleLine = true,
                visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showToken = !showToken }) {
                        Icon(
                            imageVector = if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpleActive,
                    unfocusedBorderColor = NavPillBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PurpleActive
                )
            )

            // Info note
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp).padding(top = 1.dp)
                )
                Text(
                    "Token được lưu cục bộ trên thiết bị. Lấy token tại hanaminikata.com/settings.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            // Save confirmation
            if (saved) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp))
                    Text("Token đã được lưu!", color = Green, fontSize = 12.sp)
                }
            }

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                ) { Text("Đóng") }

                Button(
                    onClick = {
                        scope.launch {
                            tokenStore.saveToken(tokenInput.trim())
                            saved = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleActive)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Lưu")
                }
            }
        }
    }
}
