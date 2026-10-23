package cl.duoc.agromatch.database

import android.database.sqlite.SQLiteConstraintException
import cl.duoc.agromatch.model.User
import cl.duoc.agromatch.model.UserRole
import cl.duoc.agromatch.repository.PasswordHasher
import java.util.Locale

class DemoDataSeeder(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher
) {
    suspend fun seedAdminIfMissing() {
        val aliasKey = ADMIN_ALIAS.lowercase(Locale.ROOT)
        if (userDao.findByAliasKey(aliasKey) != null) return

        val credentials = passwordHasher.createCredentials(ADMIN_PASSWORD)
        try {
            userDao.insert(
                User(
                    alias = ADMIN_ALIAS,
                    aliasKey = aliasKey,
                    role = UserRole.ADMIN.value,
                    passwordHash = credentials.hash,
                    passwordSalt = credentials.salt
                )
            )
        } catch (_: SQLiteConstraintException) {
            // Another startup may have inserted the demo account concurrently.
        }
    }

    companion object {
        const val ADMIN_ALIAS = "admin-demo"
        const val ADMIN_PASSWORD = "AgroDemo2026!"
    }
}
