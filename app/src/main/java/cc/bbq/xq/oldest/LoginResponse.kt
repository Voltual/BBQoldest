package cc.bbq.xq.oldest

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val code: Int,
    val msg: String,
    
    // 使用JsonElement类型来接收任何格式的data
    @SerializedName("data")
    val rawData: JsonElement? = null,
    
    val timestamp: Long
) {
    // 安全解析登录数据
    fun getLoginData(): LoginData? {
        return try {
            // 尝试解析为LoginData对象
            rawData?.let { 
                Gson().fromJson(it, LoginData::class.java) 
            }
        } catch (e: Exception) {
            // 解析失败时返回null
            null
        }
    }
}

data class LoginData(
    val id: Long,
    val username: String,
    val usertoken: String,
    val is_section_moderator: Int
)
