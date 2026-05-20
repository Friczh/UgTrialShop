package com.hanami.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hanami.app.ui.theme.*

data class LogEntry(
    val level: LogLevel,
    val message: String,
    val timestamp: String
)

enum class LogLevel { INFO, SUCCESS, ERROR, WARN }

object AppLogger {
    private val _logs = mutableStateListOf<LogEntry>()
    val logs: List<LogEntry> get() = _logs

    fun log(level: LogLevel, message: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        _logs.add(LogEntry(level, message, time))
        if (_logs.size > 200) _logs.removeAt(0)
    }

    fun info(msg: String) = log(LogLevel.INFO, msg)
    fun success(msg: String) = log(LogLevel.SUCCESS, msg)
    fun error(msg: String) = log(LogLevel.ERROR, msg)
    fun warn(msg: String) = log(LogLevel.WARN, msg)
    fun clear() = _logs.clear()
}

@Composable
fun ConsoleDialog(onDismiss: () -> Unit) {
    val logs = AppLogger.logs
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.size - 1)
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(16.dp))
                .background(BgSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(18.dp))
                    Text("Console", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("(${logs.size})", color = TextMuted, fontSize = 12.sp)
                }
                Row {
                    IconButton(onClick = { AppLogger.clear() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            HorizontalDivider(color = NavPillBg)

            // Log list
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No logs yet.", color = TextMuted, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavPillBg)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { entry ->
                        LogRow(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogRow(entry: LogEntry) {
    val (prefix, color) = when (entry.level) {
        LogLevel.INFO    -> Pair("[INFO]   ", TextMuted)
        LogLevel.SUCCESS -> Pair("[OK]     ", Green)
        LogLevel.ERROR   -> Pair("[ERR]    ", Red)
        LogLevel.WARN    -> Pair("[WARN]   ", PurpleLight)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = entry.timestamp,
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = prefix,
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = entry.message,
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}
