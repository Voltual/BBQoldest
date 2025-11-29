package cc.bbq.xq.oldest

import android.app.Application
import androidx.multidex.MultiDex

class BBQApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }
}