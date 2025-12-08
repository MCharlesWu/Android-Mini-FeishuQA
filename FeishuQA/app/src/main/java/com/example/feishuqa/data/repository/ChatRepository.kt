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
        // 初始化一些示例消息用于测试
        initializeSampleMessages()
    }

    private fun initializeSampleMessages() {
        val sampleMessages = listOf(
            Message(
                id = "sample_1",
                conversationId = "1",
                senderId = "user",
                type = MessageType.TEXT,
                content = "你好，请介绍一下你自己",
                status = MessageStatus.SENT,
                timestamp = System.currentTimeMillis() - 3600000
            ),
            Message(
                id = "sample_2",
                conversationId = "1",
                senderId = "ai",
                type = MessageType.TEXT,
                content = """我是一个知识问答助手，主要擅长通过阅读、归纳和总结信息来为用户解答问题。无论是知识查询、信息整理还是日常疑问，我都会尽力提供准确、清晰的回答，帮助你高效获取所需内容。

AI 基于你有权限的资料生成，数据保密仅你可见。""",
                status = MessageStatus.SENT,
                timestamp = System.currentTimeMillis() - 3500000
            ),
            Message(
                id = "sample_3",
                conversationId = "1",
                senderId = "user",
                type = MessageType.TEXT,
                content = "可以给我一个Kotlin代码示例吗？",
                status = MessageStatus.SENT,
                timestamp = System.currentTimeMillis() - 1800000
            ),
            Message(
                id = "sample_4",
                conversationId = "1",
                senderId = "ai",
                type = MessageType.TEXT,
                content = """好的，这是一个 Kotlin 的 Compose 示例：

```kotlin
@Composable
fun Greeting(name: String) {
    Text(text = "Hello, ${'$'}name!")
}
```

这段代码定义了一个简单的 UI 组件，用于显示问候语。在 Jetpack Compose 中，我们使用 @Composable 注解来标记可组合函数。""",
                status = MessageStatus.SENT,
                timestamp = System.currentTimeMillis() - 1700000
            )
        )
        _localMessages.addAll(sampleMessages)
    }

    // 获取历史消息 (支持分页)
    suspend fun getMessages(conversationId: String, page: Int, pageSize: Int = 20): List<Message> {
        delay(200) // 模拟极快读取
        val allMessages = _localMessages.filter { it.conversationId == conversationId }
            .sortedByDescending { it.timestamp }
        
        val startIndex = (page - 1) * pageSize
        val endIndex = startIndex + pageSize
        
        return if (startIndex < allMessages.size) {
            allMessages.subList(startIndex, minOf(endIndex, allMessages.size))
        } else {
            emptyList()
        }
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
        delay(1000 + (Math.random() * 1000).toLong()) // 1-2秒随机延迟
        
        val fullResponse = mockAiResponse(userQuery)
        val stringBuilder = StringBuilder()

        // 模拟逐字输出，速度随机变化
        for (char in fullResponse) {
            delay((20 + Math.random() * 40).toLong()) // 20-60ms随机延迟
            stringBuilder.append(char)
            emit(stringBuilder.toString())
        }
    }

    private fun mockAiResponse(query: String): String {
        return when {
            query.contains("你好", ignoreCase = true) || query.contains("hi", ignoreCase = true) -> """
                你好！我是一个知识问答助手，很高兴为你提供帮助。
                
                我可以协助你解答各种问题，包括：
                - **知识查询**：提供准确的信息和解释
                - **代码示例**：分享编程相关的代码片段
                - **文档整理**：帮助你整理和总结内容
                - **日常疑问**：回答各种实用问题
                
                AI 基于你有权限的资料生成，数据保密仅你可见。
                有什么我可以帮助你的吗？
            """.trimIndent()
            
            query.contains("代码", ignoreCase = true) || query.contains("kotlin", ignoreCase = true) -> """
                好的！这是一个 Kotlin 的 Compose 示例：

                ```kotlin
                @Composable
                fun Greeting(name: String) {
                    Text(
                        text = "Hello, ${'$'}name!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                ```

                这个组件展示了 Jetpack Compose 的基本用法：
                - **@Composable**：标记可组合函数
                - **参数传递**：通过函数参数传递数据
                - **样式设置**：使用 Modifier 和主题样式
                - **文本显示**：使用 Text 组件显示内容

                你可以这样调用它：
                ```kotlin
                Greeting(name = "World")
                ```
            """.trimIndent()
            
            query.contains("表格", ignoreCase = true) || query.contains("table", ignoreCase = true) -> """
                下面是一个 Markdown 表格示例：

                | 功能 | 描述 | 状态 |
                |------|------|------|
                | 文本消息 | 支持富文本显示 | ✅ 已完成 |
                | 打字机效果 | AI回复逐字显示 | ✅ 已完成 |
                | Markdown解析 | 支持标题、代码块、表格等 | ✅ 已完成 |
                | 历史记录 | 支持消息持久化 | ✅ 已完成 |
                | 文件上传 | 支持附件发送 | 🚧 开发中 |
                | 语音输入 | 支持语音转文字 | 📋 规划中 |

                这个表格展示了当前应用的功能状态。
            """.trimIndent()
            
            query.contains("标题", ignoreCase = true) || query.contains("heading", ignoreCase = true) -> """
                # 一级标题
                ## 二级标题
                ### 三级标题
                #### 四级标题
                
                这是不同级别的 Markdown 标题示例。
                
                ## 功能特性
                
                ### 核心功能
                - **打字机效果**：AI回复逐字显示
                - **Markdown支持**：完整解析各种格式
                - **历史记录**：消息持久化存储
                
                ### 技术特点
                - 基于 **MVVM** 架构
                - 使用 **Jetpack Compose**
                - 支持 **协程** 和 **Flow**
            """.trimIndent()
            
            else -> """我收到了你的消息："$query"

这是一个模拟的 AI 回复，展示了以下功能：

- **打字机效果**：你看到的内容正在逐字显示
- **Markdown支持**：支持 **粗体**、*斜体*、\`代码\` 等格式
- **实时响应**：模拟真实的 AI 对话体验

你可以尝试询问：
- "你好" - 查看自我介绍
- "代码" - 获取 Kotlin 示例
- "表格" - 查看表格渲染
- "标题" - 查看标题格式

有什么其他问题我可以帮助你解答吗？"""
        }
    }
}