package com.example.feishuqa.data.entity

import com.google.gson.annotations.SerializedName

// AI回复响应对象
data class ChatResponse
(
    val id: String?,
    val model: String?,
    val choices: List<Choice>?
)

// 对应 choices 数组里的每一项
data class Choice
(
    val index: Int?,
    // 对应 JSON 中的 "message": {...}
    val message: AiMessage?,

    // 常用字段，代表为什么停止生成 (如 "stop" 代表正常结束)
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class AiMessage
(
    val role: String,   // "user" 或 "assistant"
    val content: String // 对话的具体内容
)

// 调用API的请求类
data class ChatRequest
(
    val model: String = "deepseek-chat",
    val messages: List<AiMessage>,
    val stream: Boolean = false
)