package cc.bbq.xq.oldest.ui.user

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.UserManager
import cc.bbq.xq.oldest.RetrofitClient
import cc.bbq.xq.oldest.databinding.ActivityUserListBinding
import cc.bbq.xq.oldest.ui.user.adapter.UserAdapter
import cc.bbq.xq.oldest.ui.user.UserDetailActivity

abstract class BaseUserListActivity : AppCompatActivity() {

    protected lateinit var binding: ActivityUserListBinding
    protected lateinit var adapter: UserAdapter
    protected lateinit var userManager: UserManager
    
    protected var loading = false
    protected var currentPage = 1
    protected var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        userManager = ViewModelProvider(this).get(UserManager::class.java)
        setupToolbar()
        setupRecyclerView()
        setupRefresh()
        loadInitialData()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter { user ->
            // 点击用户项跳转到详情页
            UserDetailActivity.start(this, user.id)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BaseUserListActivity)
            adapter = this@BaseUserListActivity.adapter
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount
                    
                    if (!loading && lastVisibleItemPosition == totalItemCount - 1 && currentPage < totalPages) {
                        loadNextPage()
                    }
                }
            })
        }
    }
    
    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            resetPagination()
            loadData()
        }
    }
    
    protected fun resetPagination() {
        currentPage = 1
        totalPages = 1
        adapter.submitList(emptyList())
    }
    
    abstract fun loadInitialData()
    abstract fun loadData()
    
    protected fun loadNextPage() {
        currentPage++
        loadData()
    }
    
    protected fun handleLoadingState(isLoading: Boolean) {
        this.loading = isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.swipeRefresh.isRefreshing = false
    }
    
    protected fun handleDataLoaded(data: RetrofitClient.models.UserListData) {
        totalPages = data.pagecount
        
        val newList = if (currentPage == 1) {
            data.list
        } else {
            val currentList = adapter.currentList.toMutableList()
            currentList.addAll(data.list)
            currentList
        }
        
        adapter.submitList(newList)
        handleLoadingState(false)
        
        // 显示空状态
        binding.emptyState.visibility = if (newList.isEmpty()) View.VISIBLE else View.GONE
    }
    
    protected fun handleError(message: String) {
        binding.errorText.text = message
        binding.errorLayout.visibility = View.VISIBLE
        handleLoadingState(false)
    }
}
