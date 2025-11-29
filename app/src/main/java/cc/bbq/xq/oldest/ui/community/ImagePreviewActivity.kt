package cc.bbq.xq.oldest.ui.community

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import cc.bbq.xq.oldest.R
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class ImagePreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        
        val imageUrl = intent.getStringExtra("IMAGE_URL") ?: return finish()
        
        val photoView: PhotoView = findViewById(R.id.photo_view)
        val btnClose: ImageView = findViewById(R.id.btn_close)
        
        Glide.with(this)
            .load(imageUrl)
            .into(photoView)
        
        btnClose.setOnClickListener { finish() }
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }
}