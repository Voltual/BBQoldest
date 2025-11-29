package cc.bbq.xq.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.bbq.xq.AuthManager
import cc.bbq.xq.R
import cc.bbq.xq.databinding.ActivityMyPostsBinding

class MyPostsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPostsBinding
    private var userId: Long = -1L

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"
        
        fun start(context: Context, userId: Long) {
            val intent = Intent(context, MyPostsActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        if (userId == -1L) {
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 根据是否当前用户设置标题
        val currentUserId = AuthManager.getUserId(this)
        if (currentUserId == userId) {
            supportActionBar?.title = "我的帖子"
        } else {
            supportActionBar?.title = "用户帖子"
        }

        // 加载Fragment并传递用户ID
        val fragment = MyPostsFragment.newInstance(userId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}