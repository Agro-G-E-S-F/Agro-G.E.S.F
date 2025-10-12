package com.example.agrogesf.data.repository

import android.content.Context
import com.example.agrogesf.data.local.AppDatabase
import com.example.agrogesf.data.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.io.File

class AgroRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val pestDao = database.pestDao()
    private val detectionDao = database.detectionDao()
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val appContext = context.applicationContext

    // User operations
    suspend fun registerUser(name: String, email: String, password: String): Result<User> {
        return try {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                Result.failure(Exception("Email já cadastrado"))
            } else {
                val user = User(name = name, email = email, password = password)
                val userId = userDao.insert(user)
                Result.success(user.copy(id = userId))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    // Pest operations
    fun getPestsByType(type: PestType): Flow<List<Pest>> {
        return pestDao.getPestsByType(type)
    }

    suspend fun getPestById(pestId: String): Pest? {
        return pestDao.getPestById(pestId)
    }

    suspend fun savePestsFromWifi(pests: List<Pest>) {
        pestDao.insertAll(pests)
    }

    // Detection operations
    suspend fun recordDetection(pestId: String, userId: Long, latitude: Double, longitude: Double): Long {
        val detection = Detection(
            pestId = pestId,
            userId = userId,
            latitude = latitude,
            longitude = longitude
        )
        pestDao.incrementDetectionCount(pestId)
        return detectionDao.insert(detection)
    }

    fun getDetectionsByUser(userId: Long): Flow<List<Detection>> {
        return detectionDao.getDetectionsByUser(userId)
    }

    // Firebase sync operations
    suspend fun syncDataToFirebase(): Result<Unit> {
        return try {
            // Sync users
            val unsyncedUsers = userDao.getUnsyncedUsers()
            unsyncedUsers.forEach { user ->
                val userData = hashMapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "createdAt" to user.createdAt
                )
                firestore.collection("users")
                    .document(user.id.toString())
                    .set(userData)
                    .await()

                userDao.update(user.copy(syncedToFirebase = true))
            }

            // Sync detections
            val unsyncedDetections = detectionDao.getUnsyncedDetections()
            unsyncedDetections.forEach { detection ->
                val detectionData = hashMapOf(
                    "pestId" to detection.pestId,
                    "userId" to detection.userId,
                    "latitude" to detection.latitude,
                    "longitude" to detection.longitude,
                    "detectedAt" to detection.detectedAt
                )
                firestore.collection("detections")
                    .document(detection.id.toString())
                    .set(detectionData)
                    .await()

                detectionDao.update(detection.copy(syncedToFirebase = true))
            }

            // Sync pest statistics
            val mostDetected = pestDao.getMostDetectedPests()
            val statsData = mostDetected.map { pest ->
                hashMapOf(
                    "pestId" to pest.id,
                    "pestName" to pest.name,
                    "detectionCount" to pest.detectedCount,
                    "type" to pest.type.name
                )
            }

            firestore.collection("statistics")
                .document("pest_analytics")
                .set(hashMapOf("mostDetected" to statsData, "lastUpdated" to System.currentTimeMillis()))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // File storage operations
    fun saveImageLocally(imageBytes: ByteArray, filename: String): String {
        val imagesDir = File(appContext.filesDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val imageFile = File(imagesDir, filename)
        imageFile.writeBytes(imageBytes)
        return imageFile.absolutePath
    }

    fun getImageFile(path: String): File {
        return File(path)
    }
}
