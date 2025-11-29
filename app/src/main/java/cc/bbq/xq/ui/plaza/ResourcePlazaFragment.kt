package cc.bbq.xq.ui.plaza

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.bbq.xq.R
import cc.bbq.xq.RetrofitClient
import cc.bbq.xq.RetrofitClient.models.AppListResponse
import cc.bbq.xq.databinding.FragmentResourcePlazaBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Response

class ResourcePlazaFragment : Fragment() {
    private var _binding: FragmentResourcePlazaBinding? = null
    private val binding get() = _binding!!
  
    private val viewModel: PlazaViewModel by viewModels { 
        PlazaViewModelFactory(requireActivity().application)
    }
  
    private var isFullLoadEnabled = false
    private var isSearchMode = false

    // 适配器实例
    private lateinit var newSharesAdapter: AppHorizontalAdapter
    private lateinit var popularAppsAdapter: AppGridAdapter
    private lateinit var searchAdapter: AppGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResourcePlazaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      
        binding.switchLoadAll.isChecked = isFullLoadEnabled
      
        binding.switchLoadAll.setOnCheckedChangeListener { _, isChecked ->
            isFullLoadEnabled = isChecked
            loadData(isFullLoadEnabled)
        }

        setupRecyclerViews()
        setupObservers()
        loadData(false)
        setupSearch()
    }

    private fun setupRecyclerViews() {
        val glideRequestManager = Glide.with(this)
      
        // 初始化适配器
        newSharesAdapter = AppHorizontalAdapter(requireContext(), glideRequestManager)
        popularAppsAdapter = AppGridAdapter(requireContext(), glideRequestManager)
        searchAdapter = AppGridAdapter(requireContext(), glideRequestManager)

        binding.rvNewShares.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = newSharesAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(10) // 缓存少量视图
        }

        binding.rvPopularApps.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = popularAppsAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(20) // 缓存少量视图
            
            // 添加滚动监听实现分页加载
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!isSearchMode && !viewModel.isLoading.value!! && 
                        !recyclerView.canScrollVertically(1)) {
                        viewModel.loadNextPage()
                    }
                }
            })
        }
      
        binding.rvSearchResults.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = searchAdapter
            setItemViewCacheSize(20) // 缓存少量视图
            
            // 添加滚动监听实现分页加载
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (isSearchMode && !viewModel.isLoading.value!! && 
                        !recyclerView.canScrollVertically(1)) {
                        viewModel.searchNextPage()
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        viewModel.plazaData.observe(viewLifecycleOwner) { plazaData ->
            if (!isSearchMode) {
                resetUI()
              
                // 更新最新分享列表
                newSharesAdapter.submitList(
                    if (isFullLoadEnabled) plazaData.newShares else plazaData.newShares.take(4)
                )
              
                // 更新热门应用列表
                popularAppsAdapter.submitList(plazaData.popularApps)
            }
        }
      
        // 搜索结果显示
        viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            if (searchResults.isNotEmpty() && isSearchMode) {
                showSearchResults(searchResults)
            }
        }
        
        // 加载状态显示
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingMoreIndicator.isVisible = isLoading
        }
        
        // 错误消息处理
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                // 隐藏键盘
                v.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }
      
        binding.searchContainer.setEndIconOnClickListener {
            performSearch()
        }
    }
  
    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            isSearchMode = true
            viewModel.searchResources(query)
        } else {
            isSearchMode = false
            viewModel.cancelSearch()
            resetUI()
        }
    }
  
    private fun showSearchResults(results: List<AppItem>) {
        binding.tvNewSharesTitle.visibility = View.GONE
        binding.rvNewShares.visibility = View.GONE
        binding.tvPopularTitle.visibility = View.GONE
        binding.rvPopularApps.visibility = View.GONE
      
        binding.tvSearchResultsTitle.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.VISIBLE
      
        searchAdapter.submitList(results)
    }

    private fun loadData(fullLoad: Boolean) {
        viewModel.loadData(fullLoad)
    }
  
    private fun resetUI() {
        isSearchMode = false
        binding.tvNewSharesTitle.visibility = View.VISIBLE
        binding.rvNewShares.visibility = View.VISIBLE
        binding.tvPopularTitle.visibility = View.VISIBLE
        binding.rvPopularApps.visibility = View.VISIBLE
        binding.tvSearchResultsTitle.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 清除适配器引用
        binding.rvNewShares.adapter = null
        binding.rvPopularApps.adapter = null
        binding.rvSearchResults.adapter = null
        _binding = null
    }
}

