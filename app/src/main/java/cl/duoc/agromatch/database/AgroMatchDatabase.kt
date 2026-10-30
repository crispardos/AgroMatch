package cl.duoc.agromatch.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.agromatch.model.Interest
import cl.duoc.agromatch.model.Need
import cl.duoc.agromatch.model.Rating
import cl.duoc.agromatch.model.User
import cl.duoc.agromatch.model.PilotProfile
import cl.duoc.agromatch.model.PilotCoverage
import cl.duoc.agromatch.model.PilotSpecialty
import cl.duoc.agromatch.model.NeedPhoto
import androidx.room.AutoMigration

@Database(
    entities = [
        User::class,
        PilotProfile::class,
        PilotCoverage::class,
        PilotSpecialty::class,
        Need::class,
        Interest::class,
        Rating::class,
        NeedPhoto::class
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class AgroMatchDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun pilotProfileDao(): PilotProfileDao
    abstract fun pilotCoverageDao(): PilotCoverageDao
    abstract fun pilotSpecialtyDao(): PilotSpecialtyDao
    abstract fun needDao(): NeedDao
    abstract fun interestDao(): InterestDao
    abstract fun ratingDao(): RatingDao
    abstract fun needPhotoDao(): NeedPhotoDao

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
                ).addMigrations(MIGRATION_3_4, MIGRATION_4_5).build().also { instance = it }
            }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS index_interests_needId_pilotId")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `need_photos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `needId` INTEGER NOT NULL,
                        `filePath` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        FOREIGN KEY(`needId`) REFERENCES `needs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_need_photos_needId` ON `need_photos` (`needId`)")
            }
        }
    }
}
