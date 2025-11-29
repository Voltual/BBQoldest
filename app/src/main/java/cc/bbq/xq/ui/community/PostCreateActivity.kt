package cc.bbq.xq.ui.community

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cc.bbq.xq.AuthManager
import cc.bbq.xq.R
import cc.bbq.xq.RetrofitClient
import cc.bbq.xq.databinding.ActivityPostCreateBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class PostCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "创建新帖"
    }
    
    private fun setupListeners() {
        binding.btnSubmit.setOnClickListener {
            if (validateInput()) {
                submitPost()
            }
        }
    }
    
    private fun validateInput(): Boolean {
        if (binding.etTitle.text.isNullOrBlank()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (binding.etContent.text.isNullOrBlank()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun submitPost() {
        val credentials = AuthManager.getCredentials(this) ?: run {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
            return
        }
        
        val title = binding.etTitle.text.toString()
        val content = "${binding.etContent.text} 機型：Android｜"
        val imageUrls = binding.etImageUrls.text.toString()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.createPost(
                    token = credentials.third,
                    title = title,
                    content = content,
                    sectionId = 10, // 默认综合板块
                    imageUrls = imageUrls
                )
                
                runOnUiThread {
                    if (response.isSuccessful && response.body()?.code == 1) {
                        Toast.makeText(this@PostCreateActivity, "发帖成功", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMsg = response.body()?.msg ?: "未知错误"
                        Toast.makeText(this@PostCreateActivity, "发帖失败: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@PostCreateActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}