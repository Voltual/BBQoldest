package cc.bbq.xq.oldest.ui.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.RetrofitClient

class MessageAdapter(
    private val onItemClick: (RetrofitClient.models.MessageNotification) -> Unit
) : ListAdapter<RetrofitClient.models.MessageNotification, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(
        itemView: View,
        private val onItemClick: (RetrofitClient.models.MessageNotification) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val content: TextView = itemView.findViewById(R.id.tvContent)
        private val time: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(message: RetrofitClient.models.MessageNotification) {
            title.text = message.title
            content.text = message.content
            time.text = message.time
            
            // 根据消息类型设置背景色
            val bgColor = when (message.type) {
                3 -> R.color.message_like_bg // 点赞
                5 -> R.color.message_comment_bg // 评论
                else -> R.color.message_default_bg
            }
            
            itemView.setBackgroundColor(
                ContextCompat.getColor(itemView.context, bgColor)
            )
            
            itemView.setOnClickListener { onItemClick(message) }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<RetrofitClient.models.MessageNotification>() {
    override fun areItemsTheSame(
        oldItem: RetrofitClient.models.MessageNotification, 
        newItem: RetrofitClient.models.MessageNotification
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: RetrofitClient.models.MessageNotification, 
        newItem: RetrofitClient.models.MessageNotification
    ): Boolean = oldItem == newItem
}