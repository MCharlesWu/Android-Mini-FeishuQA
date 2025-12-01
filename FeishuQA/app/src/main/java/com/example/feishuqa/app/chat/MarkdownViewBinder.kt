package com.example.feishuqa.app.chat

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.feishuqa.R
import com.example.feishuqa.common.utils.MarkdownNode
import com.example.feishuqa.common.utils.MarkdownParser
import com.example.feishuqa.common.utils.SpanType

/**
 * 负责将 Markdown 节点渲染为 Android View
 * 核心逻辑已完全委托给 MarkdownParser，此处只负责 UI 构建
 */
object MarkdownViewBinder {

    fun bind(context: Context, container: LinearLayout, content: String) {
        container.removeAllViews()
        
        // 1. 调用通用解析器获取 Block 结构
        val nodes = MarkdownParser.parse(content)

        // 2. 映射 Block 到 View
        for (node in nodes) {
            val view = when (node) {
                is MarkdownNode.Heading -> createHeading(context, node)
                is MarkdownNode.Paragraph -> createParagraph(context, node)
                is MarkdownNode.CodeBlock -> createCodeBlock(context, node)
                is MarkdownNode.Table -> createTable(context, node)
                is MarkdownNode.UnorderedList -> createUnorderedList(context, node)
                is MarkdownNode.OrderedList -> createOrderedList(context, node)
                is MarkdownNode.BlockQuote -> createBlockQuote(context, node)
                is MarkdownNode.HorizontalRule -> createDivider(context)
            }
            container.addView(view)
        }
    }

    // --- Block View Creators ---

