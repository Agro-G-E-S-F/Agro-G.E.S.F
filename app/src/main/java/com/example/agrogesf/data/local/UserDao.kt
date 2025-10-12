package com.example.agrogesf.data.local

import androidx.room.*
import com.example.agrogesf.data.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE syncedToFirebase = 0")
    suspend fun getUnsyncedUsers(): List<User>

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
}