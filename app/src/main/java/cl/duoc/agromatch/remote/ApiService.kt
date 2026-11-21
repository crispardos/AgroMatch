package cl.duoc.agromatch.remote

import retrofit2.http.*

interface ApiService {
    @GET("get-needs")
    suspend fun getNeedsCustom(): List<NeedRemote>

    @POST("create-need")
    suspend fun createNeedCustom(@Body need: NeedRemote): NeedRemote

    @GET("needs/{id}")
    suspend fun getNeedById(@Path("id") id: Int): NeedRemote

    @PUT("needs/{id}")
    suspend fun updateNeed(@Path("id") id: Int, @Body need: NeedRemote): NeedRemote

    @PATCH("needs/{id}")
    suspend fun patchNeed(@Path("id") id: Int, @Body need: PatchNeedRequest): NeedRemote

    @DELETE("needs/{id}")
    suspend fun deleteNeed(@Path("id") id: Int)

    @GET("status")
    suspend fun checkStatus(): String
}