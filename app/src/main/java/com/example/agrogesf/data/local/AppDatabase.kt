package com.example.agrogesf.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.agrogesf.data.converters.StringListConverter
import com.example.agrogesf.data.models.User
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.Detection
import com.example.agrogesf.data.models.RaspberryDetection

@Database(
    entities = [
        User::class,
        Pest::class,
        Detection::class,
        RaspberryDetection::class  // ⭐ NOVA ENTIDADE
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun pestDao(): PestDao
    abstract fun detectionDao(): DetectionDao
    abstract fun raspberryDetectionDao(): RaspberryDetectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adiciona as novas colunas
                database.execSQL("ALTER TABLE detections ADD COLUMN pestName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE detections ADD COLUMN pestType TEXT NOT NULL DEFAULT 'PRAGA'")
                database.execSQL("ALTER TABLE detections ADD COLUMN confidence REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE detections ADD COLUMN imagePath TEXT NOT NULL DEFAULT ''")
            }
        }
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agro_gesf_database"
                )
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
