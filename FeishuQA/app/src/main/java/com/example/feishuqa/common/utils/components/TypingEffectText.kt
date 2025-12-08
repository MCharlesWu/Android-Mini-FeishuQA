package com.example.feishuqa.common.utils.components

import androidx.compose.runtime.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 打字机效果文本组件
 * 逐字显示文本，模拟打字效果
 */
@Composable
fun TypingEffectText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    typingSpeed: Long = 50L, // 每个字符的延迟时间（毫秒）
    onTypingComplete: (() -> Unit)? = null
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(text) {
        // 重置状态
        displayedText = ""
        currentIndex = 0
        
        // 逐字显示
        while (currentIndex < text.length) {
            delay(typingSpeed)
            displayedText = text.substring(0, currentIndex + 1)
            currentIndex++
        }
        
        // 打字完成回调
        onTypingComplete?.invoke()
    }
    
    Text(
        text = displayedText,
        modifier = modifier,
        style = style
    )
}

/**
 * 打字机效果的Markdown文本组件
 * 支持Markdown格式的打字机效果
 */
@Composable
fun TypingMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    typingSpeed: Long = 50L,
    onTypingComplete: (() -> Unit)? = null
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(text) {
        // 重置状态
        displayedText = ""
        currentIndex = 0
        
        // 逐字显示
        while (currentIndex < text.length) {
            delay(typingSpeed)
            displayedText = text.substring(0, currentIndex + 1)
            currentIndex++
        }
        
        // 打字完成回调
        onTypingComplete?.invoke()
    }
    
    // 使用MarkdownText显示已显示的部分
    MarkdownText(
        content = displayedText,
        modifier = modifier,
        style = style
    )
}