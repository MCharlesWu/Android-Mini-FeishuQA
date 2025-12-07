package com.example.feishuqa.app.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.example.feishuqa.common.utils.theme.FeishuQATheme
import com.example.feishuqa.data.repository.ChatRepository
import com.example.feishuqa.data.repository.ChatRepositoryExample

/**
 * 聊天界面Activity
 * 负责承载聊天界面的Compose内容
 */
class ChatActivity : ComponentActivity() {
    
    private lateinit var chatViewModel: ChatViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化ViewModel - 使用正确的构造函数
        chatViewModel = ChatViewModel(application)
        
        // 设置默认对话ID
        val conversationId = intent.getStringExtra("conversation_id") ?: "1"
        chatViewModel.setCurrentConversation(conversationId)
        
        setContent {
            FeishuQATheme {
                ChatScreen(
                    onBackClick = { finish() },
                    viewModel = chatViewModel
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    FeishuQATheme {
        ChatScreen()
    }
}