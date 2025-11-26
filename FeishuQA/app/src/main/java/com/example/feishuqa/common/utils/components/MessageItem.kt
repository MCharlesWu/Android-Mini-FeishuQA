package com.example.feishuqa.common.utils.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feishuqa.data.entity.Message
import com.example.feishuqa.data.entity.MessageStatus

@Composable
fun MessageItem(message: Message) {
    if (message.isUser()) {
        UserMessageItem(message)
    } else {
        AiMessageItem(message)
    }
}

@Composable
fun UserMessageItem(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Surface(
                color = Color(0xFFE8F2FF), // 飞书蓝浅色背景
                shape = RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp),
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }
            if (message.status == MessageStatus.SENDING) {
                Text(
                    text = "发送中...",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Avatar(isUser = true)
    }
}

@Composable
fun AiMessageItem(message: Message) {
    val clipboardManager = LocalClipboardManager.current
    var expandedThinking by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        // AI 头部信息（可选，如果需要显示模型名称）
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(isUser = false, size = 24.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "飞书智能助手",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 深度思考折叠卡片 (模拟)
        if (message.content.length > 20) { // 简单模拟，仅对长消息显示
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { expandedThinking = !expandedThinking },
                color = Color(0xFFF5F6F7),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "已完成深度思考",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (expandedThinking) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                }
            }
            if (expandedThinking) {
                Text(
                    text = "> 这里是模拟的思考过程...\n> 分析用户意图...\n> 检索知识库...",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                )
            }
        }

        // 消息正文 (无背景气泡)
        SelectionContainer {
            MarkdownTextMock(content = message.content)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 底部操作栏
        if (message.status != MessageStatus.TYPING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                ActionIcon(Icons.Outlined.ContentCopy, "复制") {
                    clipboardManager.setText(AnnotatedString(message.content))
                }
                Spacer(modifier = Modifier.width(16.dp))
                ActionIcon(Icons.Outlined.ThumbUp, "点赞") {}
                Spacer(modifier = Modifier.width(16.dp))
                ActionIcon(Icons.Outlined.ThumbDown, "点踩") {}
                Spacer(modifier = Modifier.width(16.dp))
                ActionIcon(Icons.Outlined.Refresh, "重新生成") {}
                Spacer(modifier = Modifier.weight(1f))
                ActionIcon(Icons.Outlined.Share, "分享") {}
            }
        }
    }
}

@Composable
fun ActionIcon(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Icon(
        imageVector = icon,
        contentDescription = desc,
        modifier = Modifier
            .size(18.dp)
            .clickable(onClick = onClick),
        tint = Color.Gray
    )
}

@Composable
fun Avatar(isUser: Boolean, size: Dp = 36.dp) {
    Surface(
        shape = CircleShape,
        color = if (isUser) Color(0xFF3370FF) else Color.Transparent, // 用户蓝底，AI透明(或彩色Logo)
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isUser) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.6f)
                )
            } else {
                // 模拟飞书彩色 Logo
                Box(
                    modifier = Modifier
                        .size(size)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF3370FF), Color(0xFF9F33FF))
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun MarkdownTextMock(content: String) {
    val parts = content.split("```")

    Column {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // 代码块部分
                CodeBlock(code = part)
            } else {
                // 普通文本
                if (part.isNotEmpty()) {
                    Text(
                        text = part.trim(),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF1F2329) // 深黑色
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlock(code: String) {
    val lines = code.trim().lines()
    val language = if (lines.isNotEmpty()) lines.first() else ""
    val actualCode = if (lines.size > 1) lines.drop(1).joinToString("\n") else code

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF5F6F7)) // 浅灰背景
            .padding(12.dp)
    ) {
        if (language.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = language,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = actualCode,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF1F2329),
            fontSize = 13.sp
        )
    }
}