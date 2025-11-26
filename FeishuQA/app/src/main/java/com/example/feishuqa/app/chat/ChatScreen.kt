package com.example.feishuqa.app.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feishuqa.common.utils.components.ChatInputBar
import com.example.feishuqa.common.utils.components.MessageItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { }, // 标题留空，追求极简
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = { /* History */ }) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = Color.Black)
                    }
                    IconButton(onClick = { /* New Chat */ }) {
                        Icon(Icons.Outlined.CloseFullscreen, contentDescription = "Maximize", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                onSendText = { text ->
                    viewModel.sendTextMessage(text)
                },
                onAttachClick = {
                    // TODO: 打开文件选择器或图片选择器
                },
                onVoiceClick = {
                    // TODO: 录音逻辑
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.messages.isEmpty()) {
                // 空状态：欢迎页
                WelcomeContent()
            } else {
                // 消息列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // 列表头部 (实际是视觉底部，因为 reverseLayout)
                    // 加载更多提示
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }

                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        MessageItem(message = message)
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 模拟 Logo
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = Color.Transparent,
            modifier = Modifier
                .size(64.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(Color(0xFF3370FF), Color(0xFF9F33FF))
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        ) {
            // 空内容，只显示渐变圆
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "飞书知识问答",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "整合你可访问的 2,140,886 个知识点，AI 搜索生成回答",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}