package com.example.agrogesf.data.local

import androidx.room.*
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.PestType
import kotlinx.coroutines.flow.Flow

@Dao
interface PestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pests: List<Pest>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pest: Pest)

    @Query("SELECT * FROM pests WHERE type = :type ORDER BY name ASC")
    fun getPestsByType(type: PestType): Flow<List<Pest>>

    @Query("SELECT * FROM pests WHERE id = :pestId")
    suspend fun getPestById(pestId: String): Pest?

    @Query("UPDATE pests SET detectedCount = detectedCount + 1 WHERE id = :pestId")
    suspend fun incrementDetectionCount(pestId: String)

    @Query("SELECT * FROM pests ORDER BY detectedCount DESC LIMIT 10")
    suspend fun getMostDetectedPests(): List<Pest>

    @Query("DELETE FROM pests")
    suspend fun deleteAll()
}