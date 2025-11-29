package cc.bbq.xq.oldest.ui.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cc.bbq.xq.oldest.AuthManager
import cc.bbq.xq.oldest.RetrofitClient
import kotlinx.coroutines.launch

class UserDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _userData = MutableLiveData<RetrofitClient.models.UserInformationData>()
    val userData: LiveData<RetrofitClient.models.UserInformationData> = _userData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadUserDetails(userId: Long) {
        // 使用 getApplication() 获取 Application 实例
        val context = getApplication<Application>()
        val credentials = AuthManager.getCredentials(context) ?: run {
            _errorMessage.postValue("请先登录")
            return
        }

        _isLoading.postValue(true)
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getUserInformation(
                    userId = userId,
                    token = credentials.third
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    response.body()?.data?.let {
                        _userData.postValue(it)
                        _errorMessage.postValue("")
                    }
                } else {
                    _errorMessage.postValue("加载失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("网络错误: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
