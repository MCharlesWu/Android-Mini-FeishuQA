package com.example.feishuqa.data.repository

import android.content.Context
import com.example.feishuqa.app.history.HistoryModel
import com.example.feishuqa.data.entity.AIModel
import com.example.feishuqa.data.entity.AIModels
import com.example.feishuqa.data.entity.Conversation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 主界面数据仓库
 * Model层：负责主界面相关数据的获取和处理
 * 
 * 现在使用 HistoryModel 来获取历史对话，统一使用新的索引文件结构
 */
class MainRepository(private val context: Context) {

    // 使用我们完成的 HistoryModel
    private val historyModel = HistoryModel(context)

    // 未登录用户的标识
    companion object {
        const val GUEST_USER_ID = "guest"
    }

    // 当前登录用户ID（未登录时使用 GUEST_USER_ID）
    private var currentUserId: String = GUEST_USER_ID

    /**
     * 设置当前用户ID
     */
    fun setUserId(userId: String) {
        currentUserId = userId
    }

    /**
     * 获取历史对话列表
     * 现在使用 HistoryModel 来获取，统一使用新的索引文件结构
     */
    suspend fun getConversations(): List<Conversation> {
        return withContext(Dispatchers.IO) {
            try {
                // 使用 HistoryModel 获取对话列表
                historyModel.getAllConversations(currentUserId)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    /**
     * 创建新对话
     * 现在使用 HistoryModel 来创建，统一使用新的索引文件结构
     */
    suspend fun createConversation(title: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 使用 HistoryModel 创建对话
                val conversationIndex = historyModel.createConversation(currentUserId, title)
                Result.success(conversationIndex.conversationId)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    /**
     * 获取可用的AI模型列表
     */
    fun getAvailableModels(): List<AIModel> {
        return AIModels.defaultModels
    }

    /**
     * 获取默认模型
     */
    fun getDefaultModel(): AIModel {
        return AIModels.defaultModel
    }

    /**
     * 删除对话
     */
    suspend fun deleteConversation(conversationId: String) {
        withContext(Dispatchers.IO) {
            historyModel.deleteConversation(conversationId)
        }
    }

    /**
     * 重命名对话
     */
    suspend fun renameConversation(conversationId: String, newTitle: String) {
        withContext(Dispatchers.IO) {
            historyModel.updateConversationTitle(conversationId, newTitle)
        }
    }

    /**
     * 切换置顶状态
     */
    suspend fun togglePinConversation(conversationId: String, isPinned: Boolean) {
        withContext(Dispatchers.IO) {
            historyModel.togglePinConversation(conversationId, isPinned)
        }
    }

    /**
     * 删除指定用户的所有对话（用于清理未登录用户的临时数据）
     */
    suspend fun deleteConversationsByUserId(userId: String) {
        withContext(Dispatchers.IO) {
            historyModel.deleteConversationsByUserId(userId)
        }
    }
}





