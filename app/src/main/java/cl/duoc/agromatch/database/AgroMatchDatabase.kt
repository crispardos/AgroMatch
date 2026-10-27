package cl.duoc.agromatch.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.duoc.agromatch.model.User
import cl.duoc.agromatch.model.PilotProfile
import cl.duoc.agromatch.model.PilotCoverage
import cl.duoc.agromatch.model.PilotSpecialty
import androidx.room.AutoMigration

@Database(
    entities = [
        User::class,
        PilotProfile::class,
        PilotCoverage::class,
        PilotSpecialty::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)]

)
abstract class AgroMatchDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun pilotProfileDao(): PilotProfileDao
    abstract fun pilotCoverageDao(): PilotCoverageDao
    abstract fun pilotSpecialtyDao(): PilotSpecialtyDao
    companion object {
        private const val DATABASE_NAME = "agromatch.db"

        @Volatile
        private var instance: AgroMatchDatabase? = null

        fun getInstance(context: Context): AgroMatchDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AgroMatchDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
    }
}
