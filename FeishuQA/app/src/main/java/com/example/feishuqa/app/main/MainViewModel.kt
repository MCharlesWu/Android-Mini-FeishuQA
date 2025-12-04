package com.example.feishuqa.app.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feishuqa.data.entity.AIModel
import com.example.feishuqa.data.entity.AIModels
import com.example.feishuqa.data.entity.Conversation
import com.example.feishuqa.data.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 主界面ViewModel
 * ViewModel层：处理业务逻辑，管理UI状态
 */
class MainViewModel(private val context: Context) : ViewModel() {

    private val repository = MainRepository(context)

    // UI状态
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // 导航事件
    private val _navigateToConversation = MutableStateFlow<String?>(null)
    val navigateToConversation: StateFlow<String?> = _navigateToConversation.asStateFlow()

    init {
        loadConversations()
    }

    /**
     * 加载历史对话列表
     */
    fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val conversations = repository.getConversations()
                _uiState.value = _uiState.value.copy(
                    conversations = conversations,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * 切换联网搜索状态
     */
    fun toggleWebSearch() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isWebSearchEnabled = !currentState.isWebSearchEnabled)
    }

    /**
     * 切换输入模式
     */
    fun toggleInputMode() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isTextInputMode = !currentState.isTextInputMode)
    }

    /**
     * 选择AI模型
     */
    fun selectModel(model: AIModel) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    /**
     * 发送消息
     */
    fun sendMessage(text: String) {
        viewModelScope.launch {
            // TODO: 实际发送消息到API
            // 这里可以调用Repository发送消息
        }
    }

    /**
     * 创建新对话
     */
    fun createNewConversation(title: String) {
        viewModelScope.launch {
            val result = repository.createConversation(title)
            result.onSuccess { conversationId ->
                _navigateToConversation.value = conversationId
                loadConversations() // 刷新列表
            }.onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }

    /**
     * 选择对话
     */
    fun selectConversation(conversationId: String) {
        _uiState.value = _uiState.value.copy(selectedConversationId = conversationId)
        _navigateToConversation.value = conversationId
    }

    /**
     * 清除导航事件
     */
    fun clearNavigation() {
        _navigateToConversation.value = null
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * 获取可用模型列表
     */
    fun getAvailableModels(): List<AIModel> {
        return repository.getAvailableModels()
    }
}

/**
 * 主界面UI状态
 */
data class MainUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedModel: AIModel = AIModels.defaultModel,
    val isWebSearchEnabled: Boolean = false,
    val isTextInputMode: Boolean = true,
    val selectedConversationId: String? = null
)

