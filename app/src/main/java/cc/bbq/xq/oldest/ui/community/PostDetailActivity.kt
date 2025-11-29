package cc.bbq.xq.oldest.ui.community

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cc.bbq.xq.oldest.AuthManager
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.RetrofitClient
import cc.bbq.xq.oldest.databinding.ActivityPostDetailBinding
import cc.bbq.xq.oldest.ui.community.adapter.CommentAdapter
import cc.bbq.xq.oldest.ui.user.UserDetailActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var viewModel: PostDetailViewModel
    private lateinit var commentAdapter: CommentAdapter
    private var postId: Long = 0
    private var currentReplyComment: RetrofitClient.models.Comment? = null
    private var currentUserId: Long? = null
    private var postAuthorId: Long? = null
    private var isLiked = false
    private val imageViews = mutableListOf<ImageView>() // 存储图片视图引用

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
      
        postId = intent.getLongExtra("POST_ID", 0)
        if (postId == 0L) {
            finish()
            return
        }
        
        // 获取当前用户ID
        val credentials = AuthManager.getCredentials(this)
        currentUserId = credentials?.first?.toLongOrNull()
      
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        loadData()
    }
  
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[PostDetailViewModel::class.java]
      
        viewModel.postDetail.observe(this) { post ->
            post?.let {
                binding.tvTitle.text = it.title
                binding.tvContent.text = it.content
                binding.tvAuthor.text = it.nickname
                postAuthorId = it.userid // 修复：使用小写 userid
                binding.tvViews.text = "浏览: ${it.view}"
                binding.tvLikes.text = "点赞: ${it.thumbs}"
                binding.tvComments.text = "评论: ${it.comment}"
                binding.tvTime.text = it.create_time_ago
                binding.tvLocation.text = it.ip_address
              
                // 更新点赞状态
                isLiked = it.is_thumbs == 1
                updateLikeButton()
              
                // 加载头像并设置点击事件
                Glide.with(this)
                    .load(it.usertx.replace("\\", ""))
                    .circleCrop()
                    .placeholder(R.drawable.ic_menu_profile)
                    .into(binding.ivAvatar)
                
                // 修复：使用小写 userid
binding.ivAvatar.setOnClickListener {
    postAuthorId?.let { userId ->
        UserDetailActivity.start(this, userId)
    }
}
              
                // 清除现有图片
                binding.imageContainer.removeAllViews()
                imageViews.clear()
              
                // 添加新图片
                it.img_url?.take(3)?.forEachIndexed { index, imageUrl ->
                    val imageView = ImageView(this).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        tag = imageUrl.replace("\\", "")
                    }
                    
                    Glide.with(this)
                        .load(imageUrl.replace("\\", ""))
                        .into(imageView)
                    
                    // 添加点击事件
                    imageView.setOnClickListener {
                        startActivity(
                            Intent(this@PostDetailActivity, ImagePreviewActivity::class.java).apply {
                                putExtra("IMAGE_URL", imageView.tag as String)
                            }
                        )
                    }
                    
                    binding.imageContainer.addView(imageView)
                    imageViews.add(imageView)
                }
            }
        }
      
        viewModel.comments.observe(this) { comments ->
            commentAdapter.submitList(comments)
            binding.progressBar.visibility = View.GONE
        }
      
        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                binding.errorText.text = message
                binding.errorLayout.visibility = View.VISIBLE
            } else {
                binding.errorLayout.visibility = View.GONE
            }
        }
      
        viewModel.replySuccess.observe(this) { success ->
            if (success) {
                viewModel.loadComments(postId)
                currentReplyComment = null
            }
        }
      
        viewModel.commentSuccess.observe(this) { success ->
            if (success) {
                viewModel.loadComments(postId)
                val currentCount = binding.tvComments.text.toString()
                    .substringAfter("评论: ")
                    .toIntOrNull() ?: 0
                binding.tvComments.text = "评论: ${currentCount + 1}"
            }
        }
        
        viewModel.likeSuccess.observe(this) { success ->
            if (success) {
                isLiked = !isLiked
                updateLikeButton()
                val currentLikes = binding.tvLikes.text.toString()
                    .substringAfter("点赞: ")
                    .toIntOrNull() ?: 0
                    
                val newCount = if (isLiked) currentLikes + 1 else currentLikes - 1
                binding.tvLikes.text = "点赞: $newCount"
            }
        }
        
        viewModel.deleteSuccess.observe(this) { success ->
            if (success) finish()
        }
    }
  
    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(
            onReplyClicked = { comment ->
                currentReplyComment = comment
                showReplyDialog(comment)
            },
            onImageClicked = { imageUrl ->
                startActivity(
                    Intent(this, ImagePreviewActivity::class.java).apply {
                        putExtra("IMAGE_URL", imageUrl)
                    }
                )
            }
        )
      
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@PostDetailActivity)
            adapter = commentAdapter
        }
    }
  
    private fun showMoreMenu() {
        val popup = PopupMenu(this, binding.btnMore)
        popup.menuInflater.inflate(R.menu.post_detail_menu, popup.menu)
        
        // 直接显示所有菜单项（服务器会验证权限）
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_comment -> showCommentDialog()
                R.id.action_share -> sharePost()
                R.id.action_delete -> confirmDeletePost()
                R.id.action_like -> toggleLike()
            }
            true
        }
        popup.show()
    }
    
    private fun sharePost() {
        val shareUrl = "http://api.xiaoqu.online/post/$postId.html"
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "分享一个有趣的帖子: $shareUrl")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, "分享帖子"))
    }
    
    private fun confirmDeletePost() {
        AlertDialog.Builder(this)
            .setTitle("删除帖子")
            .setMessage("确定要删除这个帖子吗？此操作不可撤销。")
            .setPositiveButton("删除") { _, _ -> 
                deletePost()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun deletePost() {
        viewModel.deletePost(postId)
    }
    
    private fun toggleLike() {
        viewModel.toggleLike(postId, isLiked)
    }
    
    private fun updateLikeButton() {
        val iconRes = if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_outline
        binding.btnLike.setImageResource(iconRes)
    }
  
    private fun showReplyDialog(comment: RetrofitClient.models.Comment) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reply, null)
        val etReply = dialogView.findViewById<EditText>(R.id.etReply)
        val etImageUrl = dialogView.findViewById<EditText>(R.id.etImageUrl)
      
        etReply.hint = "回复 @${comment.nickname}"
      
        MaterialAlertDialogBuilder(this)
            .setTitle("回复评论")
            .setView(dialogView)
            .setPositiveButton("发送") { dialog, which ->
                val replyContent = etReply.text.toString().trim()
                val imageUrl = etImageUrl.text.toString().trim()
              
                if (replyContent.isNotEmpty()) {
                    viewModel.postReply(
                        postId = postId,
                        commentId = comment.id,
                        content = "@${comment.nickname} $replyContent",
                        imageUrl = imageUrl
                    )
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
  
    private fun showCommentDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reply, null)
        val etComment = dialogView.findViewById<EditText>(R.id.etReply)
        val etImageUrl = dialogView.findViewById<EditText>(R.id.etImageUrl)
      
        etComment.hint = "评论本帖"
      
        MaterialAlertDialogBuilder(this)
            .setTitle("评论帖子")
            .setView(dialogView)
            .setPositiveButton("发送") { dialog, which ->
                val commentContent = etComment.text.toString().trim()
                val imageUrl = etImageUrl.text.toString().trim()
              
                if (commentContent.isNotEmpty()) {
                    viewModel.postComment(
                        postId = postId,
                        content = commentContent,
                        imageUrl = imageUrl
                    )
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            binding.errorLayout.visibility = View.GONE
            loadData()
        }
      
        binding.btnBack.setOnClickListener { finish() }
        binding.btnMore.setOnClickListener { showMoreMenu() }
        binding.btnLike.setOnClickListener { toggleLike() }
    }
  
    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadPostDetail(postId)
        viewModel.loadComments(postId)
    }
}