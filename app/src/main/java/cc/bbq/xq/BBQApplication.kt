package cc.bbq.xq

import android.app.Application
import androidx.multidex.MultiDex

class BBQApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }
}