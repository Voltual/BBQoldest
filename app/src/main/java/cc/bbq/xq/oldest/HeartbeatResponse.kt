package cc.bbq.xq.oldest

data class HeartbeatResponse(
    val code: Int,
    val msg: String,
    val data: List<Any>,
    val timestamp: Long
)