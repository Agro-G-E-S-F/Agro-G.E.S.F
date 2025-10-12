package com.example.agrogesf.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedToFirebase: Boolean = false
)
