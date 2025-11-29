package cc.bbq.xq.oldest.ui.community

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cc.bbq.xq.oldest.AuthManager
import cc.bbq.xq.oldest.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response

class PostDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _postDetail = MutableLiveData<RetrofitClient.models.PostDetail?>()
    val postDetail: LiveData<RetrofitClient.models.PostDetail?> = _postDetail

    private val _comments = MutableLiveData<List<RetrofitClient.models.Comment>>()
    val comments: LiveData<List<RetrofitClient.models.Comment>> = _comments

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _replySuccess = MutableLiveData<Boolean>()
    val replySuccess: LiveData<Boolean> = _replySuccess

    private val _commentSuccess = MutableLiveData<Boolean>()
    val commentSuccess: LiveData<Boolean> = _commentSuccess
    
    private val _likeSuccess = MutableLiveData<Boolean>()
    val likeSuccess: LiveData<Boolean> = _likeSuccess
    
    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    fun loadPostDetail(postId: Long) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val credentials = AuthManager.getCredentials(context) ?: run {
                    _errorMessage.postValue("请先登录")
                    return@launch
                }
                
                val response = RetrofitClient.instance.getPostDetail(
                    token = credentials.third,
                    postId = postId
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    _postDetail.postValue(response.body()?.data)
                    _errorMessage.postValue("")
                } else {
                    _errorMessage.postValue("加载失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("网络错误: ${e.message}")
            }
        }
    }
    
    fun loadComments(postId: Long, page: Int = 1) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getPostComments(
                    postId = postId,
                    limit = 20,
                    page = page
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    _comments.postValue(response.body()?.data?.list ?: emptyList())
                }
            } catch (e: Exception) {
                // 评论加载失败不影响主内容显示
            }
        }
    }
    
    fun postReply(postId: Long, commentId: Long, content: String, imageUrl: String = "") {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val credentials = AuthManager.getCredentials(context) ?: run {
                    _errorMessage.postValue("请先登录")
                    return@launch
                }
              
                val response = RetrofitClient.instance.postComment(
                    token = credentials.third,
                    content = content,
                    postId = postId,
                    parentId = commentId,
                    imageUrl = imageUrl
                )
              
                if (response.isSuccessful && response.body()?.code == 1) {
                    _replySuccess.postValue(true)
                } else {
                    _errorMessage.postValue("回复失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("网络错误: ${e.message}")
            }
        }
    }
  
    fun postComment(postId: Long, content: String, imageUrl: String = "") {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val credentials = AuthManager.getCredentials(context) ?: run {
                    _errorMessage.postValue("请先登录")
                    return@launch
                }
              
                val response = RetrofitClient.instance.postComment(
                    token = credentials.third,
                    content = content,
                    postId = postId,
                    parentId = 0, // 设置为0表示评论帖子
                    imageUrl = imageUrl
                )
              
                if (response.isSuccessful && response.body()?.code == 1) {
                    _commentSuccess.postValue(true)
                } else {
                    _errorMessage.postValue("评论失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("网络错误: ${e.message}")
            }
        }
    }
    
    fun toggleLike(postId: Long, isLiked: Boolean) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val credentials = AuthManager.getCredentials(context) ?: run {
                    _errorMessage.postValue("请先登录")
                    return@launch
                }
                
                // 点赞/取消点赞使用相同的API
                val response = RetrofitClient.instance.likePost(
                    token = credentials.third,
                    postId = postId
                )
                
            if (response.isSuccessful && response.body()?.code == 1) {
                _likeSuccess.postValue(true)
            } else {
                val action = if (isLiked) "Unlike" else "Like"  // 修改这里
                _errorMessage.postValue("$action failed: ${response.body()?.msg ?: "Unknown error"}")
            }
        } catch (e: Exception) {
            val action = if (isLiked) "Unlike" else "Like"  // 修改这里
            _errorMessage.postValue("$action failed: ${e.message}")
        }
    }
}
    
    fun deletePost(postId: Long) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val credentials = AuthManager.getCredentials(context) ?: run {
                    _errorMessage.postValue("请先登录")
                    return@launch
                }
                
                val response = RetrofitClient.instance.deletePost(
                    token = credentials.third,
                    postId = postId
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    _deleteSuccess.postValue(true)
                } else {
                    _errorMessage.postValue("删除失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("删除失败: ${e.message}")
            }
        }
    }
}