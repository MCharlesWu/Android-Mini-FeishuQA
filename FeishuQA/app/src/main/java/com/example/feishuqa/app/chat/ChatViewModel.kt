package com.example.feishuqa.app.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feishuqa.data.entity.Message
import com.example.feishuqa.data.entity.MessageStatus
import com.example.feishuqa.data.entity.MessageType
import com.example.feishuqa.data.repository.ChatRepository
import com.example.feishuqa.common.utils.JsonUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import android.app.Application
import androidx.lifecycle.AndroidViewModel

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoadingMore: Boolean = false,
    val isTyping: Boolean = false, // AI 是否正在输入
    val error: String? = null,
    val chatState: ChatState = ChatState.Idle,
    val config: ChatConfig = ChatConfig(),
    val currentPage: Int = 1,
    val hasMoreMessages: Boolean = true
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val context = application.applicationContext
    
    // 当前会话 ID
    private var conversationId: String = ""
    private var conversationFileName: String = ""
    
    // 公开访问当前会话ID
    val currentConversationId: String get() = conversationId

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // 初始化时不加载，等待设置对话ID后加载
    }
    
    /**
     * 设置当前对话ID并加载对应的消息
     */
    fun setCurrentConversation(conversationId: String) {
        if (this.conversationId != conversationId) {
            this.conversationId = conversationId
            this.conversationFileName = "conversation_$conversationId.json"
            // 清除当前消息并加载新对话的消息
            _uiState.update { it.copy(messages = emptyList(), chatState = ChatState.Idle) }
            loadMessages(conversationId)
        }
    }

    /**
     * 加载消息列表
     */
    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(chatState = ChatState.Loading) }
            try {
                val history = repository.getMessages(conversationId, page = _uiState.value.currentPage)
                val updatedMessages = if (_uiState.value.currentPage == 1) {
                    history
                } else {
                    _uiState.value.messages + history
                }
                
                _uiState.update { 
                    it.copy(
                        messages = updatedMessages,
                        chatState = if (updatedMessages.isEmpty()) ChatState.Empty else ChatState.Success,
                        hasMoreMessages = history.size >= 20 // 假设每页20条消息
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message,
                        chatState = ChatState.Error(e.message ?: "加载消息失败")
                    )
                }
            }
        }
    }

    /**
     * 加载更多历史消息（上拉加载）
     */
    fun loadMoreMessages() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMoreMessages) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = _uiState.value.currentPage + 1
            
            try {
                val moreMessages = repository.getMessages(conversationId, page = nextPage)
                val updatedMessages = _uiState.value.messages + moreMessages
                
                _uiState.update { 
                    it.copy(
                        messages = updatedMessages,
                        currentPage = nextPage,
                        isLoadingMore = false,
                        hasMoreMessages = moreMessages.size >= 20
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoadingMore = false,
                        error = e.message
                    )
                }
            }
        }
    }

    /**
     * 从本地存储加载聊天历史
     */
    private fun loadChatHistory() {
        viewModelScope.launch {
            try {
                val jsonContent = JsonUtils.readJsonFromFiles(context, conversationFileName)
                if (jsonContent.isNotEmpty() && jsonContent != "[]") {
                    // 解析JSON并更新消息列表
                    val jsonArray = org.json.JSONArray(jsonContent)
                    val loadedMessages = mutableListOf<Message>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val message = Message(
                            id = jsonObject.getString("id"),
                            conversationId = jsonObject.getString("conversationId"),
                            senderId = jsonObject.getString("senderId"),
                            type = MessageType.valueOf(jsonObject.getString("type")),
                            content = jsonObject.getString("content"),
                            extraInfo = jsonObject.optString("extraInfo", ""),
                            timestamp = jsonObject.getLong("timestamp"),
                            status = MessageStatus.valueOf(jsonObject.getString("status"))
                        )
                        loadedMessages.add(message)
                    }
                    
                    // 按时间戳倒序排列（最新的在前面）
                    loadedMessages.sortByDescending { it.timestamp }
                    
                    _uiState.update { state ->
                        state.copy(messages = loadedMessages, chatState = ChatState.Success)
                    }
                } else {
                    // 如果本地没有历史记录，加载模拟数据
                    loadMessages(conversationId)
                }
            } catch (e: Exception) {
                // 如果读取失败，使用默认的模拟数据
                e.printStackTrace()
                loadMessages(conversationId)
            }
        }
    }

    /**
     * 保存聊天历史到本地存储
     */
    private fun saveChatHistory() {
        viewModelScope.launch {
            try {
                val messages = _uiState.value.messages
                // 将消息列表转换为JSON格式并保存
                // 这里需要根据实际的消息结构来实现
                val jsonArray = org.json.JSONArray()
                messages.forEach { message ->
                    val jsonObject = org.json.JSONObject().apply {
                        put("id", message.id)
                        put("conversationId", message.conversationId)
                        put("senderId", message.senderId)
                        put("type", message.type.name)
                        put("content", message.content)
                        put("extraInfo", message.extraInfo)
                        put("timestamp", message.timestamp)
                        put("status", message.status.name)
                    }
                    jsonArray.put(jsonObject)
                }
                
                JsonUtils.overwriteJsonArray(context, conversationFileName, jsonArray)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 发送文本消息
     */
    fun sendTextMessage(content: String) {
        if (content.isBlank()) return

        val userMsg = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = "user",
            type = MessageType.TEXT,
            content = content,
            status = MessageStatus.SENDING,
            timestamp = System.currentTimeMillis()
        )

        // 1. 立即更新 UI 显示用户消息
        updateMessageList(userMsg)

        viewModelScope.launch {
            try {
                // 2. 发送给服务端（模拟）
                val success = repository.sendMessage(userMsg)
                if (success) {
                    updateMessageStatus(userMsg.id, MessageStatus.SENT)
                    // 保存消息到本地
                    saveChatHistory()
                    // 3. 触发 AI 回复
                    triggerAiResponse(content)
                } else {
                    updateMessageStatus(userMsg.id, MessageStatus.FAILED)
                    // 保存失败状态的消息
                    saveChatHistory()
                }
            } catch (e: Exception) {
                updateMessageStatus(userMsg.id, MessageStatus.FAILED)
                _uiState.update { 
                    it.copy(error = e.message, chatState = ChatState.Error(e.message ?: "发送消息失败"))
                }
                // 保存失败状态的消息
                saveChatHistory()
            }
        }
    }

    /**
     * 触发 AI 回复 (打字机效果)
     */
    private fun triggerAiResponse(userQuery: String) {
        val aiMsgId = UUID.randomUUID().toString()
        val initialAiMsg = Message(
            id = aiMsgId,
            conversationId = conversationId,
            senderId = "ai",
            type = MessageType.TEXT,
            content = "...", // 初始占位符
            status = MessageStatus.TYPING
        )

        updateMessageList(initialAiMsg)
        _uiState.update { it.copy(isTyping = true, chatState = ChatState.Typing) }

        viewModelScope.launch {
            try {
                repository.streamAiResponse(userQuery, conversationId).collect { partialContent ->
                    // 更新消息内容
                    _uiState.update { state ->
                        val updatedList = state.messages.map { msg ->
                            if (msg.id == aiMsgId) {
                                msg.copy(content = partialContent, status = MessageStatus.TYPING)
                            } else {
                                msg
                            }
                        }
                        state.copy(messages = updatedList)
                    }
                }
                
                // 结束输入状态
                _uiState.update { state ->
                    val updatedList = state.messages.map { msg ->
                        if (msg.id == aiMsgId) msg.copy(status = MessageStatus.SENT) else msg
                    }
                    state.copy(messages = updatedList, isTyping = false, chatState = ChatState.Success)
                }
                
                // 保存聊天历史
                saveChatHistory()
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    val updatedList = state.messages.map { msg ->
                        if (msg.id == aiMsgId) msg.copy(status = MessageStatus.FAILED) else msg
                    }
                    state.copy(
                        messages = updatedList, 
                        isTyping = false, 
                        chatState = ChatState.Error(e.message ?: "AI回复失败")
                    )
                }
            }
        }
    }

    /**
     * 重新生成AI回复
     */
    fun regenerateAiResponse(messageId: String) {
        val message = _uiState.value.messages.find { it.id == messageId }
        if (message != null && !message.isUser()) {
            // 删除旧的AI消息并重新生成
            _uiState.update { state ->
                val updatedList = state.messages.filter { it.id != messageId }
                state.copy(messages = updatedList)
            }
            
            // 找到对应用户消息并重新触发AI回复
            val userMessageIndex = _uiState.value.messages.indexOfFirst { it.id == messageId } + 1
            if (userMessageIndex < _uiState.value.messages.size) {
                val userMessage = _uiState.value.messages[userMessageIndex]
                if (userMessage.isUser()) {
                    triggerAiResponse(userMessage.content)
                }
            }
        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.update { it.copy(error = null, chatState = ChatState.Idle) }
    }

    private fun updateMessageList(newMessage: Message) {
        _uiState.update { state ->
            // 注意：我们使用 reverseLayout，所以新消息加到列表头部 (索引0)
            val updatedMessages = listOf(newMessage) + state.messages
            
            // 限制消息数量，避免内存溢出
            val finalMessages = if (updatedMessages.size > state.config.maxMessages) {
                updatedMessages.take(state.config.maxMessages)
            } else {
                updatedMessages
            }
            
            state.copy(messages = finalMessages)
        }
    }

    private fun updateMessageStatus(id: String, status: MessageStatus) {
        _uiState.update { state ->
            val updatedList = state.messages.map {
                if (it.id == id) it.copy(status = status) else it
            }
            state.copy(messages = updatedList)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 保存聊天历史
        saveChatHistory()
    }
}