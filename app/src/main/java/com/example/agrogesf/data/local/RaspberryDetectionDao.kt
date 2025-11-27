package com.example.agrogesf.data.local

import androidx.room.*
import com.example.agrogesf.data.models.RaspberryDetection
import kotlinx.coroutines.flow.Flow

@Dao
interface RaspberryDetectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detection: RaspberryDetection): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(detections: List<RaspberryDetection>)

    @Update
    suspend fun update(detection: RaspberryDetection)

    @Delete
    suspend fun delete(detection: RaspberryDetection)

    @Query("SELECT * FROM raspberry_detections ORDER BY detectedAt DESC")
    fun getAllDetections(): Flow<List<RaspberryDetection>>

    @Query("SELECT * FROM raspberry_detections WHERE pestType = :type ORDER BY detectedAt DESC")
    fun getDetectionsByType(type: String): Flow<List<RaspberryDetection>>

    @Query("SELECT * FROM raspberry_detections WHERE syncedToFirebase = 0")
    suspend fun getUnsyncedDetections(): List<RaspberryDetection>

    @Query("SELECT * FROM raspberry_detections WHERE id = :id")
    suspend fun getDetectionById(id: Long): RaspberryDetection?

    @Query("DELETE FROM raspberry_detections")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM raspberry_detections")
    suspend fun getCount(): Int
}