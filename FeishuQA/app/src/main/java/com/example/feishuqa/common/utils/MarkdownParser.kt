package com.example.feishuqa.common.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.regex.Pattern

/**
 * Markdown解析器
 * 支持常见的Markdown格式：标题、代码块、表格、粗体、斜体、链接等
 */
object MarkdownParser {
    
    private val CODE_BLOCK_PATTERN = Pattern.compile("```([\\s\\S]*?)```")
    private val INLINE_CODE_PATTERN = Pattern.compile("`([^`]+)`")
    private val BOLD_PATTERN = Pattern.compile("\\*\\*([^*]+)\\*\\*")
    private val ITALIC_PATTERN = Pattern.compile("\\*([^*]+)\\*")
    private val HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s*(.+)$", Pattern.MULTILINE)
    private val LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)")
    private val TABLE_PATTERN = Pattern.compile("\\|(.+)\\|")
    private val HIGHLIGHT_PATTERN = Pattern.compile("==([^=]+)==")
    
    /**
     * 解析Markdown文本为AnnotatedString
     */
    fun parseMarkdown(text: String): AnnotatedString {
        return buildAnnotatedString {
            // 处理代码块
            val codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(text)
            val codeBlocks = mutableListOf<Pair<IntRange, String>>()
            while (codeBlockMatcher.find()) {
                codeBlocks.add(codeBlockMatcher.start()..codeBlockMatcher.end() to codeBlockMatcher.group(1))
            }
            
            // 处理行内代码
            val inlineCodeMatcher = INLINE_CODE_PATTERN.matcher(text)
            val inlineCodes = mutableListOf<Pair<IntRange, String>>()
            while (inlineCodeMatcher.find()) {
                inlineCodes.add(inlineCodeMatcher.start()..inlineCodeMatcher.end() to inlineCodeMatcher.group(1))
            }
            
            // 处理粗体
            val boldMatcher = BOLD_PATTERN.matcher(text)
            val boldTexts = mutableListOf<Pair<IntRange, String>>()
            while (boldMatcher.find()) {
                boldTexts.add(boldMatcher.start()..boldMatcher.end() to boldMatcher.group(1))
            }
            
            // 处理斜体
            val italicMatcher = ITALIC_PATTERN.matcher(text)
            val italicTexts = mutableListOf<Pair<IntRange, String>>()
            while (italicMatcher.find()) {
                italicTexts.add(italicMatcher.start()..italicMatcher.end() to italicMatcher.group(1))
            }
            
            // 处理链接
            val linkMatcher = LINK_PATTERN.matcher(text)
            val links = mutableListOf<Triple<IntRange, String, String>>()
            while (linkMatcher.find()) {
                links.add(Triple(linkMatcher.start()..linkMatcher.end(), linkMatcher.group(1), linkMatcher.group(2)))
            }
            
            // 处理高亮
            val highlightMatcher = HIGHLIGHT_PATTERN.matcher(text)
            val highlights = mutableListOf<Pair<IntRange, String>>()
            while (highlightMatcher.find()) {
                highlights.add(highlightMatcher.start()..highlightMatcher.end() to highlightMatcher.group(1))
            }
            
            append(text)
            
            // 应用样式
            codeBlocks.forEach { (range, _) ->
                addStyle(
                    style = SpanStyle(
                        background = Color(0xFFF5F5F5),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    start = range.first,
                    end = range.last
                )
            }
            
            inlineCodes.forEach { (range, _) ->
                addStyle(
                    style = SpanStyle(
                        background = Color(0xFFF5F5F5),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color(0xFFD32F2F)
                    ),
                    start = range.first,
                    end = range.last
                )
            }
            
            boldTexts.forEach { (range, _) ->
                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                    start = range.first,
                    end = range.last
                )
            }
            
            italicTexts.forEach { (range, _) ->
                addStyle(
                    style = SpanStyle(fontStyle = FontStyle.Italic),
                    start = range.first,
                    end = range.last
                )
            }
            
            links.forEach { (range, text, url) ->
                addStyle(
                    style = SpanStyle(
                        color = Color(0xFF1976D2),
                        textDecoration = TextDecoration.Underline
                    ),
                    start = range.first,
                    end = range.last
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = url,
                    start = range.first,
                    end = range.last
                )
            }
            
            highlights.forEach { (range, _) ->
                addStyle(
                    style = SpanStyle(
                        background = Color(0xFFFFFF00),
                        color = Color.Black
                    ),
                    start = range.first,
                    end = range.last
                )
            }
        }
    }
    
    /**
     * 检查文本是否包含Markdown格式
     */
    fun containsMarkdown(text: String): Boolean {
        return CODE_BLOCK_PATTERN.matcher(text).find() ||
               INLINE_CODE_PATTERN.matcher(text).find() ||
               BOLD_PATTERN.matcher(text).find() ||
               ITALIC_PATTERN.matcher(text).find() ||
               HEADER_PATTERN.matcher(text).find() ||
               LINK_PATTERN.matcher(text).find() ||
               HIGHLIGHT_PATTERN.matcher(text).find()
    }
    
    /**
     * 解析标题级别
     */
    fun parseHeaderLevel(line: String): Int {
        val matcher = HEADER_PATTERN.matcher(line)
        return if (matcher.find()) {
            matcher.group(1).length
        } else {
            0
        }
    }
    
    /**
     * 移除Markdown标记，获取纯文本
     */
    fun stripMarkdown(text: String): String {
        return text.replace(CODE_BLOCK_PATTERN.toRegex(), "")
            .replace(INLINE_CODE_PATTERN.toRegex(), "$1")
            .replace(BOLD_PATTERN.toRegex(), "$1")
            .replace(ITALIC_PATTERN.toRegex(), "$1")
            .replace(LINK_PATTERN.toRegex(), "$1")
            .replace(HIGHLIGHT_PATTERN.toRegex(), "$1")
    }
}