package cc.bbq.xq.oldest.utils

import android.content.Context
import android.view.WindowManager

object DimensionUtils {
    fun getScreenWidth(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels
    }
}