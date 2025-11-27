package com.example.agrogesf.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// Adicione esses campos novos à sua classe Detection existente:
@Entity(tableName = "detections")
data class Detection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pestId: String,
    val userId: Long,
    val latitude: Double,
    val longitude: Double,
    val detectedAt: Long = System.currentTimeMillis(),
    val syncedToFirebase: Boolean = false,

    // ⭐ CAMPOS NOVOS PARA AS PREDIÇÕES:
    val pestName: String = "",        // Nome da praga/doença
    val pestType: String = "PRAGA",   // "PRAGA" ou "DOENCA" (String em vez de enum)
    val confidence: Float = 0f,       // Porcentagem de confiança (0.0 a 1.0)
    val imagePath: String = ""        // Caminho da imagem capturada
)