package com.example.agrogesf.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detections")
data class Detection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pestId: String,
    val userId: Long,
    val latitude: Double,
    val longitude: Double,
    val detectedAt: Long = System.currentTimeMillis(),
    val syncedToFirebase: Boolean = false
)