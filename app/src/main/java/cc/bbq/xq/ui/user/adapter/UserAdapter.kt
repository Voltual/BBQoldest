package cc.bbq.xq.ui.user.adapter

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
import com.bumptech.glide.Glide

class UserAdapter(
    private val onItemClicked: (user: RetrofitClient.models.UserItem) -> Unit
) : ListAdapter<RetrofitClient.models.UserItem, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    class UserViewHolder(
        itemView: View,
        private val onItemClicked: (user: RetrofitClient.models.UserItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val nickname: TextView = itemView.findViewById(R.id.tvNickname)
        private val hierarchy: TextView = itemView.findViewById(R.id.tvHierarchy)
        private val signature: TextView = itemView.findViewById(R.id.tvSignature)
        private val titleContainer: ViewGroup = itemView.findViewById(R.id.titleContainer)

        fun bind(user: RetrofitClient.models.UserItem) {
            nickname.text = user.nickname
            hierarchy.text = user.hierarchy
            signature.text = user.signature
            
            // 加载头像
            val avatarUrl = user.usertx.replace("\\", "")
            Glide.with(itemView.context)
                .load(avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_menu_profile)
                .into(avatar)
            
            // 处理用户标签
            titleContainer.removeAllViews()
            user.title.take(2).forEach { titleText ->
                val titleView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_user_title, titleContainer, false)
                titleView.findViewById<TextView>(R.id.tvTitle).text = titleText
                titleContainer.addView(titleView)
            }
            
            itemView.setOnClickListener { onItemClicked(user) }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<RetrofitClient.models.UserItem>() {
    override fun areItemsTheSame(
        oldItem: RetrofitClient.models.UserItem, 
        newItem: RetrofitClient.models.UserItem
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: RetrofitClient.models.UserItem, 
        newItem: RetrofitClient.models.UserItem
    ): Boolean = oldItem == newItem
}