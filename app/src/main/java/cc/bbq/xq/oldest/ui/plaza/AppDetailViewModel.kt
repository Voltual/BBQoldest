package cc.bbq.xq.oldest.ui.plaza

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cc.bbq.xq.oldest.AuthManager
import cc.bbq.xq.oldest.RetrofitClient
import cc.bbq.xq.oldest.RetrofitClient.models.AppComment
import kotlinx.coroutines.launch
import retrofit2.Response

class AppDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _appDetail = MutableLiveData<RetrofitClient.models.AppDetail?>()
    val appDetail: LiveData<RetrofitClient.models.AppDetail?> = _appDetail

    private val _comments = MutableLiveData<List<AppComment>>()
    val comments: LiveData<List<AppComment>> = _comments

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadAppDetail(appId: Long, versionId: Long) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val credentials = AuthManager.getCredentials(context)
                val token = credentials?.third ?: ""

                val response = RetrofitClient.instance.getAppsInformation(
                    token = token,
                    appsId = appId,
                    appsVersionId = versionId
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    _appDetail.postValue(response.body()?.data)
                    _errorMessage.postValue("")
                } else {
                    _errorMessage.postValue("加载失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("网络错误: ${e.message}")
            }
        }
    }
    
    fun loadComments(appId: Long, versionId: Long, page: Int = 1) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAppsCommentList(
                    appsId = appId,
                    appsVersionId = versionId,
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
}