package cc.bbq.xq.oldest.ui.plaza

import android.content.Intent
import android.view.ViewGroup
import android.net.Uri // 添加 Uri 导入
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible // 添加 isVisible 扩展
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.RetrofitClient
import cc.bbq.xq.oldest.RetrofitClient.models.AppDetail
import cc.bbq.xq.oldest.databinding.ActivityAppDetailBinding
import cc.bbq.xq.oldest.ui.plaza.adapter.AppCommentAdapter
import cc.bbq.xq.oldest.utils.DimensionUtils
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

class AppDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDetailBinding
    private lateinit var viewModel: AppDetailViewModel
    private lateinit var commentAdapter: AppCommentAdapter
    private var appId: Long = 0
    private var versionId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        appId = intent.getLongExtra("APP_ID", 0)
        versionId = intent.getLongExtra("VERSION_ID", 0)
        if (appId == 0L || versionId == 0L) {
            finish()
            return
        }
        
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        loadData()
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AppDetailViewModel::class.java]
        
        viewModel.appDetail.observe(this) { app ->
            app?.let {
                binding.tvAppName.text = it.appname
                binding.tvAppSize.text = "大小: ${it.app_size}"
                binding.tvAppDesc.text = it.app_introduce ?: "暂无描述"
                binding.tvDownloadCount.text = "下载次数: ${it.download_count}"
                binding.tvVersion.text = "版本: ${it.version}"
                binding.tvAuthor.text = "开发者: ${it.nickname}" // 添加前缀
                
                // 加载应用图标
                Glide.with(this)
                    .load((it.app_icon ?: "").replace("\\", ""))
                    .placeholder(R.drawable.ic_menu_apps)
                    .error(R.drawable.ic_menu_apps)
                    .into(binding.ivAppIcon)
                
                // 加载应用截图
                val screenshots = it.app_introduction_image_array?.filter { url -> url.isNotEmpty() }
                if (!screenshots.isNullOrEmpty()) {
                    binding.cardScreenshots.isVisible = true // 使用扩展函数
                    binding.imageContainer.removeAllViews()
                    
                    val screenWidth = DimensionUtils.getScreenWidth(this)
                    val imageSize = (screenWidth * 0.6).toInt()
                    
                    screenshots.forEachIndexed { index, imageUrl ->
                        val imageView = ImageView(this).apply {
                            layoutParams = ViewGroup.MarginLayoutParams(
                                imageSize,
                                (imageSize * 0.75).toInt() // 4:3 比例
                            ).apply {
                                marginEnd = if (index < screenshots.size - 1) 16 else 0
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            adjustViewBounds = true
                        }
                        Glide.with(this)
                            .load(imageUrl.replace("\\", ""))
                            .into(imageView)
                        binding.imageContainer.addView(imageView)
                    }
                } else {
                    binding.cardScreenshots.isVisible = false // 使用扩展函数
                }
                
                // 设置下载按钮点击事件
                binding.btnDownload.setOnClickListener {
                    val downloadUrl = app.download
                    if (downloadUrl.isNullOrEmpty()) {
                        Snackbar.make(binding.root, "该应用无法直接下载", Snackbar.LENGTH_SHORT).show()
                    } else {
                        // 修复：添加 Intent.ACTION_VIEW
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                        startActivity(intent)
                    }
                }
            }
        }
        
        viewModel.comments.observe(this) { comments ->
            commentAdapter.submitList(comments)
            binding.progressBar.isVisible = false // 使用扩展函数
            binding.tvCommentsTitle.text = "用户评论(${comments.size})" // 添加评论数量
        }
        
        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                binding.errorText.text = message
                binding.errorLayout.isVisible = true // 使用扩展函数
                binding.progressBar.isVisible = false // 使用扩展函数
            } else {
                binding.errorLayout.isVisible = false // 使用扩展函数
            }
        }
    }
    
    private fun setupRecyclerView() {
        commentAdapter = AppCommentAdapter()
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@AppDetailActivity)
            adapter = commentAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            binding.errorLayout.isVisible = false // 使用扩展函数
            loadData()
        }
        
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // 添加分享功能
        binding.btnShare.setOnClickListener {
            val appName = binding.tvAppName.text.toString()
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "发现一个不错的应用: $appName")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(intent, "分享应用"))
        }
    }
    
    private fun loadData() {
        binding.progressBar.isVisible = true // 使用扩展函数
        viewModel.loadAppDetail(appId, versionId)
        viewModel.loadComments(appId, versionId)
    }
}