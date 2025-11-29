package cc.bbq.xq.ui.user

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import cc.bbq.xq.AuthManager
import cc.bbq.xq.R
import cc.bbq.xq.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FollowListActivity : BaseUserListActivity() {

    override fun loadInitialData() {
        loadData()
    }

    override fun loadData() {
        handleLoadingState(true)
        binding.errorLayout.visibility = View.GONE
        
        val credentials = AuthManager.getCredentials(this) ?: run {
            handleError(getString(R.string.error_not_logged_in))
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userManager.getFollowList(
                    token = credentials.third,
                    page = currentPage
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    response.body()?.data?.let { data ->
                        runOnUiThread {
                            handleDataLoaded(data)
                        }
                    } ?: runOnUiThread {
                        handleError(getString(R.string.error_data_invalid))
                    }
                } else {
                    runOnUiThread {
                        handleError(response.body()?.msg ?: getString(R.string.error_unknown))
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    handleError(e.message ?: getString(R.string.error_network))
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, FollowListActivity::class.java))
        }
    }
}