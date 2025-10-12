package com.example.agrogesf.data.repository

import android.content.Context
import android.net.Uri
import com.example.agrogesf.data.local.AppDatabase
import com.example.agrogesf.data.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AdminRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val pestDao = database.pestDao()
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage
    private val appContext = context.applicationContext

    // Verificar se é admin
    fun isAdmin(email: String, password: String): Boolean {
        val admin = Admin()
        return admin.isValid(email, password)
    }

    // Adicionar nova praga/doença
    suspend fun addPest(pestForm: PestForm): Result<Unit> {
        return try {
            val pest = Pest(
                id = pestForm.id.ifEmpty { UUID.randomUUID().toString() },
                name = pestForm.name,
                description = buildFullDescription(pestForm),
                images = pestForm.images,
                type = pestForm.type
            )

            // Salvar localmente
            pestDao.insert(pest)

            // Salvar no Firebase
            savePestToFirebase(pest, pestForm)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Atualizar praga/doença existente
    suspend fun updatePest(pestForm: PestForm): Result<Unit> {
        return try {
            val pest = Pest(
                id = pestForm.id,
                name = pestForm.name,
                description = buildFullDescription(pestForm),
                images = pestForm.images,
                type = pestForm.type
            )

            pestDao.insert(pest) // insert com REPLACE strategy
            savePestToFirebase(pest, pestForm)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Deletar praga/doença
    suspend fun deletePest(pestId: String): Result<Unit> {
        return try {
            val pest = pestDao.getPestById(pestId)
            if (pest != null) {
                // Deletar imagens locais
                pest.images.forEach { imagePath ->
                    try {
                        File(imagePath).delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Deletar do Firebase
                firestore.collection("pests")
                    .document(pestId)
                    .delete()
                    .await()

                // Deletar imagens do Storage
                pest.images.forEach { imagePath ->
                    try {
                        val fileName = File(imagePath).name
                        storage.reference
                            .child("pests/$pestId/$fileName")
                            .delete()
                            .await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obter todas as pragas (admin view)
    fun getAllPests(): Flow<List<Pest>> {
        return database.pestDao().getPestsByType(PestType.PRAGA)
    }

    fun getAllDiseases(): Flow<List<Pest>> {
        return database.pestDao().getPestsByType(PestType.DOENCA)
    }

    // Upload de imagem para Firebase Storage
    suspend fun uploadImage(uri: Uri, pestId: String): Result<String> {
        return try {
            val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference
                .child("pests/$pestId/$fileName")

            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            // Salvar localmente também
            val localPath = saveImageLocally(uri, fileName)

            Result.success(localPath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Salvar imagem localmente
    private fun saveImageLocally(uri: Uri, fileName: String): String {
        val imagesDir = File(appContext.filesDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val imageFile = File(imagesDir, fileName)
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(imageFile).use { output ->
                input.copyTo(output)
            }
        }

        return imageFile.absolutePath
    }

    // Construir descrição completa
    private fun buildFullDescription(form: PestForm): String {
        return buildString {
            append(form.description)

            if (form.scientificName.isNotBlank()) {
                append("\n\n**Nome Científico:** ${form.scientificName}")
            }

            if (form.symptoms.isNotBlank()) {
                append("\n\n**Sintomas:**\n${form.symptoms}")
            }

            if (form.prevention.isNotBlank()) {
                append("\n\n**Prevenção:**\n${form.prevention}")
            }

            if (form.treatment.isNotBlank()) {
                append("\n\n**Tratamento:**\n${form.treatment}")
            }
        }
    }

    // Salvar no Firebase
    private suspend fun savePestToFirebase(pest: Pest, form: PestForm) {
        val pestData = hashMapOf(
            "id" to pest.id,
            "name" to pest.name,
            "description" to pest.description,
            "type" to pest.type.name,
            "scientificName" to form.scientificName,
            "symptoms" to form.symptoms,
            "prevention" to form.prevention,
            "treatment" to form.treatment,
            "images" to pest.images.map { path ->
                // Converter path local para URL do Firebase Storage
                val fileName = File(path).name
                "pests/${pest.id}/$fileName"
            },
            "lastUpdated" to System.currentTimeMillis()
        )

        firestore.collection("pests")
            .document(pest.id)
            .set(pestData)
            .await()
    }

    // Buscar praga por ID
    suspend fun getPestById(pestId: String): Pest? {
        return pestDao.getPestById(pestId)
    }

    // Sincronizar todas as pragas do Firebase para local
    suspend fun syncPestsFromFirebase(): Result<Int> {
        return try {
            val snapshot = firestore.collection("pests").get().await()
            var count = 0

            snapshot.documents.forEach { doc ->
                try {
                    val pest = Pest(
                        id = doc.getString("id") ?: return@forEach,
                        name = doc.getString("name") ?: return@forEach,
                        description = doc.getString("description") ?: "",
                        type = if (doc.getString("type") == "PRAGA") PestType.PRAGA else PestType.DOENCA,
                        images = (doc.get("images") as? List<String>) ?: emptyList()
                    )

                    pestDao.insert(pest)
                    count++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}