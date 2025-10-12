package com.example.agrogesf.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.agrogesf.data.converters.StringListConverter
import com.example.agrogesf.data.models.User
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.Detection

@Database(
    entities = [User::class, Pest::class, Detection::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun pestDao(): PestDao
    abstract fun detectionDao(): DetectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agro_gesf_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
