package com.example.feishuqa.app.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.feishuqa.R
import com.example.feishuqa.data.entity.Message

class ChatAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_AI = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser()) TYPE_USER else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            UserViewHolder(inflater.inflate(R.layout.item_message_user, parent, false))
        } else {
            AiViewHolder(inflater.inflate(R.layout.item_message_ai, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        if (holder is UserViewHolder) holder.bind(msg)
        else if (holder is AiViewHolder) holder.bind(msg)
    }

    // 更新：支持 Markdown 渲染的用户消息
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val markdownContainer: LinearLayout = itemView.findViewById(R.id.markdownContainer)
        
        fun bind(message: Message) {
            // 使用 Binder 渲染，这样代码块就会被高亮显示
            MarkdownViewBinder.bind(itemView.context, markdownContainer, message.content)
        }
    }

    class AiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val markdownContainer: LinearLayout = itemView.findViewById(R.id.markdownContainer)
        private val cardThinking: View = itemView.findViewById(R.id.cardThinking)
        private val tvThinkingStatus: TextView = itemView.findViewById(R.id.tvThinkingStatus)
        private val layoutActions: View = itemView.findViewById(R.id.layoutActions)

        fun bind(message: Message) {
            MarkdownViewBinder.bind(itemView.context, markdownContainer, message.content)

            if (message.content.length > 50) {
                cardThinking.visibility = View.VISIBLE
                tvThinkingStatus.text = if (message.status.name == "TYPING") "深度思考中..." else "已完成深度思考"
            } else {
                cardThinking.visibility = View.GONE
            }

            layoutActions.visibility = if (message.status.name == "TYPING") View.GONE else View.VISIBLE
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.content == newItem.content && oldItem.status == newItem.status
    }
}