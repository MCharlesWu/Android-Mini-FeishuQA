package com.example.feishuqa.data.repository

import com.example.feishuqa.data.entity.Message
import com.example.feishuqa.data.entity.MessageStatus
import com.example.feishuqa.data.entity.MessageType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class ChatRepository {

    private val _localMessages = mutableListOf<Message>()

    suspend fun getMessages(conversationId: String, page: Int, pageSize: Int = 20): List<Message> {
        delay(200)
        return _localMessages.filter { it.conversationId == conversationId }
            .sortedByDescending { it.timestamp }
            .take(pageSize * page)
    }

    suspend fun sendMessage(message: Message): Boolean {
        delay(300)
        _localMessages.add(0, message)
        return true
    }

    fun streamAiResponse(userQuery: String, conversationId: String): Flow<String> = flow {
        delay(1500)
        
        val fullResponse = mockAiResponse(userQuery)
        val stringBuilder = StringBuilder()

        for (char in fullResponse) {
            delay(15) 
            stringBuilder.append(char)
            emit(stringBuilder.toString())
        }
    }

    private fun mockAiResponse(query: String): String {
        return when {
            query.contains("测试") -> """
                # Markdown 全功能测试
                
                ## 1. 文本样式
                这是普通文本，支持 **粗体**、`行内代码`、~~删除线~~ 和 [飞书链接](https://feishu.cn)。
                
                ## 2. 列表
                **有序列表：**
                1. 第一步：打开应用
                2. 第二步：输入问题
                
                **无序列表：**
                - 简洁高效
                - 智能搜索
                
                ```kotlin
                fun main() {
                    println("Hello FeishuQA!")
                }
                ```
            """.trimIndent()
            
            // 修复：针对包含代码块的输入，先换行再引用，避免破坏 Markdown 结构
            query.contains("```") -> """
                我收到了你发送的代码片段：
                
                $query
                
                这段代码看起来很不错！需要我为你解释一下吗？
            """.trimIndent()
            
            else -> "我收到了你的消息：\n\n$query\n\n这是一个模拟的 AI 回复。"
        }
    }
}