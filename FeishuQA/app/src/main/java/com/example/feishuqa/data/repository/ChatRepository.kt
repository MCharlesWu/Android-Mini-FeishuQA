package com.example.feishuqa.data.repository

import com.example.feishuqa.data.model.HistoryChatItem

/**
 * 聊天数据仓库
 * 负责历史对话等数据管理
 */
class ChatRepository {

    companion object {
        @Volatile
        private var instance: ChatRepository? = null

        fun getInstance(): ChatRepository {
            return instance ?: synchronized(this) {
                instance ?: ChatRepository().also { instance = it }
            }
        }
    }

    /**
     * 获取历史对话列表
     * TODO: 实际项目中从后端API获取
     */
    fun getHistoryChatList(): List<HistoryChatItem> {
        // 模拟数据
        return listOf(
            HistoryChatItem("1", "简单介绍一下自己"),
            HistoryChatItem("2", "如何使用飞书文档"),
            HistoryChatItem("3", "项目进度汇报模板"),
            HistoryChatItem("4", "团队协作最佳实践"),
            HistoryChatItem("5", "会议纪要怎么写")
        )
    }

    /**
     * 创建新对话
     * TODO: 实际项目中调用后端API
     */
    fun createNewChat(): String {
        // 返回新对话ID
        return System.currentTimeMillis().toString()
    }
}


