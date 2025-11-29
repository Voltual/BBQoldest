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
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.databinding.FragmentCommunityBinding
import cc.bbq.xq.oldest.ui.community.adapter.PostAdapter // 添加导入

class MyLikesFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
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
        
        communityManager = ViewModelProvider(this).get(CommunityManager::class.java)
        setupRecyclerView()
        setupObservers()
        loadData()
        
        binding.swipeRefresh.setOnRefreshListener {
            communityManager.reset()
            loadData()
        }
    }
    
    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostAdapter(requireContext()) // 使用正确的适配器
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount
                    
                    if (!communityManager.isLoading.value!! && lastVisibleItemPosition == totalItemCount - 1) {
                        loadData()
                    }
                }
            })
        }
    }
    
    private fun loadData() {
        communityManager.loadLikesRecords(requireContext())
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