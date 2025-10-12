package com.example.agrogesf.data.local

import androidx.room.*
import com.example.agrogesf.data.models.Detection
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionDao {
    @Insert
    suspend fun insert(detection: Detection): Long

    @Query("SELECT * FROM detections WHERE userId = :userId ORDER BY detectedAt DESC")
    fun getDetectionsByUser(userId: Long): Flow<List<Detection>>

    @Query("SELECT * FROM detections WHERE syncedToFirebase = 0")
    suspend fun getUnsyncedDetections(): List<Detection>

    @Update
    suspend fun update(detection: Detection)

    @Query("SELECT COUNT(*) FROM detections WHERE pestId = :pestId")
    suspend fun getDetectionCountForPest(pestId: String): Int
}