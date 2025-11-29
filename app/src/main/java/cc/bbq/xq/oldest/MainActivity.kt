package cc.bbq.xq.oldest

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cc.bbq.xq.oldest.databinding.ActivityMainBinding
import cc.bbq.xq.oldest.ui.community.PostCreateActivity
import cc.bbq.xq.oldest.ui.community.SearchActivity // 添加正确的导入
import cc.bbq.xq.oldest.ui.message.MessageCenterActivity
import com.google.android.material.navigation.NavigationView
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val authScope = CoroutineScope(Dispatchers.IO + Job())
    private var loginRetryCount = 0 // 登录重试计数器
    private val MAX_LOGIN_RETRY = 1 // 最大重试次数

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        binding.appBarMain.fab.visibility = View.GONE

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, 
                R.id.nav_market, 
                R.id.nav_messages, // 添加消息中心
                R.id.nav_profile
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        
        // 添加导航菜单点击监听器
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_messages -> {
                    if (AuthManager.getCredentials(this) != null) {
                        startActivity(Intent(this, MessageCenterActivity::class.java))
                    } else {
                        Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
                        navToLogin()
                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                
                R.id.search -> { // 搜索入口
                    startActivity(Intent(this, SearchActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    AuthManager.clearCredentials(this)
                    Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
                    navToLogin()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    // 默认导航行为 - 直接使用 NavController 导航
                    menuItem.isChecked = true
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    
                    // 使用 NavController 导航到目标
                    try {
                        navController.navigate(menuItem.itemId)
                    } catch (e: IllegalArgumentException) {
                        // 处理无法导航的情况
                        e.printStackTrace()
                    }
                    true
                }
            }
        }
        
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        authScope.launch {
            val credentials = AuthManager.getCredentials(this@MainActivity)
            if (credentials != null) {
                tryAutoLogin(credentials.first, credentials.second)
            } else {
                withContext(Dispatchers.Main) {
                    navToLogin()
                }
            }
        }
    }
    
    private suspend fun tryAutoLogin(username: String, password: String) {
        try {
            val deviceId = AuthManager.getDeviceId(this@MainActivity)
            val response = RetrofitClient.instance.login(
                username = username,
                password = password,
                device = deviceId
            )
            
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        when (body.code) {
                            1 -> {
                                val loginData = body.data
                                if (loginData != null) {
                                    AuthManager.saveCredentials(
                                        this@MainActivity,
                                        username,
                                        password,
                                        loginData.usertoken,
                                        loginData.id // 从登录响应中获取userid
                                    )
                                    startHeartbeatService(loginData.usertoken)
                                    loginRetryCount = 0 // 登录成功后重置重试计数器
                                } else {
                                    showLoginError("自动登录成功但数据错误")
                                    navToLogin()
                                }
                            }
                            0 -> {
                                showLoginError(body.msg)
                                navToLogin()
                            }
                            else -> {
                                showLoginError("未知响应: ${body.code}")
                                navToLogin()
                            }
                        }
                    } else {
                        showLoginError("服务器未返回数据")
                        navToLogin()
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        429 -> "请求太频繁"
                        500 -> "服务器错误"
                        else -> "网络错误: ${response.code()}"
                    }
                    showLoginError(errorMsg)
                    navToLogin()
                }
            }
        } catch (e: Exception) {
            // 检测特定JSON解析错误
            if (e is JsonSyntaxException || 
                (e is IllegalStateException && e.message?.contains("Expected BEGIN_OBJECT but was BEGIN_ARRAY") == true)) {
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity, 
                        "检测到顶号状态，尝试自动重新登录...", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                // 增加重试计数器
                loginRetryCount++
                
                if (loginRetryCount <= MAX_LOGIN_RETRY) {
                    // 延迟1.5秒后重试
                    delay(1500)
                    tryAutoLogin(username, password)
                } else {
                    // 超过最大重试次数，跳转到登录界面
                    withContext(Dispatchers.Main) {
                        showLoginError("自动重试失败，请手动登录")
                        navToLogin()
                    }
                }
            } else {
                // 其他异常正常处理
                withContext(Dispatchers.Main) {
                    showLoginError("网络异常: ${e.message}")
                    navToLogin()
                }
            }
        }
    }
    
    private fun navToLogin() {
        findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.nav_login)
    }
    
    private fun showLoginError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    fun startHeartbeatService(token: String) {
        Intent(this, HeartbeatService::class.java).apply {
            putExtra("TOKEN", token)
            startService(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> { // 顶部工具栏搜索图标
                startActivity(Intent(this, SearchActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                return true
            }
            R.id.action_create_post -> {
                if (AuthManager.getCredentials(this) != null) {
                    startActivity(Intent(this, PostCreateActivity::class.java))
                } else {
                    Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
                    navToLogin()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        authScope.cancel()
    }
}