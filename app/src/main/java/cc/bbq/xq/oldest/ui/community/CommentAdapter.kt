package cc.bbq.xq.oldest.ui.community.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.RetrofitClient
import cc.bbq.xq.oldest.ui.user.UserDetailActivity
import com.bumptech.glide.Glide

class CommentAdapter(
    private val onReplyClicked: (comment: RetrofitClient.models.Comment) -> Unit,
    private val onImageClicked: (imageUrl: String) -> Unit
) : ListAdapter<RetrofitClient.models.Comment, CommentAdapter.CommentViewHolder>(
    CommentDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view, onReplyClicked, onImageClicked)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(
        itemView: View,
        private val onReplyClicked: (comment: RetrofitClient.models.Comment) -> Unit,
        private val onImageClicked: (imageUrl: String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val author: TextView = itemView.findViewById(R.id.tvAuthor)
        private val content: TextView = itemView.findViewById(R.id.tvContent)
        private val time: TextView = itemView.findViewById(R.id.tvTime)
        private val replyInfo: TextView = itemView.findViewById(R.id.tvReplyInfo)
        private val replyButton: TextView = itemView.findViewById(R.id.tvReplyButton)
        private val imageContainer: LinearLayout = itemView.findViewById(R.id.imageContainer)

        fun bind(comment: RetrofitClient.models.Comment) {
            author.text = comment.nickname ?: comment.username ?: "匿名用户"
            content.text = comment.content
            time.text = comment.time_ago ?: comment.time ?: "未知时间"
            
            // 加载头像并设置点击事件
            val avatarUrl = comment.usertx?.replace("\\", "") ?: ""
            Glide.with(itemView.context)
                .load(if (avatarUrl.isNotEmpty()) avatarUrl else R.drawable.ic_menu_profile)
                .circleCrop()
                .placeholder(R.drawable.ic_menu_profile)
                .into(avatar)
            
            // 使用 userId
            avatar.setOnClickListener {
                comment.userid?.let { userId ->
                    UserDetailActivity.start(itemView.context, userId)
                }
            }
            
            // 回复信息显示
            if (!comment.parentnickname.isNullOrEmpty() && !comment.parentcontent.isNullOrEmpty()) {
                replyInfo.text = "回复 @${comment.parentnickname}: ${comment.parentcontent}"
                replyInfo.visibility = View.VISIBLE
            } else {
                replyInfo.visibility = View.GONE
            }
            
            // 图片显示
            imageContainer.removeAllViews()
            comment.image_path?.let { images ->
                images.take(3).forEach { imageUrl ->
                    val cleanUrl = imageUrl.replace("\\", "")
                    if (cleanUrl.isNotEmpty()) {
                        val imageView = ImageView(itemView.context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                dpToPx(120f)
                            ).apply {
                                topMargin = dpToPx(8f)
                            }
                            adjustViewBounds = true
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            tag = cleanUrl
                        }
                        
                        Glide.with(itemView)
                            .load(cleanUrl)
                            .into(imageView)
                            
                        imageView.setOnClickListener {
                            onImageClicked(cleanUrl)
                        }
                        
                        imageContainer.addView(imageView)
                    }
                }
            }
            
            // 回复按钮
            replyButton.setOnClickListener { onReplyClicked(comment) }
        }
        
        private fun dpToPx(dp: Float): Int {
            return (dp * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}

class CommentDiffCallback : DiffUtil.ItemCallback<RetrofitClient.models.Comment>() {
    override fun areItemsTheSame(
        oldItem: RetrofitClient.models.Comment, 
        newItem: RetrofitClient.models.Comment
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: RetrofitClient.models.Comment, 
        newItem: RetrofitClient.models.Comment // 修复拼写错误
    ) = oldItem == newItem
}