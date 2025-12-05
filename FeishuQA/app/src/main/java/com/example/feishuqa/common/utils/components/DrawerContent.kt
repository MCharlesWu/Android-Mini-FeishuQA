package com.example.feishuqa.common.utils.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feishuqa.common.utils.theme.*
import com.example.feishuqa.data.entity.Conversation
import java.text.SimpleDateFormat
import java.util.*

/**
 * 侧边栏抽屉内容组件
 * 使用LinearLayout风格的垂直布局
 */
@Composable
fun DrawerContent(
    conversations: List<Conversation>,
    selectedConversationId: String?,
    onNewConversation: () -> Unit,
    onConversationClick: (String) -> Unit,
    onKnowledgeBaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 使用Column模拟LinearLayout垂直布局
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp) // 状态栏安全区
    ) {
        // 1. 新建对话按钮
        NewConversationButton(
            onClick = onNewConversation
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 2. 知识库入口
        KnowledgeBaseEntry(
            onClick = onKnowledgeBaseClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 3. 历史对话标题
        Text(
            text = "历史对话",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 4. 历史对话列表（LinearLayout风格的垂直列表）
        if (conversations.isEmpty()) {
            // 空状态
            EmptyConversationState(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(
                    items = conversations,
                    key = { it.id }
                ) { conversation ->
                    DrawerConversationItem(
                        conversation = conversation,
                        isSelected = conversation.id == selectedConversationId,
                        onClick = { onConversationClick(conversation.id) }
                    )
                }
            }
        }
    }
}

/**
 * 新建对话按钮
 */
@Composable
private fun NewConversationButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FeishuBlue
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "新对话",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 知识库入口
 */
@Composable
private fun KnowledgeBaseEntry(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "知识库",
            fontSize = 16.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 侧边栏对话列表项
 */
@Composable
private fun DrawerConversationItem(
    conversation: Conversation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) FeishuBlueLight else Color.Transparent
    
    // LinearLayout风格的水平布局项
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 对话内容（垂直布局）
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.getDisplayTitle(),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 空对话状态
 */
@Composable
private fun EmptyConversationState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = TextDisabled,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "暂无历史对话",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

/**
 * 格式化时间
 */
private fun formatConversationTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        diff < 604800000 -> "${diff / 86400000}天前"
        else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
    }
}

