package cc.bbq.xq.oldest.ui.plaza.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.RetrofitClient
import com.bumptech.glide.Glide

class AppCommentAdapter : ListAdapter<RetrofitClient.models.AppComment, AppCommentAdapter.AppCommentViewHolder>(
    AppCommentDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppCommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return AppCommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppCommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AppCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val author: TextView = itemView.findViewById(R.id.tvAuthor)
        private val content: TextView = itemView.findViewById(R.id.tvContent)
        private val time: TextView = itemView.findViewById(R.id.tvTime)
        private val replyInfo: TextView = itemView.findViewById(R.id.tvReplyInfo)

        fun bind(comment: RetrofitClient.models.AppComment) {
            author.text = comment.nickname
            content.text = comment.content
            time.text = comment.time_ago
            
            // 加载头像
            Glide.with(itemView.context)
                .load(comment.usertx.replace("\\", ""))
                .circleCrop()
                .placeholder(R.drawable.ic_menu_profile)
                .into(avatar)
            
            // 显示回复信息
            comment.parentnickname?.let {
                replyInfo.text = "回复 @$it: ${comment.parentcontent}"
                replyInfo.visibility = View.VISIBLE
            } ?: run {
                replyInfo.visibility = View.GONE
            }
        }
    }
}

class AppCommentDiffCallback : DiffUtil.ItemCallback<RetrofitClient.models.AppComment>() {
    override fun areItemsTheSame(
        oldItem: RetrofitClient.models.AppComment, 
        newItem: RetrofitClient.models.AppComment
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: RetrofitClient.models.AppComment, 
        newItem: RetrofitClient.models.AppComment
    ): Boolean = oldItem == newItem
}
