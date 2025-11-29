package cc.bbq.xq.oldest

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HeartbeatService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var token: String
    private var heartbeatJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        token = intent?.getStringExtra("TOKEN") ?: return START_NOT_STICKY
        startHeartbeat()
        return START_STICKY
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel() // 取消之前的任务
        heartbeatJob = scope.launch {
            while (true) {
                try {
                    val response = RetrofitClient.instance.heartbeat(token = token)
                    Log.d("HEARTBEAT", "Status: ${response.code()}")
                } catch (e: Exception) {
                    Log.e("HEARTBEAT", "Failed: ${e.message}")
                }
                
                // 等待一分钟
                kotlinx.coroutines.delay(60 * 1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        heartbeatJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}