package com.example.feishuqa.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.feishuqa.common.utils.ImageUtils
import com.example.feishuqa.common.utils.JsonUtils
import com.example.feishuqa.data.entity.Message
import com.example.feishuqa.data.entity.MessageType
import com.example.feishuqa.data.entity.MessageStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.also
import kotlin.collections.find


// 单例 Repository
class ChatRepositoryExample private constructor(private val context: Context) {

    private val _messages = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messages: LiveData<MutableList<Message>> = _messages
    
    // 当前对话ID
    private var currentConversationId: String? = null
    
    // 本地存储文件名（根据对话ID区分）
    private fun getChatHistoryFile(conversationId: String): String = "chat_history_$conversationId.json"

    companion object {
        @Volatile
        private var instance: ChatRepositoryExample? = null

        // 在 Application 或 MainActivity 初始化一次
        fun getInstance(context: Context): ChatRepositoryExample {
            return instance ?: synchronized(this) {
                instance ?: ChatRepositoryExample(context.applicationContext).also { instance = it }
            }
        }
    }
    
    init {
        // 不再在初始化时加载，等待设置对话ID后加载
    }
    
    /**
     * 设置当前对话ID，并加载对应的消息
     */
    fun setCurrentConversation(conversationId: String) {
        if (currentConversationId != conversationId) {
            currentConversationId = conversationId
            loadChatHistory()
        }
    }
    
    /**
     * 清除当前对话
     */
    fun clearCurrentConversation() {
        currentConversationId = null
        _messages.postValue(mutableListOf())
    }

    // 核心业务：发送消息（包含压缩、上传模拟、状态流转）
    fun sendMessage(text: String, uri: Uri?) {
        val msgId = UUID.randomUUID().toString()
        val conversationId = currentConversationId ?: return

        // 1. 立即展示 loading 状态
        val userMsg = Message(
            id = msgId,
            conversationId = conversationId,
            senderId = "user",
            type = if (uri != null) MessageType.IMAGE else MessageType.TEXT,
            content = text,
            extraInfo = uri?.toString(), // 先暂时用 Uri 显示预览
            timestamp = System.currentTimeMillis(),
            status = if (uri != null) MessageStatus.SENDING else MessageStatus.SENT
        )
        addMessageInternal(userMsg)

        // 2. 后台处理
        CoroutineScope(Dispatchers.IO).launch {
            var finalPath: String? = null

            // 2.1 如果是图片，进行压缩
            if (uri != null) {
                // 【核心】在这里使用 context 调用压缩，ViewModel 不需要知道 context
                finalPath = ImageUtils.compressImage(context, uri)
                // 模拟网络上传耗时
                delay(1500)

                // 2.2 更新消息状态（压缩完成，替换为真实路径，Loading 结束）
                updateMessageStatus(msgId, finalPath, MessageStatus.SENT)
            }

            // 3. 模拟 AI 流式回复
            val aiMsgId = UUID.randomUUID().toString()
            val aiMsg = Message(
                id = aiMsgId,
                conversationId = conversationId,
                senderId = "ai",
                type = MessageType.TEXT,
                content = "", // 初始内容为空
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENDING // 初始状态为发送中
            )
            
            withContext(Dispatchers.Main) { addMessageInternal(aiMsg) }
            
            // 4. 流式输出 AI 回复
            var fullContent = ""
            streamAiResponse(text, conversationId).collect { partialContent ->
                fullContent = partialContent
                // 更新消息内容（逐字显示）
                updateMessageContent(aiMsgId, fullContent)
            }
            
            // 5. 更新消息状态为已发送
            updateMessageStatus(aiMsgId, null, MessageStatus.SENT)
        }
    }

    private fun addMessageInternal(msg: Message) {
        val list = _messages.value ?: mutableListOf()
        list.add(msg)
        _messages.postValue(list)
        saveChatHistory() // 保存到本地
    }

    private suspend fun updateMessageStatus(id: String, realPath: String?, status: MessageStatus) {
        withContext(Dispatchers.Main) {
            val list = _messages.value ?: return@withContext
            val targetIndex = list.indexOfFirst { it.id == id }
            if (targetIndex != -1) {
                val target = list[targetIndex]
                val updatedMessage = target.copy(
                    status = status,
                    extraInfo = realPath ?: target.extraInfo
                )
                list[targetIndex] = updatedMessage
                _messages.value = list // 触发刷新
                saveChatHistory() // 保存到本地
            }
        }
    }
    
    private suspend fun updateMessageContent(id: String, content: String) {
        withContext(Dispatchers.Main) {
            val list = _messages.value ?: return@withContext
            val targetIndex = list.indexOfFirst { it.id == id }
            if (targetIndex != -1) {
                val target = list[targetIndex]
                val updatedMessage = target.copy(content = content)
                list[targetIndex] = updatedMessage
                _messages.value = list // 触发刷新
            }
        }
    }
    
    /**
     * 加载本地聊天历史
     */
    private fun loadChatHistory() {
        val conversationId = currentConversationId ?: return
        
        try {
            val fileName = getChatHistoryFile(conversationId)
            val jsonContent = JsonUtils.readJsonFromFiles(context, fileName)
            if (jsonContent.isNotEmpty() && jsonContent != "[]") {
                val jsonArray = JSONArray(jsonContent)
                val loadedMessages = mutableListOf<Message>()
                
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val message = Message(
                        id = jsonObject.getString("id"),
                        conversationId = jsonObject.getString("conversationId"),
                        senderId = jsonObject.getString("senderId"),
                        type = MessageType.valueOf(jsonObject.getString("type")),
                        content = jsonObject.getString("content"),
                        extraInfo = jsonObject.optString("extraInfo").takeIf { it.isNotEmpty() },
                        timestamp = jsonObject.getLong("timestamp"),
                        status = MessageStatus.valueOf(jsonObject.getString("status"))
                    )
                    loadedMessages.add(message)
                }
                
                // 按时间戳排序（最新的在最后）
                loadedMessages.sortBy { it.timestamp }
                _messages.postValue(loadedMessages)
            } else {
                // 没有历史消息，清空当前列表
                _messages.postValue(mutableListOf())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果加载失败，保持空列表
            _messages.postValue(mutableListOf())
        }
    }
    
    /**
     * 保存聊天历史到本地
     */
    private fun saveChatHistory() {
        val conversationId = currentConversationId ?: return
        
        try {
            val messages = _messages.value ?: return
            val jsonArray = JSONArray()
            
            messages.forEach { message ->
                val jsonObject = JSONObject().apply {
                    put("id", message.id)
                    put("conversationId", message.conversationId)
                    put("senderId", message.senderId)
                    put("type", message.type.name)
                    put("content", message.content)
                    put("extraInfo", message.extraInfo ?: "")
                    put("timestamp", message.timestamp)
                    put("status", message.status.name)
                }
                jsonArray.put(jsonObject)
            }
            
            val fileName = getChatHistoryFile(conversationId)
            JsonUtils.overwriteJsonArray(context, fileName, jsonArray)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 清空当前对话的聊天历史
     */
    fun clearChatHistory() {
        val conversationId = currentConversationId ?: return
        
        try {
            // 清空内存中的消息
            _messages.postValue(mutableListOf())
            // 删除本地文件
            val fileName = getChatHistoryFile(conversationId)
            context.deleteFile(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 模拟 AI 流式回复 (打字机效果)
     */
    private fun streamAiResponse(userQuery: String, conversationId: String): Flow<String> = flow {
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