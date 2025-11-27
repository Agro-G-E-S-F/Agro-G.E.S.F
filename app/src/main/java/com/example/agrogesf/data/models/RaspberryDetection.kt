package com.example.agrogesf.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raspberry_detections")
data class RaspberryDetection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pestId: String,           // ID da praga/doença
    val pestName: String,         // Nome da praga/doença
    val pestType: String,         // "PRAGA" ou "DOENCA"
    val confidence: Float,        // Porcentagem de confiança (0.0 a 1.0)
    val imagePath: String,        // Caminho da imagem capturada
    val detectedAt: Long = System.currentTimeMillis(), // Timestamp
    val syncedToFirebase: Boolean = false
)