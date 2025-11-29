package cc.bbq.xq.oldest.ui.home

import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.content.Intent
import cc.bbq.xq.oldest.ui.message.MessageCenterActivity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cc.bbq.xq.oldest.ui.user.MyPostsActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cc.bbq.xq.oldest.AuthManager
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.RetrofitClient
import cc.bbq.xq.oldest.databinding.FragmentHomeBinding
import cc.bbq.xq.oldest.ui.user.FollowListActivity
import cc.bbq.xq.oldest.ui.user.FanListActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 检查登录状态
        val isLoggedIn = AuthManager.getCredentials(requireContext()) != null
        
        if (isLoggedIn) {
            showAuthenticatedContent()
            loadUserData()
        } else {
            showLoginPrompt()
        }
        
        // 设置头像点击事件
        binding.ivAvatar.setOnClickListener {
            if (!isLoggedIn) {
                findNavController().navigate(R.id.nav_login)
            }
        }
        
        // 消息中心点击事件
        binding.layoutMessageCenter.setOnClickListener {
            if (AuthManager.getCredentials(requireContext()) != null) {
                startActivity(Intent(requireContext(), MessageCenterActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.nav_login)
            }
        }
        
        // 浏览记录入口
        binding.layoutBrowseHistory.setOnClickListener {
            if (AuthManager.getCredentials(requireContext()) != null) {
                findNavController().navigate(R.id.nav_browse_history)
            } else {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.nav_login)
            }
        }
    
        // 我喜欢的入口
        binding.layoutMyLikes.setOnClickListener {
            if (AuthManager.getCredentials(requireContext()) != null) {
                findNavController().navigate(R.id.nav_my_likes)
            } else {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.nav_login)
            }
        }
        
        // ============== 新增绑定入口 ==============
        // 我的关注点击事件
        binding.layoutFollowers.setOnClickListener {
            if (AuthManager.getCredentials(requireContext()) != null) {
                FollowListActivity.start(requireContext())
            } else {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.nav_login)
            }
        }
        
        // 我的粉丝点击事件
        binding.layoutFans.setOnClickListener {
            if (AuthManager.getCredentials(requireContext()) != null) {
                FanListActivity.start(requireContext())
            } else {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.nav_login)
            }
        }
        
            // 2. 添加我的帖子点击事件
   binding.layoutPosts.setOnClickListener {
    val credentials = AuthManager.getCredentials(requireContext())
    if (credentials != null) {
        // 获取当前用户ID并传递给Activity
        val userId = AuthManager.getUserId(requireContext())
        if (userId != null) {
            MyPostsActivity.start(requireContext(), userId)
        } else {
            Toast.makeText(requireContext(), "无法获取用户ID", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.nav_login)
    }
}

        // =========================================
        
        // 设置滑动监听 - 方向已反转
        binding.root.setOnTouchListener(OnSwipeTouchListener(requireContext()).apply {
            onSwipeLeft = {  // 改为向左滑动触发
                findNavController().navigate(R.id.nav_community)
            }
        })
    }
    
    private fun showAuthenticatedContent() {
        binding.layoutLoginPrompt.visibility = View.GONE
        binding.layoutPersonalCenter.visibility = View.VISIBLE
        
        binding.tvNickname.text = "加载中..."
        binding.tvLevel.text = "LV0"
        binding.tvUserId.text = "ID: --"
        binding.tvCoins.text = "0"
        
        binding.tvFollowersCount.text = "0"
        binding.tvFansCount.text = "0"
        binding.tvPostsCount.text = "0"
        binding.tvLikesCount.text = "0"
    }
    
    private fun showLoginPrompt() {
        binding.layoutLoginPrompt.visibility = View.VISIBLE
        binding.layoutPersonalCenter.visibility = View.GONE
        
        binding.textPrompt.visibility = View.VISIBLE
        binding.btnLoginPrompt.visibility = View.VISIBLE
        
        binding.btnLoginPrompt.setOnClickListener {
            findNavController().navigate(R.id.nav_login)
        }
    }
    
    private fun loadUserData() {
        val credentials = AuthManager.getCredentials(requireContext()) ?: return
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getUserInfo(
                    token = credentials.third
                )
                
                if (response.isSuccessful && response.body()?.code == 1) {
                    response.body()?.data?.let { userData ->
                        updateUserUI(userData)
                    }
                } else {
                    showToast("获取用户信息失败: ${response.body()?.msg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                showToast("网络错误: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updateUserUI(userData: RetrofitClient.models.UserData) {
        Glide.with(this)
            .load(userData.usertx.replace("\\", ""))
            .circleCrop()
            .placeholder(R.drawable.ic_menu_profile)
            .into(binding.ivAvatar)
        
        binding.tvNickname.text = userData.nickname
        binding.tvLevel.text = userData.hierarchy
        binding.tvUserId.text = userData.username
        binding.tvCoins.text = userData.money.toString()
        
        binding.tvFollowersCount.text = userData.followerscount
        binding.tvFansCount.text = userData.fanscount
        binding.tvPostsCount.text = userData.postcount
        binding.tvLikesCount.text = userData.likecount
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// 修改后的滑动监听器 - 方向反转
class OnSwipeTouchListener(context: Context) : View.OnTouchListener {

    private val gestureDetector: GestureDetector
    var onSwipeLeft: (() -> Unit)? = null  // 改为左滑回调

    init {
        gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val diffX = e2.x - e1.x
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {  // 检测向左滑动 (X值减小)
                        onSwipeLeft?.invoke()
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
}