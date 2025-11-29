package cc.bbq.xq

data class HeartbeatResponse(
    val code: Int,
    val msg: String,
    val data: List<Any>,
    val timestamp: Long
)