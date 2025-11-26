package com.example.feishuqa.data.repository

import com.example.feishuqa.data.entity.Message
import com.example.feishuqa.data.entity.MessageStatus
import com.example.feishuqa.data.entity.MessageType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * 模拟数据仓库，负责提供对话数据和模拟AI交互
 */
class ChatRepository {

    // 内存中模拟存储消息
    private val _localMessages = mutableListOf<Message>()

    init {
        // 初始为空，以展示欢迎页
        // 如果需要测试列表页，可以手动发送一条消息
    }

    // 获取历史消息 (支持分页)
    suspend fun getMessages(conversationId: String, page: Int, pageSize: Int = 20): List<Message> {
        delay(200) // 模拟极快读取
        return _localMessages.filter { it.conversationId == conversationId }
            .sortedByDescending { it.timestamp }
            .take(pageSize * page)
    }

    // 发送消息
    suspend fun sendMessage(message: Message): Boolean {
        delay(300)
        _localMessages.add(0, message)
        return true
    }

    // 模拟 AI 流式回复 (打字机效果)
    fun streamAiResponse(userQuery: String, conversationId: String): Flow<String> = flow {
        // 模拟深度思考时间
        delay(1500)
        
        val fullResponse = mockAiResponse(userQuery)
        val stringBuilder = StringBuilder()

        // 模拟逐字输出
        for (char in fullResponse) {
            delay(30) // 打字速度
            stringBuilder.append(char)
            emit(stringBuilder.toString())
        }
    }

    private fun mockAiResponse(query: String): String {
        return when {
            query.contains("自我介绍") -> """
                我是一个知识问答助手，主要擅长通过阅读、归纳和总结信息来为用户解答问题。无论是知识查询、信息整理还是日常疑问，我都会尽力提供准确、清晰的回答，帮助你高效获取所需内容。
                
                AI 基于你有权限的资料生成，数据保密仅你可见。
            """.trimIndent()
            query.contains("代码") -> """
                好的，这是一个 Kotlin 的 Compose 示例：
                ```kotlin
                @Composable
                fun Greeting(name: String) {
                    Text(text = "Hello, ${'$'}name!")
                }
                ```
                这段代码定义了一个简单的 UI 组件。
            """.trimIndent()
            else -> "我收到了你的消息：“$query”。\n这是一个模拟的 AI 回复。\n支持 **Markdown** 格式渲染。"
        }
    }
}