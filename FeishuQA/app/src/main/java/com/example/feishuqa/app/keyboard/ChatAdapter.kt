package com.example.feishuqa.app.keyboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.feishuqa.R
import com.example.feishuqa.data.entity.Message
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.ext.tables.TablePlugin
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.isNotEmpty

class ChatAdapter(private val context: Context, private val onImageClick: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<Message>()
    private val markwon: Markwon
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    companion object {
        const val TYPE_USER = 1
        const val TYPE_AI = 2
    }

    init {
        // 初始化Markwon，支持基础Markdown功能和表格
        markwon = Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .build()
    }

    fun submitList(list: List<Message>) {
        messages.clear()
        messages.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser()) TYPE_USER else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutId = if (viewType == TYPE_USER) R.layout.item_msg_user else R.layout.item_msg_ai
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ChatViewHolder).bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContent: TextView = itemView.findViewById(R.id.tvMessageContent)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivMessageImage)
        private val tvTime: TextView? = itemView.findViewById(R.id.tvMessageTime)

        fun bind(msg: Message) {
            // 设置文字 - 使用Markwon解析Markdown
            if (msg.content.isNotEmpty()) {
                tvContent.visibility = View.VISIBLE
                markwon.setMarkdown(tvContent, msg.content)
            } else {
                tvContent.visibility = View.GONE
            }

            // 设置图片
            if (msg.type == com.example.feishuqa.data.entity.MessageType.IMAGE && msg.extraInfo != null) {
                ivImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(msg.extraInfo)
                    .override(800, 800) // 增大显示尺寸
                    .fitCenter()
                    .into(ivImage)

                // 点击图片回调
                ivImage.setOnClickListener { onImageClick(msg.extraInfo) }
            } else {
                ivImage.visibility = View.GONE
            }

            // 设置时间戳
            tvTime?.let {
                it.visibility = View.VISIBLE
                it.text = timeFormat.format(Date(msg.timestamp))
            }
        }
    }
}