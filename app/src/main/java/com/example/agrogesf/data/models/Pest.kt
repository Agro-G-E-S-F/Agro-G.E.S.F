package com.example.agrogesf.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.agrogesf.data.converters.StringListConverter

@Entity(tableName = "pests")
@TypeConverters(StringListConverter::class)
data class Pest(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val images: List<String>, // Lista de caminhos de imagens locais
    val type: PestType,
    val detectedCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class PestType {
    PRAGA, // Pest
    DOENCA // Disease
}