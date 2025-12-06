package com.example.feishuqa.data.entity

import kotlinx.serialization.Serializable

/**
 * 会话详情实体类，对应/data/data/com.example.feishuqa/files/conversations/conv_001.json
 */
@Serializable
data class ConversationDetail
(
    val conversationId: String,
    val userId: String,
    var title: String,
    val createTime: Long,
    var updateTime: Long,
    var isPinned: Boolean = false,
    val modelType: String = "gpt-3.5", // 默认模型类型
    val messageDir: String
)