package com.example.feishuqa.data.entity

import java.util.UUID

enum class MessageType {
    TEXT, IMAGE, AUDIO, FILE
}

enum class MessageStatus {
    SENDING, SENT, FAILED, TYPING
}

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderId: String, // "user" or "ai"
    val type: MessageType,
    val content: String, // 文本内容 或 文件路径/Url
    val extraInfo: String? = null, // 用于存储文件大小、语音时长、PDF解析后的摘要等
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT
) {
    fun isUser() = senderId == "user"
}