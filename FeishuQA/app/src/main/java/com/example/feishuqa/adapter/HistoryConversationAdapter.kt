package com.example.feishuqa.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.feishuqa.R
import com.example.feishuqa.data.entity.Conversation
import com.example.feishuqa.databinding.ItemHistoryConversationBinding

/**
 * 历史对话列表适配器
 */
class HistoryConversationAdapter(
    private val onItemClick: (Conversation) -> Unit
) : RecyclerView.Adapter<HistoryConversationAdapter.ViewHolder>() {

    // 当前对话列表
    private var conversations: List<Conversation> = emptyList()

    // 当前选中的对话ID
    private var selectedId: String? = null

    /**
     * 更新对话列表
     */
    fun submitList(newList: List<Conversation>) {
        conversations = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemHistoryConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            // 设置标题
            binding.tvTitle.text = conversation.getDisplayTitle()

            // 设置选中状态背景
            val isSelected = conversation.id == selectedId
            binding.layoutItem.setBackgroundResource(
                if (isSelected) R.drawable.bg_conversation_item_selected
                else android.R.color.transparent
            )

            // 点击事件
            binding.root.setOnClickListener {
                val previousSelected = selectedId
                selectedId = conversation.id

                // 刷新之前选中项和当前选中项
                if (previousSelected != null) {
                    val previousIndex = conversations.indexOfFirst { it.id == previousSelected }
                    if (previousIndex >= 0) {
                        notifyItemChanged(previousIndex)
                    }
                }
                notifyItemChanged(adapterPosition)

                onItemClick(conversation)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount(): Int = conversations.size

    /**
     * 设置选中的对话
     */
    fun setSelectedConversation(id: String?) {
        val previousSelected = selectedId
        selectedId = id

        if (previousSelected != null) {
            val previousIndex = conversations.indexOfFirst { it.id == previousSelected }
            if (previousIndex >= 0) {
                notifyItemChanged(previousIndex)
            }
        }

        if (id != null) {
            val newIndex = conversations.indexOfFirst { it.id == id }
            if (newIndex >= 0) {
                notifyItemChanged(newIndex)
            }
        }
    }
}





