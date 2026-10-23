package cl.duoc.agromatch.repository

import kotlinx.coroutines.runBlocking
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    private val hasher = PasswordHasher(iterations = 10_000)

    @Test
    fun credentialsVerifyOnlyForTheOriginalPassword() = runBlocking {
        val credentials = hasher.createCredentials("AgroMatch123")

        assertTrue(hasher.verify("AgroMatch123", credentials))
        assertFalse(hasher.verify("AgroMatch124", credentials))
    }

    @Test
    fun eachAccountReceivesDifferentSaltAndHash() = runBlocking {
        val first = hasher.createCredentials("AgroMatch123")
        val second = hasher.createCredentials("AgroMatch123")

        assertNotEquals(first.salt, second.salt)
        assertNotEquals(first.hash, second.hash)
    }

    @Test
    fun derivedHashMatchesTheJdkPbkdf2Implementation() = runBlocking {
        val password = "AgroMatch123"
        val credentials = hasher.createCredentials(password)
        val salt = credentials.salt.hexToBytes()
        val expected = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(password.toCharArray(), salt, 10_000, 256))
            .encoded

        assertArrayEquals(expected, credentials.hash.hexToBytes())
    }

    private fun String.hexToBytes(): ByteArray = ByteArray(length / 2) { index ->
        substring(index * 2, index * 2 + 2).toInt(16).toByte()
    }
}
