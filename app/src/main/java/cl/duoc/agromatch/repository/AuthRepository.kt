package cl.duoc.agromatch.repository

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteConstraintException
import cl.duoc.agromatch.database.DemoDataSeeder
import cl.duoc.agromatch.database.PilotDemoSeeder
import cl.duoc.agromatch.database.UserDao
import cl.duoc.agromatch.model.User
import cl.duoc.agromatch.model.UserRole
import kotlinx.coroutines.CancellationException
import java.util.Locale

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Failure(val reason: AuthFailure) : AuthResult
}

enum class AuthFailure {
    INVALID_FIELDS,
    ALIAS_ALREADY_EXISTS,
    INVALID_CREDENTIALS,
    STORAGE_ERROR
}

class AuthRepository(
    context: Context,
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher = PasswordHasher()
) {
    private val preferences: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val seeder = DemoDataSeeder(userDao, passwordHasher)

    suspend fun initialize(): User? {
        seeder.seedAdminIfMissing()
        val pilotSeeder = PilotDemoSeeder(userDao, passwordHasher)
        pilotSeeder.seedPilotsIfMissing()
        return restoreSession()
    }

    suspend fun register(aliasInput: String, password: String, role: UserRole): AuthResult {
        val alias = aliasInput.trim()
        if (!isValidAlias(alias) || !isValidPassword(password) || role == UserRole.ADMIN) {
            return AuthResult.Failure(AuthFailure.INVALID_FIELDS)
        }

        val aliasKey = normalizeAlias(alias)
        if (userDao.findByAliasKey(aliasKey) != null) {
            return AuthResult.Failure(AuthFailure.ALIAS_ALREADY_EXISTS)
        }

        return try {
            val credentials = passwordHasher.createCredentials(password)
            val draft = User(
                alias = alias,
                aliasKey = aliasKey,
                role = role.value,
                passwordHash = credentials.hash,
                passwordSalt = credentials.salt
            )
            val user = draft.copy(id = userDao.insert(draft))
            saveSession(user.id)
            AuthResult.Success(user)
        } catch (_: SQLiteConstraintException) {
            AuthResult.Failure(AuthFailure.ALIAS_ALREADY_EXISTS)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            AuthResult.Failure(AuthFailure.STORAGE_ERROR)
        }
    }

    suspend fun login(aliasInput: String, password: String): AuthResult {
        val alias = aliasInput.trim()
        if (alias.isBlank() || password.isEmpty()) {
            return AuthResult.Failure(AuthFailure.INVALID_FIELDS)
        }

        return try {
            val user = userDao.findByAliasKey(normalizeAlias(alias))
                ?: return AuthResult.Failure(AuthFailure.INVALID_CREDENTIALS)
            val credentials = PasswordCredentials(user.passwordHash, user.passwordSalt)
            if (!passwordHasher.verify(password, credentials)) {
                AuthResult.Failure(AuthFailure.INVALID_CREDENTIALS)
            } else {
                saveSession(user.id)
                AuthResult.Success(user)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            AuthResult.Failure(AuthFailure.STORAGE_ERROR)
        }
    }

    suspend fun restoreSession(): User? {
        val userId = preferences.getLong(KEY_ACTIVE_USER_ID, INVALID_USER_ID)
        if (userId == INVALID_USER_ID) return null

        val user = userDao.findById(userId)
        if (user == null) clearSession()
        return user
    }

    fun logout() {
        clearSession()
    }

    private fun saveSession(userId: Long) {
        preferences.edit().putLong(KEY_ACTIVE_USER_ID, userId).apply()
    }

    private fun clearSession() {
        preferences.edit().remove(KEY_ACTIVE_USER_ID).apply()
    }

    private fun isValidAlias(alias: String): Boolean = alias.length in MIN_ALIAS_LENGTH..MAX_ALIAS_LENGTH

    private fun isValidPassword(password: String): Boolean =
        password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH &&
            password.any(Char::isUpperCase) &&
            password.any(Char::isLowerCase) &&
            password.any(Char::isDigit)

    private fun normalizeAlias(alias: String): String = alias.trim().lowercase(Locale.ROOT)

    companion object {
        private const val PREFERENCES_NAME = "agromatch_session"
        private const val KEY_ACTIVE_USER_ID = "active_user_id"
        private const val INVALID_USER_ID = -1L
        private const val MIN_ALIAS_LENGTH = 3
        private const val MAX_ALIAS_LENGTH = 40
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MAX_PASSWORD_LENGTH = 128
    }
}