    private fun createHeading(context: Context, node: MarkdownNode.Heading): View {
        return TextView(context).apply {
            text = node.text
            setTextColor(Color.parseColor("#1F2329"))
            textSize = when (node.level) {
                1 -> 24f
                2 -> 20f
                else -> 16f
            }
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 16, 0, 8)
        }
    }

    private fun createParagraph(context: Context, node: MarkdownNode.Paragraph): View {
        return TextView(context).apply {
            // 调用 applyInlineStyles 处理行内样式
            text = applyInlineStyles(node.text)
            setTextColor(Color.parseColor("#1F2329"))
            textSize = 16f
            setLineSpacing(12f, 1.2f)
            setPadding(0, 0, 0, 12)
        }
    }

    private fun createCodeBlock(context: Context, node: MarkdownNode.CodeBlock): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(context, R.drawable.bg_rounded_gray)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16; bottomMargin = 16 }
        }

        val header = TextView(context).apply {
            text = node.language.ifEmpty { "Text" }
            textSize = 12f
            setTextColor(Color.GRAY)
            setTypeface(null, Typeface.BOLD)
            setPadding(24, 12, 24, 12)
            background = ContextCompat.getDrawable(context, R.drawable.bg_code_header)
        }
        container.addView(header)

        val codeView = TextView(context).apply {
            text = node.code
            textSize = 13f
            setTextColor(Color.parseColor("#24292E"))
            typeface = Typeface.MONOSPACE
            setPadding(24, 16, 24, 16)
        }
        container.addView(codeView)
        return container
    }

    private fun createTable(context: Context, node: MarkdownNode.Table): View {
        val scroll = HorizontalScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16; bottomMargin = 16 }
        }
        val table = TableLayout(context).apply {
            background = ContextCompat.getDrawable(context, R.drawable.bg_rounded_gray)
        }
        // Header
        val headerRow = TableRow(context).apply {
            setBackgroundColor(Color.parseColor("#F5F6F7"))
            setPadding(0, 8, 0, 8)
        }
        node.headers.forEach { h -> headerRow.addView(createTableCell(context, h, true)) }
        table.addView(headerRow)
        // Rows
        node.rows.forEachIndexed { index, row ->
            val tr = TableRow(context).apply {
                if (index % 2 == 1) setBackgroundColor(Color.parseColor("#FAFAFA"))
            }
            row.forEach { cell -> tr.addView(createTableCell(context, cell, false)) }
            table.addView(tr)
        }
        scroll.addView(table)
        return scroll
    }

    private fun createTableCell(context: Context, text: String, isHeader: Boolean): View {
        return TextView(context).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.parseColor("#1F2329"))
            if (isHeader) setTypeface(null, Typeface.BOLD)
            setPadding(24, 12, 24, 12)
        }
    }

    private fun createUnorderedList(context: Context, node: MarkdownNode.UnorderedList): View {
        val container = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        node.items.forEach { item ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 4, 0, 4)
            }
            val dot = TextView(context).apply {
                text = "•"
                textSize = 18f
                setTextColor(Color.parseColor("#3370FF"))
                setPadding(0, 0, 16, 0)
            }
            val content = TextView(context).apply {
                text = applyInlineStyles(item) // 支持列表项内的富文本
                setTextColor(Color.parseColor("#1F2329"))
                textSize = 16f
            }
            row.addView(dot)
            row.addView(content)
            container.addView(row)
        }
        return container
    }

    private fun createOrderedList(context: Context, node: MarkdownNode.OrderedList): View {
        val container = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        node.items.forEachIndexed { index, item ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 4, 0, 4)
            }
            val num = TextView(context).apply {
                text = "${index + 1}."
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#1F2329"))
                setPadding(0, 0, 16, 0)
            }
            val content = TextView(context).apply {
                text = applyInlineStyles(item)
                setTextColor(Color.parseColor("#1F2329"))
                textSize = 16f
            }
            row.addView(num)
            row.addView(content)
            container.addView(row)
        }
        return container
    }

    private fun createBlockQuote(context: Context, node: MarkdownNode.BlockQuote): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 8)
        }
        val bar = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(10, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor("#DEE0E3"))
        }
        val content = TextView(context).apply {
            text = node.content
            setTextColor(Color.GRAY)
            setTypeface(null, Typeface.ITALIC)
            setPadding(24, 0, 0, 0)
        }
        container.addView(bar)
        container.addView(content)
        return container
    }

    private fun createDivider(context: Context): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2).apply {
                topMargin = 24
                bottomMargin = 24
            }
            setBackgroundColor(Color.parseColor("#DEE0E3"))
        }
    }

    // --- Inline Style Application ---
    
    /**
     * 将 Parser 解析出的行内样式应用到 SpannableString 上
     * 这里的逻辑纯粹是 UI 映射：SpanType -> Android Span Object
     */
    private fun applyInlineStyles(text: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)
        
        // 调用解析器 (逻辑复用!)
        val spans = MarkdownParser.parseInline(text)

        for (span in spans) {
            when (span.type) {
                SpanType.BOLD -> {
                    // 样式：粗体
                    builder.setSpan(StyleSpan(Typeface.BOLD), span.start, span.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    // 技巧：把 "**" 设为透明，模拟"去除"标记符的效果，保持索引对齐
                    hideMark(builder, span.start, span.start + 2)
                    hideMark(builder, span.end - 2, span.end)
                }
                SpanType.CODE -> {
                    // 样式：等宽 + 灰底 + 粉字
                    builder.setSpan(TypefaceSpan("monospace"), span.start, span.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(BackgroundColorSpan(Color.parseColor("#F2F3F5")), span.start, span.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(ForegroundColorSpan(Color.parseColor("#E83E8C")), span.start, span.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    // 隐藏 "`"
                    hideMark(builder, span.start, span.start + 1)
                    hideMark(builder, span.end - 1, span.end)
                }
                SpanType.STRIKETHROUGH -> {
                    // 样式：删除线
                    builder.setSpan(StrikethroughSpan(), span.start, span.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    // 隐藏 "~~"
                    hideMark(builder, span.start, span.start + 2)
                    hideMark(builder, span.end - 2, span.end)
                }
                else -> {}
            }
        }
        return builder
    }
    
    // 辅助方法：隐藏 Markdown 标记符 (变透明 + 极小字号)
    private fun hideMark(builder: SpannableStringBuilder, start: Int, end: Int) {
        if (start < end) {
            builder.setSpan(ForegroundColorSpan(Color.TRANSPARENT), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(RelativeSizeSpan(0.1f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}