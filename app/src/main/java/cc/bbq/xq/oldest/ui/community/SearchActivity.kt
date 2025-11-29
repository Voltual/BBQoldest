package cc.bbq.xq.oldest.ui.community

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import cc.bbq.xq.oldest.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchFragment: SearchResultFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSearchView()
        initFragment()
        
        // 处理搜索意图（当用户从其他应用启动搜索时）
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            query?.let {
                performSearch(it)
            }
        }
    }

    private fun setupSearchView() {
        val searchManager = getSystemService(SearchManager::class.java)
        binding.searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(false)
            requestFocus()
            
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    performSearch(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    // 实时搜索建议（可选）
                    return false
                }
            })
        }
    }

    private fun initFragment() {
        searchFragment = SearchResultFragment()
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, searchFragment)
            .commit()
    }

    private fun performSearch(query: String) {
        searchFragment.search(query)
        // 保存搜索历史（使用SharedPreferences或Room）
    }
}