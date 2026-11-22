package cl.duoc.agromatch.remote

data class NeedRemote(
    val id: Int = 0,
    val farmerId: Int,
    val cropCode: String,
    val zoneCode: String = "COLCHAGUA",
    val hectares: Double,
    val startEpochDay: Long = 20000L,
    val endEpochDay: Long = 20004L
)

data class PatchNeedRequest(
    val hectares: Double? = null,
    val cropCode: String? = null
)