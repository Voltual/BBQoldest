package cc.bbq.xq.ui.community.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cc.bbq.xq.R
import cc.bbq.xq.RetrofitClient
import cc.bbq.xq.ui.community.PostDetailActivity
import com.bumptech.glide.Glide

class PostAdapter(private val context: Context) : ListAdapter<RetrofitClient.models.Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
        
        // 添加点击事件
        holder.itemView.setOnClickListener {
            // 创建跳转意图
            val intent = Intent(context, PostDetailActivity::class.java).apply {
                putExtra("POST_ID", post.postid) // 传递帖子ID
            }
            context.startActivity(intent)
        }
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val content: TextView = itemView.findViewById(R.id.tvContent)
        private val author: TextView = itemView.findViewById(R.id.tvAuthor)
        private val avatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val views: TextView = itemView.findViewById(R.id.tvViews)
        private val likes: TextView = itemView.findViewById(R.id.tvLikes)
        private val comments: TextView = itemView.findViewById(R.id.tvComments)
        private val time: TextView = itemView.findViewById(R.id.tvTime)
        private val imageContainer: ViewGroup = itemView.findViewById(R.id.imageContainer)

        fun bind(post: RetrofitClient.models.Post) {
            title.text = post.title
            content.text = post.content
            author.text = post.nickname
            views.text = "浏览: ${post.view}"
            likes.text = "点赞: ${post.thumbs}"
            comments.text = "评论: ${post.comment}"
            time.text = post.create_time
            
            // 加载头像
            Glide.with(itemView)
                .load(post.usertx.replace("\\", ""))
                .circleCrop()
                .placeholder(R.drawable.ic_menu_profile)
                .into(avatar)
            
            // 处理图片
            imageContainer.removeAllViews()
            post.img_url?.take(3)?.forEach { imageUrl ->
                val imageView = ImageView(itemView.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    adjustViewBounds = true
                }
                Glide.with(itemView)
                    .load(imageUrl.replace("\\", ""))
                    .into(imageView)
                imageContainer.addView(imageView)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<RetrofitClient.models.Post>() {
    override fun areItemsTheSame(
        oldItem: RetrofitClient.models.Post, 
        newItem: RetrofitClient.models.Post
    ): Boolean = oldItem.postid == newItem.postid

    override fun areContentsTheSame(
        oldItem: RetrofitClient.models.Post, 
        newItem: RetrofitClient.models.Post
    ): Boolean = oldItem == newItem
}