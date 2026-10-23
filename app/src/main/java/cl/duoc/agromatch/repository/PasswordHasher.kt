package cl.duoc.agromatch.repository

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PasswordCredentials(
    val hash: String,
    val salt: String
)

class PasswordHasher(
    private val iterations: Int = DEFAULT_ITERATIONS,
    private val random: SecureRandom = SecureRandom()
) {
    init {
        require(iterations >= MIN_ITERATIONS)
    }

    suspend fun createCredentials(password: String): PasswordCredentials =
        withContext(Dispatchers.Default) {
            val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
            val derivedKey = derive(password, salt)
            PasswordCredentials(encode(derivedKey), encode(salt))
        }

    suspend fun verify(password: String, credentials: PasswordCredentials): Boolean =
        withContext(Dispatchers.Default) {
            val expectedHash = decodeOrNull(credentials.hash) ?: return@withContext false
            val salt = decodeOrNull(credentials.salt) ?: return@withContext false
            val actualHash = derive(password, salt)
            MessageDigest.isEqual(expectedHash, actualHash)
        }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        try {
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(SecretKeySpec(passwordBytes, HMAC_ALGORITHM))

            val firstBlockInput = ByteArray(salt.size + 4)
            salt.copyInto(firstBlockInput)
            firstBlockInput[firstBlockInput.lastIndex] = 1

            var u = mac.doFinal(firstBlockInput)
            val result = u.copyOf()
            repeat(iterations - 1) {
                u = mac.doFinal(u)
                for (index in result.indices) {
                    result[index] = (result[index].toInt() xor u[index].toInt()).toByte()
                }
            }
            return result
        } finally {
            passwordBytes.fill(0)
        }
    }

    private fun encode(value: ByteArray): String = buildString(value.size * 2) {
        value.forEach { byte ->
            val unsigned = byte.toInt() and 0xff
            append(HEX_DIGITS[unsigned ushr 4])
            append(HEX_DIGITS[unsigned and 0x0f])
        }
    }

    private fun decodeOrNull(value: String): ByteArray? =
        try {
            if (value.length % 2 != 0) {
                null
            } else {
                ByteArray(value.length / 2) { index ->
                    val high = value[index * 2].digitToInt(16)
                    val low = value[index * 2 + 1].digitToInt(16)
                    ((high shl 4) or low).toByte()
                }
            }
        } catch (_: IllegalArgumentException) {
            null
        }

    companion object {
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val SALT_BYTES = 16
        private const val HEX_DIGITS = "0123456789abcdef"
        const val DEFAULT_ITERATIONS = 600_000
        private const val MIN_ITERATIONS = 10_000
    }
}
