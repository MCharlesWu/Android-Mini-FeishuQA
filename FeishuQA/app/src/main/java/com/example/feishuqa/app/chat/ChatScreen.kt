package com.example.feishuqa.app.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feishuqa.common.utils.components.ChatInputBar
import com.example.feishuqa.common.utils.components.MessageItem
import kotlinx.coroutines.launch

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 监听消息变化，自动滚动到最新消息
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty() && !uiState.isLoadingMore) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("知识问答", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    if (uiState.error != null) {
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.Black)
                        }
                    }
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
                },
                isTyping = uiState.isTyping
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error ?: "发生错误",
                        onRetry = { viewModel.loadMessages(viewModel.currentConversationId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.messages.isEmpty() -> {
                    WelcomeContent()
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // 加载更多提示
                        if (uiState.hasMoreMessages) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isLoadingMore) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    } else {
                                        TextButton(onClick = { viewModel.loadMoreMessages() }) {
                                            Text("加载更多消息")
                                        }
                                    }
                                }
                            }
                        }

                        // 消息列表
                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { message ->
                            MessageItem(
                                message = message,
                                onRegenerate = { viewModel.regenerateAiResponse(message.id) },
                                enableTypingEffect = uiState.config.enableTypingEffect &&
                                    message.status == com.example.feishuqa.data.entity.MessageStatus.TYPING,
                                typingSpeed = uiState.config.typingSpeed
                            )
                        }

                        // AI正在输入指示器
                        if (uiState.isTyping) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Card(
                                        modifier = Modifier.padding(start = 40.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "AI正在思考...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
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