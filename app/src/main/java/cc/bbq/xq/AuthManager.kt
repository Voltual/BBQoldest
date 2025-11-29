package cc.bbq.xq

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit

object AuthManager {
    private const val PREFS_NAME = "bbq_auth"
    private const val KEY_TOKEN = "usertoken"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_DEVICE = "device_id"
    private const val KEY_USER_ID = "userid" // 新增userid存储键

    // 修改保存凭证方法，增加userid参数
    fun saveCredentials(
        context: Context, 
        username: String, 
        password: String, 
        token: String,
        userId: Long // 新增userId参数
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_USERNAME, Base64.encodeToString(username.toByteArray(), Base64.DEFAULT))
            putString(KEY_PASSWORD, Base64.encodeToString(password.toByteArray(), Base64.DEFAULT))
            putString(KEY_TOKEN, token)
            putLong(KEY_USER_ID, userId) // 存储 userid
            putString(KEY_DEVICE, generateDeviceId())
        }
    }

    // 修改获取凭证方法，返回包含userid的四元组
fun getCredentials(context: Context): Quadruple<String, String, String, Long>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encodedUser = prefs.getString(KEY_USERNAME, null)
        val encodedPass = prefs.getString(KEY_PASSWORD, null)
        val token = prefs.getString(KEY_TOKEN, null)
        val userId = prefs.getLong(KEY_USER_ID, -1)
        
        return if (encodedUser != null && encodedPass != null && token != null && userId != -1L) {
            val username = String(Base64.decode(encodedUser, Base64.DEFAULT))
            val password = String(Base64.decode(encodedPass, Base64.DEFAULT))
            Quadruple(username, password, token, userId)
        } else {
            null
        }
    }


    // 新增方法：单独获取userid
    fun getUserId(context: Context): Long? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getLong(KEY_USER_ID, -1)
        return if (userId != -1L) userId else null
    }

    fun clearCredentials(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            clear()
        }
    }

    fun getDeviceId(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DEVICE, generateDeviceId()) ?: generateDeviceId()
    }

    private fun generateDeviceId(): String {
        return (1..15).joinToString("") { (0..9).random().toString() }
    }
}

// 新增四元组数据类，用于返回四个值
data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)