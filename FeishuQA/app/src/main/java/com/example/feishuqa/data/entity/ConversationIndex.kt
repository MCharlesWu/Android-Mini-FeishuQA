package com.example.feishuqa.data.entity

import kotlinx.serialization.Serializable

/**
 * 会话索引实体类，对应对应/data/data/com.example.feishuqa/files/index/conversations.json
 */
@Serializable
data class ConversationIndex
(
    val conversationId: String,  // 会话唯一ID
    val userId: String,          // 所属用户ID
    var title: String,           // 会话标题
    val createTime: Long,        // 创建时间
    var updateTime: Long,        // 最后更新时间
    var isPinned: Boolean = false, // 是否置顶
    var isDeleted: Boolean = false, // 是否已删除
    val dataFile: String,        // 详情文件路径
    val messageDir: String       // 消息目录路径
)

// 索引文件容器
@Serializable
data class ConversationIndexFile
(
    val version: String = "1.0",
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val conversations: MutableList<ConversationIndex> = mutableListOf()
)