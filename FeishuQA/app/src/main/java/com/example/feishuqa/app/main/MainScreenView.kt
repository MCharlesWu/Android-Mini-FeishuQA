package com.example.feishuqa.app.main

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feishuqa.common.utils.components.*
import com.example.feishuqa.common.utils.theme.*
import com.example.feishuqa.data.entity.AIModel
import com.example.feishuqa.data.entity.AIModels
import com.example.feishuqa.data.entity.Conversation
import kotlinx.coroutines.launch

/**
 * 主界面视图
 * 布局结构：
 * - ModalNavigationDrawer (对应XML的DrawerLayout)
 * - ConstraintLayout 主界面布局
 * - 侧边栏使用Column (对应XML的LinearLayout)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenView(
    viewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // UI状态
    var selectedModel by remember { mutableStateOf(AIModels.defaultModel) }
    var isWebSearchEnabled by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    
    // 模拟历史对话数据
    val mockConversations = remember {
        listOf(
            Conversation(
                id = "1",
                title = "简单介绍一下自己",
                lastMessageTime = System.currentTimeMillis() - 3600000
            ),
            Conversation(
                id = "2",
                title = "如何使用飞书文档",
                lastMessageTime = System.currentTimeMillis() - 7200000
            ),
            Conversation(
                id = "3",
                title = "项目进度汇报模板",
                lastMessageTime = System.currentTimeMillis() - 86400000
            ),
            Conversation(
                id = "4",
                title = "团队协作最佳实践",
                lastMessageTime = System.currentTimeMillis() - 172800000
            ),
            Conversation(
                id = "5",
                title = "会议纪要怎么写",
                lastMessageTime = System.currentTimeMillis() - 259200000
            )
        )
    }

    // 监听导航事件
    LaunchedEffect(Unit) {
        viewModel.navigateToConversation.collect { conversationId ->
            Log.d("MainScreen", "导航到对话: $conversationId")
        }
    }

    // DrawerLayout - 使用ModalNavigationDrawer实现
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                // 侧边栏内容 (LinearLayout风格)
                DrawerContent(
                    conversations = mockConversations,
                    selectedConversationId = selectedConversationId,
                    onNewConversation = {
                        scope.launch {
                            drawerState.close()
                        }
                        Toast.makeText(context, "创建新对话", Toast.LENGTH_SHORT).show()
                    },
                    onConversationClick = { id ->
                        selectedConversationId = id
                        scope.launch {
                            drawerState.close()
                        }
                        Toast.makeText(context, "打开对话: $id", Toast.LENGTH_SHORT).show()
                    },
                    onKnowledgeBaseClick = {
                        Toast.makeText(context, "打开知识库", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        },
        gesturesEnabled = true
    ) {
        // 主界面内容 - 使用ConstraintLayout
        Scaffold(
            containerColor = Color.White,
            topBar = {
                MainTopBar(
                    onLoginClick = {
                        Toast.makeText(context, "登录", Toast.LENGTH_SHORT).show()
                    },
                    onNewChatClick = {
                        Toast.makeText(context, "创建新对话", Toast.LENGTH_SHORT).show()
                    },
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        ) { paddingValues ->
            // ConstraintLayout主体布局
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val (welcomeContent, inputBar) = createRefs()

                // 欢迎内容区域
                WelcomeSection(
                    modifier = Modifier.constrainAs(welcomeContent) {
                        top.linkTo(parent.top)
                        bottom.linkTo(inputBar.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        height = Dimension.fillToConstraints
                    }
                )

                // 底部输入栏
                ChatInputBar(
                    selectedModel = selectedModel,
                    isWebSearchEnabled = isWebSearchEnabled,
                    onWebSearchToggle = { enabled ->
                        isWebSearchEnabled = enabled
                    },
                    onModelSelect = {
                        showModelDialog = true
                    },
                    onSendText = { text ->
                        viewModel.onSendQuestion(context, text)
                    },
                    onAttachClick = {
                        Toast.makeText(context, "上传附件", Toast.LENGTH_SHORT).show()
                    },
                    onVoiceClick = {
                        Toast.makeText(context, "语音输入", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.constrainAs(inputBar) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )
            }
        }
    }

    // 模型选择对话框
    if (showModelDialog) {
        ModelSelectorDialog(
            selectedModel = selectedModel,
            onModelSelected = { model ->
                selectedModel = model
            },
            onDismiss = {
                showModelDialog = false
            }
        )
    }
}

/**
 * 顶部导航栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    onLoginClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            // 登录按钮
            TextButton(
                onClick = onLoginClick
            ) {
                Text(
                    text = "登录",
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            }
        },
        actions = {
            // 新建对话按钮
            IconButton(onClick = onNewChatClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "新建对话",
                    tint = TextPrimary
                )
            }
            // 菜单按钮（打开侧边栏）
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "菜单",
                    tint = TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

/**
 * 欢迎区域内容
 */
@Composable
private fun WelcomeSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 动画Logo
        AnimatedLogo(
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 欢迎语
        Text(
            text = "嗨！这里是飞书知识问答",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 副标题
        Text(
            text = "整合你可访问的 2,140,886 个知识点，AI 搜索生成回答",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

/**
 * 动画Logo - 渐变色圆环
 */
@Composable
private fun AnimatedLogo(
    modifier: Modifier = Modifier
) {
    // 旋转动画
    val infiniteTransition = rememberInfiniteTransition(label = "logoRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 渐变圆环
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // 绘制渐变圆弧
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        GradientCyan,
                        FeishuBlue,
                        GradientPurple,
                        GradientPink,
                        GradientOrange,
                        GradientCyan
                    )
                ),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }
    }
}
