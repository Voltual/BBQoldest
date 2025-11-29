package cc.bbq.xq.oldest.ui.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.bbq.xq.oldest.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<RetrofitClient.models.MessageNotification>>(emptyList())
    val messages: StateFlow<List<RetrofitClient.models.MessageNotification>> = _messages
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage
    
    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages
    
    fun loadMessages(token: String, page: Int = 1) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getMessageNotifications(
                    token = token,
                    limit = 5,
                    page = page
                )
                
                if (response.isSuccessful) {
                    response.body()?.data?.let { data ->
                        _messages.value = data.list
                        _currentPage.value = data.current_number
                        _totalPages.value = data.pagecount
                    }
                } else {
                    _error.value = "加载失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadNextPage(token: String) {
        if (_currentPage.value < _totalPages.value) {
            loadMessages(token, _currentPage.value + 1)
        }
    }
}