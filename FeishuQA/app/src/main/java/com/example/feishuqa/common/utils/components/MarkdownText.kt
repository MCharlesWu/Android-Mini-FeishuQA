package com.example.feishuqa.common.utils.components

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.example.feishuqa.common.utils.MarkdownParser

/**
 * Markdown文本组件
 * 支持Markdown格式的文本显示
 */
@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    if (content.isBlank()) {
        return
    }
    
    // 使用MarkdownParser解析内容
    val annotatedString = remember(content) {
        MarkdownParser.parseMarkdown(content)
    }
    
    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * 简化的Markdown文本显示（用于消息项）
 * 在MessageItem中使用的简化版本
 */
@Composable
fun MarkdownTextMock(
    content: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default
) {
    if (content.isBlank()) {
        return
    }
    
    // 检查是否包含Markdown格式
    val hasMarkdown = remember(content) {
        MarkdownParser.containsMarkdown(content)
    }
    
    if (hasMarkdown) {
        // 使用完整的Markdown解析
        MarkdownText(
            content = content,
            modifier = modifier,
            style = style
        )
    } else {
        // 普通文本显示
        Text(
            text = content,
            modifier = modifier,
            style = style
        )
    }
}