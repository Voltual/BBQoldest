package cc.bbq.xq.ui.message

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cc.bbq.xq.AuthManager
import cc.bbq.xq.R
import cc.bbq.xq.RetrofitClient
import cc.bbq.xq.databinding.ActivityMessageCenterBinding
import cc.bbq.xq.ui.community.PostDetailActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MessageCenterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageCenterBinding
    private lateinit var viewModel: MessageViewModel
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 手动初始化 ViewModel
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MessageViewModel() as T
            }
        }).get(MessageViewModel::class.java)
        
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        loadData()
    }
    
    private fun setupRecyclerView() {
        adapter = MessageAdapter { message ->
            message.postid?.let { postId ->
                val intent = Intent(this, PostDetailActivity::class.java).apply {
                    putExtra("POST_ID", postId)
                }
                startActivity(intent)
            }
        }
        
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
    
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnRetry.setOnClickListener {
            loadData()
        }
        
        binding.btnLoadMore.setOnClickListener {
            AuthManager.getCredentials(this)?.third?.let { token ->
                viewModel.loadNextPage(token)
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                adapter.submitList(messages)
                binding.emptyState.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    binding.errorText.text = it
                    binding.errorLayout.visibility = View.VISIBLE
                } ?: run {
                    binding.errorLayout.visibility = View.GONE
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.currentPage.collect { page ->
                binding.pageInfo.text = "第 $page 页/共 ${viewModel.totalPages.value} 页"
            }
        }
    }
    
    private fun loadData() {
        AuthManager.getCredentials(this)?.let { (_, _, token) ->
            viewModel.loadMessages(token)
        } ?: run {
            binding.errorText.text = "用户未登录"
            binding.errorLayout.visibility = View.VISIBLE
        }
    }
}