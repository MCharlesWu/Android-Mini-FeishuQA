package com.example.feishuqa.app.chat

import com.example.feishuqa.data.entity.Message

/**
 * 聊天模型数据类
 * 用于定义聊天相关的数据模型
 */
data class ChatModel(
    val conversationId: String = "",
    val title: String = "",
    val lastMessage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageCount: Int = 0
)

/**
 * 聊天配置
 */
data class ChatConfig(
    val enableTypingEffect: Boolean = true,
    val typingSpeed: Long = 50L,
    val enableMarkdown: Boolean = true,
    val enableHistory: Boolean = true,
    val maxMessages: Int = 1000
)

/**
 * 聊天状态
 */
sealed class ChatState {
    object Idle : ChatState()
    object Loading : ChatState()
    object Typing : ChatState()
    data class Error(val message: String) : ChatState()
    object Success : ChatState()
    object Empty : ChatState()
}