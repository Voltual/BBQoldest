package cc.bbq.xq

import androidx.lifecycle.ViewModel
import cc.bbq.xq.RetrofitClient
import retrofit2.Response

class UserManager : ViewModel() {

    suspend fun getFanList(
        token: String,
        page: Int
    ): Response<RetrofitClient.models.UserListResponse> {
        return RetrofitClient.instance.getFanList(
            token = token,
            page = page
        )
    }

    suspend fun getFollowList(
        token: String,
        page: Int
    ): Response<RetrofitClient.models.UserListResponse> {
        return RetrofitClient.instance.getFollowList(
            token = token,
            page = page
        )
    }
}