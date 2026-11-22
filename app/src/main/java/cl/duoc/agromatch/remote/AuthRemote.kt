package cl.duoc.agromatch.remote


data class AuthRequest(
    val alias: String,
    val password: String,
    val role: String = ""
)

data class XanoUser(
    val id: Int,
    val alias: String? = null,
    val role: String? = null
)


data class AuthResponse(
    val authToken: String,
    val user: XanoUser
)