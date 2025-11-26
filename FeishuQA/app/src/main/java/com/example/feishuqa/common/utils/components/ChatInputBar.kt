package com.example.feishuqa.common.utils.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatInputBar(
    onSendText: (String) -> Unit,
    onAttachClick: () -> Unit,
    onVoiceClick: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isWebSearchEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(top = 8.dp, bottom = 24.dp) // 底部留出安全区
    ) {
        // 第一行：功能开关 (联网搜索等)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isWebSearchEnabled) Color(0xFFE8F2FF) else Color(0xFFF5F6F7),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { isWebSearchEnabled = !isWebSearchEnabled }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = if (isWebSearchEnabled) Color(0xFF3370FF) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "联网搜索",
                        fontSize = 12.sp,
                        color = if (isWebSearchEnabled) Color(0xFF3370FF) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            // ... 可以在这里添加更多 Chip，如 "深度思考"
            Icon(
                imageVector = Icons.Default.Add, // 这里只是演示占位，实际可为“...”更多
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        // 第二行：输入框主体
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // 左侧附件按钮
            IconButton(
                onClick = onAttachClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add, // 或者用文件夹图标
                    contentDescription = "Attach",
                    tint = Color(0xFF1F2329)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 中间输入框
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF5F6F7)) // 浅灰背景
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "问个问题，或用知识写点内容",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF3370FF)),
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 右侧按钮 (发送 或 语音)
            if (text.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3370FF))
                        .clickable {
                            onSendText(text)
                            text = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice",
                        tint = Color(0xFF1F2329)
                    )
                }
            }
        }
    }
}