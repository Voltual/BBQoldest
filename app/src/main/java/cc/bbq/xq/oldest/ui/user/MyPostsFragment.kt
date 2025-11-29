package cc.bbq.xq.oldest.ui.user

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cc.bbq.xq.oldest.AuthManager
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.RetrofitClient
import cc.bbq.xq.oldest.ui.community.BasePostListFragment
import kotlinx.coroutines.launch

class MyPostsFragment : BasePostListFragment() {

    private var userId: Long = -1L

    companion object {
        private const val ARG_USER_ID = "user_id"
        
        fun newInstance(userId: Long): MyPostsFragment {
            return MyPostsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USER_ID, userId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USER_ID, -1L)
        }
    }

    override fun loadInitialData() {
        loadData()
    }

    override fun loadData() {
        lifecycleScope.launch {
            try {
                handleLoadingState(true)
                
                if (userId == -1L) {
                    handleError("用户ID无效")
                    return@launch
                }
                
                val token = AuthManager.getCredentials(requireContext())?.third
                
                // 使用正确的 API 调用
                val response = RetrofitClient.instance.getPostsList(
                    limit = 10,
                    page = currentPage,
                    userId = userId
                )
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1 && body.data != null) {
                        totalPages = body.data.pagecount
                        
                        val posts = body.data.list.map { post ->
                            RetrofitClient.models.Post(
                                postid = post.postid,
                                title = post.title,
                                content = post.content,
                                userid = post.userid,
                                create_time = post.create_time,
                                update_time = post.update_time,
                                username = post.username,
                                nickname = post.nickname,
                                usertx = post.usertx,
                                hierarchy = post.hierarchy,
                                section_name = post.section_name,
                                sub_section_name = post.sub_section_name,
                                view = post.view,
                                thumbs = post.thumbs,
                                comment = post.comment,
                                img_url = post.img_url
                            )
                        }
                        
                        if (currentPage == 1) {
                            adapter.submitList(posts)
                        } else {
                            val currentList = adapter.currentList.toMutableList()
                            currentList.addAll(posts)
                            adapter.submitList(currentList)
                        }
                        
                        binding.errorLayout.visibility = View.GONE
                    } else {
                        handleError(body?.msg ?: "未知错误")
                    }
                } else {
                    handleError("网络错误: ${response.code()}")
                }
            } catch (e: Exception) {
                handleError("请求失败: ${e.message}")
            } finally {
                handleLoadingState(false)
            }
        }
    }

    override fun loadNextPage() {
        if (currentPage < totalPages) {
            currentPage++
            loadData()
        }
    }
}