package cc.bbq.xq.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.bbq.xq.CommunityManager
import cc.bbq.xq.databinding.FragmentCommunityBinding
import cc.bbq.xq.ui.community.adapter.PostAdapter // 添加导入

class SearchResultFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private lateinit var communityManager: CommunityManager
    private var currentQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, // 修复导入问题
        container: ViewGroup?, // 修复导入问题
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        communityManager = ViewModelProvider(this).get(CommunityManager::class.java)
        setupRecyclerView()
        setupObservers()
        
        binding.swipeRefresh.setOnRefreshListener {
            communityManager.reset()
            performSearch(currentQuery)
        }
    }
    
    fun search(query: String) {
        currentQuery = query
        binding.errorLayout.visibility = View.GONE
        communityManager.reset()
        performSearch(query)
    }
    
private fun performSearch(query: String) {
    if (query.isEmpty()) return
    communityManager.searchPosts(query) // 使用修复后的方法签名
}

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostAdapter(requireContext()) // 使用正确的适配器
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView, 
                    dx: Int, 
                    dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy) // 修复覆盖问题
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount
                    
                    if (!communityManager.isLoading.value!! && lastVisibleItemPosition == totalItemCount - 1) {
                        performSearch(currentQuery)
                    }
                }
            })
        }
    }
    
    private fun setupObservers() {
        communityManager.posts.observe(viewLifecycleOwner) { posts ->
            (binding.recyclerView.adapter as PostAdapter).submitList(posts) // 修复submitList调用
            binding.swipeRefresh.isRefreshing = false
        }
        
        communityManager.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        communityManager.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                binding.errorText.text = message
                binding.errorLayout.visibility = View.VISIBLE
            } else {
                binding.errorLayout.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}