// 数据模型类
data class AppItem(
    val id: String, 
    val name: String, 
    val iconUrl: String,
    val versionId: Long
) {
    // 添加唯一标识符用于DiffUtil
    val uniqueId: String get() = "$id-$versionId"
}

data class PlazaData(
    val newShares: List<AppItem>,
    val popularApps: List<AppItem>
)

// DiffUtil回调类 - 优化性能
class AppItemDiffCallback : DiffUtil.ItemCallback<AppItem>() {
    override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
        return oldItem.uniqueId == newItem.uniqueId
    }

    override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
        return oldItem == newItem
    }
}

// ViewModel工厂
class PlazaViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlazaViewModel(application) as T
    }
}

// 增强的ViewModel（支持分页加载）
class PlazaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlazaRepository(application.applicationContext)
    private val _plazaData = MutableLiveData<PlazaData>()
    private val _searchResults = MutableLiveData<List<AppItem>>()
    private val _errorMessage = MutableLiveData<String>()
    private val _isLoading = MutableLiveData(false)

    // 分页状态
    private var popularAppsPage = 1
    private var popularAppsTotalPages = 1
    private var searchPage = 1
    private var searchTotalPages = 1
    private var currentQuery = ""
    
    val plazaData: LiveData<PlazaData> = _plazaData
    val searchResults: LiveData<List<AppItem>> = _searchResults
    val errorMessage: LiveData<String> = _errorMessage
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadData(fullLoad: Boolean) {
        if (_isLoading.value == true) return
        
        _isLoading.value = true
        popularAppsPage = 1
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newShares = repository.getNewShares(if (fullLoad) 10 else 4)
                val popularApps = repository.getPopularApps(if (fullLoad) 20 else 8, page = popularAppsPage)
                
                // 更新总页数
                popularAppsTotalPages = repository.lastTotalPages ?: 1
                
                if (newShares.isEmpty() && popularApps.isEmpty()) {
                    _errorMessage.postValue("加载失败，请检查网络连接")
                } else {
                    _plazaData.postValue(PlazaData(
                        newShares = newShares.map { convertToUiModel(it) },
                        popularApps = popularApps.map { convertToUiModel(it) }
                    ))
                }
            } catch (e: Exception) {
                _errorMessage.postValue("发生错误: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun loadNextPage() {
        if (_isLoading.value == true || popularAppsPage >= popularAppsTotalPages) return
        
        _isLoading.value = true
        popularAppsPage++
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newApps = repository.getPopularApps(10, page = popularAppsPage)
                popularAppsTotalPages = repository.lastTotalPages ?: 1
                
                val currentList = _plazaData.value?.popularApps?.toMutableList() ?: mutableListOf()
                currentList.addAll(newApps.map { convertToUiModel(it) })
                
                _plazaData.postValue(_plazaData.value?.copy(popularApps = currentList))
            } catch (e: Exception) {
                _errorMessage.postValue("加载更多失败: ${e.localizedMessage}")
                popularAppsPage-- // 回退页码
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
  
    fun searchResources(query: String) {
        if (_isLoading.value == true) return
        
        _isLoading.value = true
        searchPage = 1
        currentQuery = query
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = repository.searchApps(query, page = searchPage)
                searchTotalPages = repository.lastTotalPages ?: 1
                _searchResults.postValue(results.map { convertToUiModel(it) })
            } catch (e: Exception) {
                _errorMessage.postValue("搜索失败: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun searchNextPage() {
        if (_isLoading.value == true || searchPage >= searchTotalPages) return
        
        _isLoading.value = true
        searchPage++
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newResults = repository.searchApps(currentQuery, page = searchPage)
                searchTotalPages = repository.lastTotalPages ?: 1
                
                val currentList = _searchResults.value?.toMutableList() ?: mutableListOf()
                currentList.addAll(newResults.map { convertToUiModel(it) })
                
                _searchResults.postValue(currentList)
            } catch (e: Exception) {
                _errorMessage.postValue("加载更多失败: ${e.localizedMessage}")
                searchPage-- // 回退页码
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun cancelSearch() {
        _searchResults.value = emptyList()
    }
  
    private fun convertToUiModel(apiItem: RetrofitClient.models.AppItem): AppItem = AppItem(
        id = apiItem.id.toString(),
        name = apiItem.appname,
        iconUrl = apiItem.app_icon.replace("\\", ""),
        versionId = apiItem.apps_version_id
    )
}

// 增强的Repository（支持分页）
class PlazaRepository(private val context: Context) {
    private val api = RetrofitClient.instance
    var lastTotalPages: Int? = null // 保存最后一次请求的总页数

    suspend fun getNewShares(limit: Int = 4): List<RetrofitClient.models.AppItem> {
        return handleResponse(api.getAppsList(
            limit = limit,
            page = 1,
            sortOrder = "desc",
            sort = "update_time"
        ))
    }

    suspend fun getPopularApps(limit: Int = 8, page: Int = 1): List<RetrofitClient.models.AppItem> {
        return handleResponse(api.getAppsList(
            limit = limit,
            page = page,
            sortOrder = "desc"
        ))
    }
  
    suspend fun searchApps(query: String, page: Int = 1, limit: Int = 20): List<RetrofitClient.models.AppItem> {
        return handleResponse(api.getAppsList(
            limit = limit,
            page = page,
            appName = query
        ))
    }
  
    private suspend fun handleResponse(response: Response<AppListResponse>): List<RetrofitClient.models.AppItem> {
        return if (response.isSuccessful) {
            when (val body = response.body()) {
                null -> emptyList()
                else -> {
                    if (body.code == 1) {
                        // 保存总页数用于分页
                        lastTotalPages = body.data?.pagecount ?: 1
                        body.data?.list ?: emptyList()
                    } else {
                        android.util.Log.w("PlazaRepository", 
                            "API error: ${body.msg} (code=${body.code})")
                        emptyList()
                    }
                }
            }
        } else {
            android.util.Log.e("PlazaRepository", 
                "HTTP error: ${response.code()} - ${response.message()}")
            emptyList()
        }
    }
}

// 优化的适配器实现 - 使用DiffUtil提高性能
class AppHorizontalAdapter(
    private val context: Context,
    private val glide: RequestManager
) : RecyclerView.Adapter<AppHorizontalAdapter.ViewHolder>() {

    private val items = mutableListOf<AppItem>()
    private val diffCallback = AppItemDiffCallback()
    
    fun submitList(newList: List<AppItem>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return diffCallback.areItemsTheSame(items[oldItemPosition], newList[newItemPosition])
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return diffCallback.areContentsTheSame(items[oldItemPosition], newList[newItemPosition])
            }
        })
        
        items.clear()
        items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        val name: TextView = itemView.findViewById(R.id.tvAppName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_app_horizontal, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        glide.load(item.iconUrl)
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.ic_menu_apps)
                .circleCrop()
            )
            .into(holder.icon)
            
        holder.itemView.setOnClickListener {
            val intent = Intent(context, AppDetailActivity::class.java).apply {
                putExtra("APP_ID", item.id.toLong())
                putExtra("VERSION_ID", item.versionId)
            }
            context.startActivity(intent)
        }
    }    
    override fun getItemCount() = items.size
}

class AppGridAdapter(
    private val context: Context,
    private val glide: RequestManager
) : RecyclerView.Adapter<AppGridAdapter.ViewHolder>() {

    private val items = mutableListOf<AppItem>()
    private val diffCallback = AppItemDiffCallback()
    
    fun submitList(newList: List<AppItem>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return diffCallback.areItemsTheSame(items[oldItemPosition], newList[newItemPosition])
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return diffCallback.areContentsTheSame(items[oldItemPosition], newList[newItemPosition])
            }
        })
        
        items.clear()
        items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        val name: TextView = itemView.findViewById(R.id.tvAppName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_app_grid, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        glide.load(item.iconUrl)
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.ic_menu_apps)
                .circleCrop()
            )
            .into(holder.icon)
            
        holder.itemView.setOnClickListener {
            val intent = Intent(context, AppDetailActivity::class.java).apply {
                putExtra("APP_ID", item.id.toLong())
                putExtra("VERSION_ID", item.versionId)
            }
            context.startActivity(intent)
        }
    }
    
    override fun getItemCount(): Int = items.size
}