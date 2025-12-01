package com.example.feishuqa.common.utils

/**
 * Markdown 块级节点 (Block)
 */
sealed class MarkdownNode {
    data class Heading(val level: Int, val text: String) : MarkdownNode()
    data class Paragraph(val text: String) : MarkdownNode()
    data class CodeBlock(val language: String, val code: String) : MarkdownNode()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownNode()
    data class UnorderedList(val items: List<String>) : MarkdownNode()
    data class OrderedList(val items: List<String>) : MarkdownNode()
    data class BlockQuote(val content: String) : MarkdownNode()
    object HorizontalRule : MarkdownNode()
}

/**
 * Markdown 行内样式数据 (Inline)
 */
data class InlineSpan(
    val type: SpanType,
    val start: Int,
    val end: Int,
    val extra: String? = null
)

enum class SpanType {
    BOLD, CODE, STRIKETHROUGH, LINK
}

/**
 * 完整版 Markdown 解析器
 */
object MarkdownParser {

    // --- Block 解析 ---
    fun parse(text: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        val lines = text.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            when {
                // 1. 代码块 (```)
                trimmed.startsWith("```") -> {
                    val language = trimmed.removePrefix("```").trim()
                    val codeContent = StringBuilder()
                    i++ // 跳过开始行
                    
                    while (i < lines.size) {
                        val currentLineTrimmed = lines[i].trim()
                        // 修复：只有纯粹的 ``` (可能带尾部空格) 才被视为结束符
                        // 且长度至少为3 (处理 `````` 的情况)
                        if (currentLineTrimmed.startsWith("```") && 
                            currentLineTrimmed.all { it == '`' }) {
                            break // 遇到结束标记，退出循环
                        }
                        
                        codeContent.append(lines[i]).append("\n")
                        i++
                    }
                    
                    // 移除最后一个多余的换行符
                    val code = if (codeContent.isNotEmpty()) codeContent.substring(0, codeContent.length - 1) else ""
                    nodes.add(MarkdownNode.CodeBlock(language, code))
                    i++ // 跳过结束行
                }

                // 2. 表格 (| Header |)
                trimmed.startsWith("|") -> {
                    val headers = parseTableRow(trimmed)
                    val rows = mutableListOf<List<String>>()
                    i++
                    // 跳过分隔行 (|---|)
                    if (i < lines.size && lines[i].trim().matches(Regex("\\|?\\s*:?-+:?\\s*(\\|\\s*:?-+:?\\s*)*\\|?"))) {
                        i++
                    }
                    // 解析数据行
                    while (i < lines.size && lines[i].trim().startsWith("|")) {
                        rows.add(parseTableRow(lines[i]))
                        i++
                    }
                    nodes.add(MarkdownNode.Table(headers, rows))
                }

                // 3. 标题 (# Heading)
                trimmed.startsWith("#") -> {
                    val level = trimmed.takeWhile { it == '#' }.length
                    if (level in 1..6) {
                        nodes.add(MarkdownNode.Heading(level, trimmed.substring(level).trim()))
                    } else {
                        nodes.add(MarkdownNode.Paragraph(line))
                    }
                    i++
                }

                // 4. 无序列表 (- Item, * Item)
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val items = mutableListOf<String>()
                    while (i < lines.size && (lines[i].trim().startsWith("- ") || lines[i].trim().startsWith("* "))) {
                        items.add(lines[i].trim().substring(2))
                        i++
                    }
                    nodes.add(MarkdownNode.UnorderedList(items))
                }

                // 5. 有序列表 (1. Item)
                trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val items = mutableListOf<String>()
                    val regex = Regex("^\\d+\\.\\s+(.*)")
                    while (i < lines.size && lines[i].trim().matches(Regex("^\\d+\\.\\s+.*"))) {
                        val match = regex.find(lines[i].trim())
                        if (match != null) items.add(match.groupValues[1])
                        i++
                    }
                    nodes.add(MarkdownNode.OrderedList(items))
                }
                
                // 6. 引用块 (> Quote)
                trimmed.startsWith("> ") -> {
                    val content = StringBuilder()
                    while (i < lines.size && lines[i].trim().startsWith("> ")) {
                        content.append(lines[i].trim().substring(2)).append("\n")
                        i++
                    }
                    nodes.add(MarkdownNode.BlockQuote(content.toString().trim()))
                }

                // 7. 分隔线 (--- 或 ***)
                trimmed == "---" || trimmed == "***" -> {
                    nodes.add(MarkdownNode.HorizontalRule)
                    i++
                }

                // 8. 普通段落
                else -> {
                    if (trimmed.isNotEmpty()) nodes.add(MarkdownNode.Paragraph(line))
                    i++
                }
            }
        }
        return nodes
    }

    private fun parseTableRow(row: String): List<String> = 
        row.split("|").map { it.trim() }.filter { it.isNotEmpty() }

    // --- Inline 解析 ---
    fun parseInline(text: String): List<InlineSpan> {
        val spans = mutableListOf<InlineSpan>()

        // 1. 粗体 **text**
        Regex("\\*\\*(.*?)\\*\\*").findAll(text).forEach { match ->
            spans.add(InlineSpan(SpanType.BOLD, match.range.first, match.range.last + 1))
        }

        // 2. 行内代码 `text`
        Regex("`(.*?)`").findAll(text).forEach { match ->
            spans.add(InlineSpan(SpanType.CODE, match.range.first, match.range.last + 1))
        }

        // 3. 删除线 ~~text~~
        Regex("~~(.*?)~~").findAll(text).forEach { match ->
            spans.add(InlineSpan(SpanType.STRIKETHROUGH, match.range.first, match.range.last + 1))
        }

        return spans
    }
}