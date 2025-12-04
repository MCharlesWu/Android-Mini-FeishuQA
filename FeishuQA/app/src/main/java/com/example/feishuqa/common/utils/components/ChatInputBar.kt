package com.example.feishuqa.common.utils.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
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
import com.example.feishuqa.common.utils.theme.FeishuBlue
import com.example.feishuqa.common.utils.theme.FeishuBlueLight
import com.example.feishuqa.common.utils.theme.TextHint
import com.example.feishuqa.common.utils.theme.TextPrimary
import com.example.feishuqa.common.utils.theme.TextSecondary
import com.example.feishuqa.data.entity.AIModel
import com.example.feishuqa.data.entity.AIModels

/**
 * 聊天输入栏组件
 * 包含：联网搜索按钮、模型选择、附件上传、文字/语音输入
 */
@Composable
fun ChatInputBar(
    selectedModel: AIModel = AIModels.defaultModel,
    isWebSearchEnabled: Boolean = false,
    onWebSearchToggle: (Boolean) -> Unit = {},
    onModelSelect: () -> Unit = {},
    onSendText: (String) -> Unit,
    onAttachClick: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    // 动画化联网搜索按钮颜色
    val webSearchBackgroundColor by animateColorAsState(
        targetValue = if (isWebSearchEnabled) FeishuBlueLight else Color(0xFFF5F6F7),
        animationSpec = tween(200),
        label = "webSearchBg"
    )
    val webSearchTextColor by animateColorAsState(
        targetValue = if (isWebSearchEnabled) FeishuBlue else TextSecondary,
        animationSpec = tween(200),
        label = "webSearchText"
    )

    Column(
        modifier = modifier
            .background(Color.White)
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        // 第一行：功能开关区域（联网搜索 + 模型选择）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 联网搜索按钮
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = webSearchBackgroundColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onWebSearchToggle(!isWebSearchEnabled) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = webSearchTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "联网搜索",
                        fontSize = 12.sp,
                        color = webSearchTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 模型选择按钮
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF5F6F7),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onModelSelect)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedModel.name,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
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
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "上传附件",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 中间输入框
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF5F6F7))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "问个问题，或用知识写点内容",
                        color = TextHint,
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(FeishuBlue),
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 右侧按钮（发送/语音切换）
            if (text.isNotBlank()) {
                // 发送按钮
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FeishuBlue)
                        .clickable {
                            onSendText(text)
                            text = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "发送",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                // 语音按钮
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "语音输入",
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}

/**
 * 增强版聊天输入栏 - 带完整状态管理
 */
@Composable
fun EnhancedChatInputBar(
    onSendMessage: (String, AIModel, Boolean) -> Unit,
    onAttachClick: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedModel by remember { mutableStateOf(AIModels.defaultModel) }
    var isWebSearchEnabled by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ChatInputBar(
            selectedModel = selectedModel,
            isWebSearchEnabled = isWebSearchEnabled,
            onWebSearchToggle = { isWebSearchEnabled = it },
            onModelSelect = { showModelDialog = true },
            onSendText = { text ->
                onSendMessage(text, selectedModel, isWebSearchEnabled)
            },
            onAttachClick = onAttachClick,
            onVoiceClick = onVoiceClick
        )
    }

    // 模型选择对话框
    if (showModelDialog) {
        ModelSelectorDialog(
            selectedModel = selectedModel,
            onModelSelected = { selectedModel = it },
            onDismiss = { showModelDialog = false }
        )
    }
}
