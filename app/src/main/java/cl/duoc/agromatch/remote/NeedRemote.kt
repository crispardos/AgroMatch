package cl.duoc.agromatch.remote

data class NeedRemote(
    val id: Int = 0,
    val farmerId: Int,
    val cropCode: String,
    val hectares: Double
)
data class PatchNeedRequest(
    val hectares: Double? = null,
    val cropCode: String? = null
)