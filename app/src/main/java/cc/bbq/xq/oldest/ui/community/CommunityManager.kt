package cc.bbq.xq.oldest

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class CommunityManager : ViewModel() {
    companion object {
        private const val PAGE_SIZE = 10
    }
    
    private var currentPage = 1
    private var totalPages = 1

    val posts = MutableLiveData<List<RetrofitClient.models.Post>>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    fun loadPosts(context: Context, sectionId: Int? = null, refresh: Boolean = false) {
        if (isLoading.value == true) return
        
        // 修复：移除未使用的 credentials 变量
        AuthManager.getCredentials(context) ?: run {
            errorMessage.postValue("请先登录")
            return
        }
        
        if (refresh) {
            currentPage = 1
        }
        
        if (currentPage > totalPages) {
            errorMessage.postValue("没有更多内容了")
            return
        }
        
        isLoading.postValue(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getPostsList(
                    limit = PAGE_SIZE,
                    page = currentPage,
                    sectionId = sectionId
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    response.body()?.data?.let { data ->
                        totalPages = data.pagecount
                        currentPage++ // 成功加载后页码+1
                        
                        if (refresh) {
                            posts.postValue(data.list)
                        } else {
                            val currentList = posts.value?.toMutableList() ?: mutableListOf()
                            currentList.addAll(data.list)
                            posts.postValue(currentList)
                        }
                        errorMessage.postValue("") // 清除错误信息
                    }
                } else {
                    errorMessage.postValue("加载失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                errorMessage.postValue("网络错误: ${e.message}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }
    
    fun loadBrowseHistory(context: Context) {
        if (isLoading.value == true) return
        
        val credentials = AuthManager.getCredentials(context) ?: run {
            errorMessage.postValue("请先登录")
            return
        }
        
        isLoading.postValue(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getBrowseHistory(
                    token = credentials.third,
                    page = currentPage
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    response.body()?.data?.let { data ->
                        totalPages = data.pagecount
                        val historyPosts = data.list.map { it.toPost() }
                        
                        if (currentPage == 1) {
                            posts.postValue(historyPosts)
                        } else {
                            val currentList = posts.value?.toMutableList() ?: mutableListOf()
                            currentList.addAll(historyPosts)
                            posts.postValue(currentList)
                        }
                        currentPage++
                        errorMessage.postValue("")
                    }
                } else {
                    errorMessage.postValue("加载失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                errorMessage.postValue("网络错误: ${e.message}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }
    
    fun loadLikesRecords(context: Context) {
        if (isLoading.value == true) return
        
        val credentials = AuthManager.getCredentials(context) ?: run {
            errorMessage.postValue("请先登录")
            return
        }
        
        isLoading.postValue(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getLikesRecords(
                    token = credentials.third,
                    page = currentPage
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    response.body()?.data?.let { data ->
                        totalPages = data.pagecount
                        
                        if (currentPage == 1) {
                            posts.postValue(data.list)
                        } else {
                            val currentList = posts.value?.toMutableList() ?: mutableListOf()
                            currentList.addAll(data.list)
                            posts.postValue(currentList)
                        }
                        currentPage++
                        errorMessage.postValue("")
                    }
                } else {
                    errorMessage.postValue("加载失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                errorMessage.postValue("网络错误: ${e.message}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }
    
    // 修复：移除未使用的 context 参数
    fun searchPosts(query: String) {
        if (isLoading.value == true) return
        
        isLoading.postValue(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.searchPosts(
                    query = query,
                    page = currentPage
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    response.body()?.data?.let { data ->
                        totalPages = data.pagecount
                        
                        if (currentPage == 1) {
                            posts.postValue(data.list)
                        } else {
                            val currentList = posts.value?.toMutableList() ?: mutableListOf()
                            currentList.addAll(data.list)
                            posts.postValue(currentList)
                        }
                        currentPage++
                        errorMessage.postValue("")
                    }
                } else {
                    errorMessage.postValue("搜索失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                errorMessage.postValue("搜索错误: ${e.message}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }
    
    fun reset() {
        currentPage = 1
        totalPages = 1
        posts.value = emptyList()
        errorMessage.value = ""
    }
}

// 添加扩展函数
private fun RetrofitClient.models.BrowseHistoryItem.toPost(): RetrofitClient.models.Post {
    return RetrofitClient.models.Post(
        postid = this.postid,
        title = this.title,
        content = this.content,
        userid = this.userid,
        create_time = this.create_time,
        update_time = this.update_time,
        username = this.username,
        nickname = this.nickname,
        usertx = this.usertx,
        hierarchy = this.hierarchy,
        section_name = this.section_name,
        sub_section_name = this.sub_section_name,
        view = this.view,
        thumbs = this.thumbs,
        comment = this.comment,
        img_url = this.img_url
    )
}