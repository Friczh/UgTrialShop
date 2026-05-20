package com.hanami.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanami.app.ui.theme.*

enum class Tab { HOME, SHOP, DEVICE, MISSION }

@Composable
fun HanamiTopBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    onSettingsClick: () -> Unit,
    onConsoleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Left: Nav Pill ────────────────────────────────────
        NavPill(selectedTab = selectedTab, onTabSelected = onTabSelected)

        // ── Right: Utility buttons ────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            UtilButton(icon = Icons.Default.Code, desc = "Console", onClick = onConsoleClick)
            UtilButton(icon = Icons.Default.Settings, desc = "Settings", onClick = onSettingsClick)
        }
    }
}

@Composable
private fun NavPill(selectedTab: Tab, onTabSelected: (Tab) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(NavPillBg)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        PillButton(
            icon = Icons.Default.Home,
            label = "Trang chủ",
            active = selectedTab == Tab.HOME,
            onClick = { onTabSelected(Tab.HOME) }
        )
        PillButton(
            icon = Icons.Default.Smartphone,
            label = "Status",
            active = selectedTab == Tab.DEVICE,
            onClick = { onTabSelected(Tab.DEVICE) }
        )
        PillButton(
            icon = Icons.Default.ShoppingCart,
            label = "Mua",
            active = selectedTab == Tab.SHOP,
            onClick = { onTabSelected(Tab.SHOP) }
        )
        PillButton(
            icon = Icons.Default.CreditCard,
            label = "Nhiệm vụ",
            active = selectedTab == Tab.MISSION,
            onClick = { onTabSelected(Tab.MISSION) }
        )
        // FAB more button
        Box(
            modifier = Modifier
                .padding(start = 2.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(PurpleActive)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "More",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PillButton(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (active) PurpleActive else Color.Transparent
    val contentColor = if (active) Color.White else TextMuted

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = if (active) 14.dp else 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        AnimatedVisibility(
            visible = active,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut()
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun UtilButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = TextDim,
            modifier = Modifier.size(22.dp)
        )
    }
}
