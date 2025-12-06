package com.example.feishuqa.app.main

import HistoryRepository
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.feishuqa.R
import com.example.feishuqa.adapter.HistoryConversationAdapter
import com.example.feishuqa.app.keyboard.ChatInputView
import com.example.feishuqa.app.login.LoginActivity
import com.example.feishuqa.data.entity.AIModel
import com.example.feishuqa.databinding.ActivityMainBinding
import com.example.feishuqa.databinding.LayoutDrawerBinding
import com.example.feishuqa.databinding.LayoutInputBarBinding
import kotlinx.coroutines.launch
import kotlin.math.log

/**
 * 主界面View层
 * View层：负责UI展示和用户交互，不包含业务逻辑
 */
class MainView(
    private val context: Context,
    private val binding: ActivityMainBinding,
    private val drawerBinding: LayoutDrawerBinding,
    private val chatInputView: ChatInputView,
    private val viewModel: MainViewModel,
    private val lifecycleOwner: LifecycleOwner
) {

    private lateinit var historyAdapter: HistoryConversationAdapter



    /**
     * 初始化View
     */
    fun init() {
        val model = HistoryRepository(context)
//        val resultList1 = model.createConversation2(context, "user_A", "222")
//        Log.d("test", "init: $resultList1")


        val resultList2 = model.getAllConversations("test_user_01")
        Log.d("test", "init: $resultList2")

//        var conversationDetail = model.getConversationDetail("conversation_1764992458418")
//        Log.d("test", "init: $conversationDetail")

        //model.updateConversationTitle("conversation_1764992458418", "444")
//
//        val USER_ID = "test_user_01"
//
//        Log.d("test1","--- 步骤 1: 创建新会话 ---")
//        // 创建会话，获取 ID
//        val newConv = model.createConversation2(context,USER_ID, "测试消息分页")
//        val convId = newConv.conversationId
//        Log.d("test1","新会话ID: $convId")
//
//        Log.d("test1","\n--- 步骤 2: 写入测试消息 (5条) ---")
        // 调用 addMessage 5 次，确保消息文件 messages/conv_xxx/messages_001.json 被创建
//        for (i in 1..5) {
//            model.addMessage(convId, "用户消息 $i", isUser = true)
//            model.addMessage(convId, "AI回复 $i", isUser = false)
//        }
//        for (i in 1..101) {
//            model.addMessage(convId, "第 $i 条测试消息", isUser = true)
//        }
//        Log.d("test1","已写入 10 条消息到磁盘...")
//
//        Log.d("test1","\n--- 步骤 3: 调用待验证函数 (getAllMessages) ---")
//        // 调用要测试的读取函数
//        val messageList = model.getAllMessages(convId)
//
//        // --- 步骤 4: 验证结果 ---
//        Log.d("test1","--- 步骤 4: 验证结果 ---")
//
//        if (messageList.size == 10) {
//            Log.d("test1","✅ 验证成功: 列表长度正确 (10条消息)。")
//        } else {
//            Log.d("test1","❌ 验证失败: 列表长度错误 (实际: ${messageList.size})。")
//        }
//
//        // 验证排序：第一条消息的 Order 应该是 1
//        Log.d("test1","第一条消息内容: ${messageList.firstOrNull()?.content} | Order: ${messageList.firstOrNull()?.messageOrder}")
//

        setupTopBar()
        setupDrawer()
        //setupInputBar() 已经被chatinputview内部接管
        observeViewModel()
        startLogoAnimation()
    }

    /**
     * 设置顶部导航栏
     */
    private fun setupTopBar() {
        // 登录按钮
        binding.btnLogin.setOnClickListener {
            val intent = android.content.Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }

        // 新建对话按钮
        binding.btnNewChat.setOnClickListener {
            viewModel.createNewConversation("新对话")
        }

        // 菜单按钮（打开侧边栏）
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    /**
     * 设置侧边栏
     */
    private fun setupDrawer() {
        // 新建对话按钮
        drawerBinding.btnNewConversation.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            viewModel.createNewConversation("新对话")
        }

        // 知识库入口
        drawerBinding.layoutKnowledgeBase.setOnClickListener {
            Toast.makeText(context, "打开知识库", Toast.LENGTH_SHORT).show()
        }

        // 设置历史对话列表
        setupHistoryList()
    }

    /**
     * 设置历史对话列表
     */
    private fun setupHistoryList() {
        historyAdapter = HistoryConversationAdapter { conversation ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            viewModel.selectConversation(conversation.id)
        }

        drawerBinding.rvHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }



    /**
     * 观察ViewModel状态变化
     */
    private fun observeViewModel() {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                // 观察UI状态
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }

                // 观察导航事件
                viewModel.navigateToConversation.collect { conversationId ->
                    conversationId?.let {
                        Toast.makeText(context, "打开对话: $it", Toast.LENGTH_SHORT).show()
                        viewModel.clearNavigation()
                    }
                }
            }
        }
    }

    /**
     * 更新UI
     */
    private fun updateUI(state: MainUiState) {
        // 更新对话列表
        historyAdapter.submitList(state.conversations)
        if (state.conversations.isEmpty()) {
            drawerBinding.rvHistory.visibility = View.GONE
            drawerBinding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            drawerBinding.rvHistory.visibility = View.VISIBLE
            drawerBinding.layoutEmptyState.visibility = View.GONE
        }

        // 更新 ChatInputView 上的状态显示 (模型名称、联网高亮)
        val tvSelectedModel = chatInputView.findViewById<android.widget.TextView>(R.id.tv_selected_model)
        val btnWebSearch = chatInputView.findViewById<android.view.View>(R.id.btn_web_search)
        val tvWebSearch = chatInputView.findViewById<android.widget.TextView>(R.id.tv_web_search)
        val icWebSearch = chatInputView.findViewById<android.widget.ImageView>(R.id.ic_web_search)

        // 安全判空，防止ID改了导致崩溃
        tvSelectedModel?.text = state.selectedModel.name

        if (state.isWebSearchEnabled) {
            btnWebSearch?.setBackgroundResource(R.drawable.bg_chip_selected)
            tvWebSearch?.setTextColor(context.getColor(R.color.feishu_blue))
            icWebSearch?.setColorFilter(context.getColor(R.color.feishu_blue))
        } else {
            btnWebSearch?.setBackgroundResource(R.drawable.bg_chip_normal)
            tvWebSearch?.setTextColor(context.getColor(R.color.text_secondary))
            icWebSearch?.setColorFilter(context.getColor(R.color.text_secondary))
        }

        // 显示错误
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    /**
     * 显示模型选择对话框
     */
    fun showModelSelectorDialog() {
        val dialogView = android.view.LayoutInflater.from(context)
            .inflate(R.layout.dialog_model_selector, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.rg_models)
        val currentModel = viewModel.uiState.value.selectedModel

        // 设置当前选中的模型
        when (currentModel.id) {
            "deepseek-r1" -> radioGroup.check(R.id.rb_deepseek)
            "gpt-4" -> radioGroup.check(R.id.rb_gpt4)
            "claude-3.5" -> radioGroup.check(R.id.rb_claude)
            "qwen" -> radioGroup.check(R.id.rb_qwen)
            "doubao" -> radioGroup.check(R.id.rb_doubao)
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        // 监听选择变化
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val model = when (checkedId) {
                R.id.rb_deepseek -> viewModel.getAvailableModels().find { it.id == "deepseek-r1" }
                R.id.rb_gpt4 -> viewModel.getAvailableModels().find { it.id == "gpt-4" }
                R.id.rb_claude -> viewModel.getAvailableModels().find { it.id == "claude-3.5" }
                R.id.rb_qwen -> viewModel.getAvailableModels().find { it.id == "qwen" }
                R.id.rb_doubao -> viewModel.getAvailableModels().find { it.id == "doubao" }
                else -> null
            }
            model?.let {
                viewModel.selectModel(it)
            }
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    /**
     * 启动Logo旋转动画
     */
    private fun startLogoAnimation() {
        val rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 8000
            repeatCount = Animation.INFINITE
            repeatMode = Animation.RESTART
        }
        binding.logoImage.startAnimation(rotateAnimation)
    }
}

