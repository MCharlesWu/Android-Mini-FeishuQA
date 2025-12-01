package com.example.feishuqa

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.feishuqa.app.chat.ChatAdapter
import com.example.feishuqa.app.chat.ChatViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter
    
    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnVoice: ImageView
    private lateinit var layoutWelcome: LinearLayout
    private lateinit var btnBack: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initViews()
        initListeners()
        observeData()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        etInput = findViewById(R.id.etInput)
        btnSend = findViewById(R.id.btnSend)
        btnVoice = findViewById(R.id.btnVoice)
        layoutWelcome = findViewById(R.id.layoutWelcome)
        btnBack = findViewById(R.id.btnBack)

        // Setup RecyclerView
        adapter = ChatAdapter()
        val layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true // 列表倒序，从底部开始
            stackFromEnd = true
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    private fun initListeners() {
        // Back
        btnBack.setOnClickListener { finish() }

        // Input Change -> Toggle Send/Voice Button
        etInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasText = !s.isNullOrBlank()
                btnSend.isVisible = hasText
                btnVoice.isVisible = !hasText
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Send
        btnSend.setOnClickListener {
            val content = etInput.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.sendTextMessage(content)
                etInput.setText("")
            }
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // 1. 更新列表
                    adapter.submitList(state.messages) {
                        // 如果是新消息(正在输入中)，滚动到底部(即顶部，因为reverseLayout)
                        if (state.isTyping && state.messages.isNotEmpty()) {
                            recyclerView.scrollToPosition(0)
                        }
                    }

                    // 2. 空状态页
                    layoutWelcome.isVisible = state.messages.isEmpty()
                    recyclerView.isVisible = state.messages.isNotEmpty()
                }
            }
        }
    }
}