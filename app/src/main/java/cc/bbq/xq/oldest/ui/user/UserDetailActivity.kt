package cc.bbq.xq.oldest.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import cc.bbq.xq.oldest.AuthManager
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.RetrofitClient
import cc.bbq.xq.oldest.databinding.ActivityUserDetailBinding
import com.bumptech.glide.Glide

class UserDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailBinding
    private val viewModel: UserDetailViewModel by viewModels()
    private var currentUserId: Long = -1L

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"
        
        fun start(context: Context, userId: Long) {
            val intent = Intent(context, UserDetailActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        if (currentUserId == -1L) {
            finish()
            return
        }

        setupClickListeners()
        setupObservers()
        viewModel.loadUserDetails(currentUserId)
    }

    private fun setupClickListeners() {
        // 点击"帖子"区域查看用户帖子
        binding.layoutPosts.setOnClickListener {
            MyPostsActivity.start(this, currentUserId)
        }
    }

    private fun setupObservers() {
        viewModel.userData.observe(this) { user ->
            binding.apply {
                tvNickname.text = user.nickname
                tvLevel.text = user.hierarchy
                tvUserId.text = "${user.username}"
                tvCoins.text = user.money.toString()
                tvFollowersCount.text = user.followerscount
                tvFansCount.text = user.fanscount
                tvPostsCount.text = user.postcount
                tvLikesCount.text = user.likecount
                
                Glide.with(this@UserDetailActivity)
                    .load(user.usertx.replace("\\", ""))
                    .circleCrop()
                    .placeholder(R.drawable.ic_menu_profile)
                    .into(ivAvatar)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                binding.errorText.text = message
                binding.errorLayout.visibility = View.VISIBLE
            } else {
                binding.errorLayout.visibility = View.GONE
            }
        }
    }
}