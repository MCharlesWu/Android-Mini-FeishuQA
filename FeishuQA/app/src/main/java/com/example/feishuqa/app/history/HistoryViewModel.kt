package com.example.feishuqa.app.history

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.feishuqa.data.entity.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 历史对话 ViewModel
 * 使用新的索引文件结构，通过 HistoryModel 访问数据
 * 支持 Compose (StateFlow) 和 XML (LiveData) 两种 UI 框架
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val historyModel = HistoryModel(context)

    // StateFlow 用于 Compose UI
    private val _uiStateFlow = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiStateFlow.asStateFlow()

    // LiveData 用于 XML UI (Fragment)
    private val _uiStateLiveData = MutableLiveData<HistoryUiState>(HistoryUiState())
    val uiStateLiveData: LiveData<HistoryUiState> = _uiStateLiveData

    // 同步更新两个状态
    private fun updateUiState(newState: HistoryUiState) {
        _uiStateFlow.value = newState
        _uiStateLiveData.value = newState
    }

    // 当前登录用户ID（应该从登录模块获取，这里暂时使用默认值）
    private var currentUserId: String = "1"

    init {
        loadConversations()
    }

    /**
     * 设置当前用户ID
     */
    fun setUserId(userId: String) {
        currentUserId = userId
        loadConversations()
    }

    /**
     * 从索引文件加载对话列表
     */
    fun loadConversations() {
        viewModelScope.launch {
            val currentState = _uiStateFlow.value
            updateUiState(currentState.copy(isLoading = true, error = null))

            try {
                val conversations = historyModel.getAllConversations(currentUserId)
                updateUiState(currentState.copy(
                    conversations = conversations,
                    isLoading = false
                ))
            } catch (e: Exception) {
                updateUiState(currentState.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                ))
            }
        }
    }

    /**
     * 更新搜索关键词
     */
    fun updateSearchQuery(query: String) {
        val currentState = _uiStateFlow.value
        updateUiState(currentState.copy(searchQuery = query))
    }

    /**
     * 选择对话
     */
    fun selectConversation(conversationId: String) {
        val currentState = _uiStateFlow.value
        updateUiState(currentState.copy(selectedConversationId = conversationId))
    }

    /**
     * 删除对话
     */
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                historyModel.deleteConversation(conversationId)
                // 重新加载列表
                loadConversations()
            } catch (e: Exception) {
                val currentState = _uiStateFlow.value
                updateUiState(currentState.copy(error = e.message ?: "删除失败"))
            }
        }
    }

    /**
     * 重命名对话
     */
    fun renameConversation(conversationId: String, newTitle: String) {
        viewModelScope.launch {
            try {
                historyModel.updateConversationTitle(conversationId, newTitle)
                // 重新加载列表
                loadConversations()
            } catch (e: Exception) {
                val currentState = _uiStateFlow.value
                updateUiState(currentState.copy(error = e.message ?: "重命名失败"))
            }
        }
    }

    /**
     * 切换置顶状态
     */
    fun togglePinConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                // 获取当前状态
                val currentState = _uiStateFlow.value
                val conversation = currentState.conversations.find { it.id == conversationId }
                val newPinnedState = !(conversation?.isPinned ?: false)

                historyModel.togglePinConversation(conversationId, newPinnedState)
                // 重新加载列表
                loadConversations()
            } catch (e: Exception) {
                val currentState = _uiStateFlow.value
                updateUiState(currentState.copy(error = e.message ?: "置顶操作失败"))
            }
        }
    }

    /**
     * 创建新对话
     */
    fun createNewConversation(initialTitle: String = "新对话"): String? {
        return try {
            val newIndex = historyModel.createConversation(currentUserId, initialTitle)
            // 重新加载列表
            loadConversations()
            newIndex.conversationId
        } catch (e: Exception) {
            val currentState = _uiStateFlow.value
            updateUiState(currentState.copy(error = e.message ?: "创建对话失败"))
            null
        }
    }
}
