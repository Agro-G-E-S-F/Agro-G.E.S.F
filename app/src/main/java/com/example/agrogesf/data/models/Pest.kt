package com.example.agrogesf.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PestType {
    PRAGA,
    DOENCA
}

@Entity(tableName = "pests")
data class Pest(
    @PrimaryKey
    val id: String,
    val name: String,
    val scientificName: String = "",
    val description: String,
    val controlMethod: String = "",
    val images: List<String> = emptyList(),
    val type: PestType,
    val detectedCount: Int = 0,
    val syncedToFirebase: Boolean = false
)