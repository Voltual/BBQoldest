package cc.bbq.xq.oldest.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.bbq.xq.oldest.CommunityManager
import cc.bbq.xq.oldest.databinding.FragmentCommunityBinding
import cc.bbq.xq.oldest.ui.community.adapter.PostAdapter

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostAdapter
    private var loading = false
    private lateinit var communityManager: CommunityManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = PostAdapter(requireContext())
        communityManager = ViewModelProvider(this).get(CommunityManager::class.java)
        
        setupRecyclerView()
        setupObservers()
        loadData()
        
        binding.swipeRefresh.setOnRefreshListener {
            communityManager.reset() // 修复：使用正确的 reset 方法
            loadData()
        }
    }
    
    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CommunityFragment.adapter
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount
                    
                    if (!loading && lastVisibleItemPosition == totalItemCount - 1) {
                        loadNextPage()
                    }
                }
            })
        }
    }
    
    private fun loadNextPage() {
        loading = true
        communityManager.loadPosts(requireContext()) // 修复：使用正确的 loadPosts 方法
    }
    
    private fun setupObservers() {
        // 修复：使用正确的 LiveData 引用
        communityManager.posts.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
            binding.swipeRefresh.isRefreshing = false
            loading = false
        }
        
        communityManager.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            loading = isLoading
        }
        
        communityManager.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                binding.errorText.text = message
                binding.errorLayout.visibility = View.VISIBLE
                loading = false
            } else {
                binding.errorLayout.visibility = View.GONE
            }
        }
    }
    
    private fun loadData() {
        communityManager.loadPosts(requireContext()) // 修复：使用正确的 loadPosts 方法
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}