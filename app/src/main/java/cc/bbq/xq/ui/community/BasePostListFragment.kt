package cc.bbq.xq.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.bbq.xq.databinding.FragmentCommunityBinding
import cc.bbq.xq.ui.community.adapter.PostAdapter

abstract class BasePostListFragment : Fragment() {

    protected lateinit var binding: FragmentCommunityBinding
    protected lateinit var adapter: PostAdapter
    protected var loading = false
    protected var currentPage = 1
    protected var totalPages = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = PostAdapter(requireContext())
        
        setupRecyclerView()
        setupRefresh()
        loadInitialData()
    }

    protected fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BasePostListFragment.adapter
            
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

    protected fun setupRefresh() {
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
    abstract fun loadNextPage()

    protected fun handleLoadingState(isLoading: Boolean) {
        this.loading = isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.swipeRefresh.isRefreshing = false
    }

    protected fun handleError(message: String) {
        binding.errorText.text = message
        binding.errorLayout.visibility = View.VISIBLE
    }
}