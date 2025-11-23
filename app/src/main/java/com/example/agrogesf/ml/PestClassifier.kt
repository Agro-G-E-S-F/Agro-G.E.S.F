package com.example.agrogesf.ml

import android.content.Context
import android.graphics.Bitmap
import ai.onnxruntime.*
import org.json.JSONObject
import java.nio.FloatBuffer
import kotlin.math.exp

/**
 * Classificador de pragas usando ONNX Runtime
 * Substitui ou complementa o PestClassifier.kt existente
 */
class ONNXPestClassifier(private val context: Context) {

    private var session: OrtSession? = null
    private var environment: OrtEnvironment? = null
    private var labels: List<String> = emptyList()
    private var isInitialized = false

    companion object {
        private const val MODEL_NAME = "agro_gesf_model.onnx"
        private const val LABELS_NAME = "labels.json"
        private const val INPUT_SIZE = 224

        // Normalização ImageNet (mesma do treinamento)
        private val MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
        private val STD = floatArrayOf(0.229f, 0.224f, 0.225f)
    }

    /**
     * Inicializa o modelo ONNX
     * Chame este método antes de usar predict()
     */
    fun initialize(): Boolean {
        if (isInitialized) return true

        return try {
            loadModel()
            loadLabels()
            isInitialized = true
            println("✓ ONNXPestClassifier inicializado com sucesso")
            true
        } catch (e: Exception) {
            println("✗ Erro ao inicializar ONNXPestClassifier: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun loadModel() {
        environment = OrtEnvironment.getEnvironment()

        val modelBytes = context.assets.open(MODEL_NAME).use { it.readBytes() }

        val sessionOptions = OrtSession.SessionOptions().apply {
            setIntraOpNumThreads(4) // Use 4 threads para melhor performance
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
        }

        session = environment?.createSession(modelBytes, sessionOptions)

        println("✓ Modelo ONNX carregado: ${modelBytes.size / 1024 / 1024} MB")
    }

    private fun loadLabels() {
        val jsonString = context.assets.open(LABELS_NAME).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val labelsArray = jsonObject.getJSONArray("labels")

        labels = List(labelsArray.length()) { i ->
            labelsArray.getString(i)
        }

        println("✓ ${labels.size} classes carregadas")
    }

    /**
     * Classifica uma imagem e retorna o resultado
     *
     * @param bitmap Imagem a ser classificada
     * @return ClassificationResult com a predição ou null se falhar
     */
    fun classify(bitmap: Bitmap): ClassificationResult? {
        if (!isInitialized) {
            println("✗ Modelo não inicializado. Chame initialize() primeiro")
            return null
        }

        if (session == null || labels.isEmpty()) {
            println("✗ Modelo ou labels não carregados")
            return null
        }

        return try {
            // 1. Preprocessar imagem
            val inputTensor = preprocessImage(bitmap)

            // 2. Executar inferência
            val outputs = session!!.run(mapOf("image" to inputTensor))

            // 3. Processar resultado
            val output = outputs[0].value as Array<FloatArray>
            val scores = output[0]

            // 4. Aplicar softmax
            val probabilities = softmax(scores)

            // 5. Obter top predições
            val topPredictions = getTopPredictions(probabilities, 5)

            // 6. Retornar resultado principal
            topPredictions.firstOrNull()?.let {
                ClassificationResult(
                    className = it.className,
                    confidence = it.confidence,
                    classIndex = it.classIndex,
                    allPredictions = topPredictions
                )
            }
        } catch (e: Exception) {
            println("✗ Erro na classificação: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun preprocessImage(bitmap: Bitmap): OnnxTensor {
        // Redimensionar para 224x224
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

        // Converter para float array normalizado
        val floatBuffer = FloatBuffer.allocate(1 * 3 * INPUT_SIZE * INPUT_SIZE)

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        // Normalizar e converter para formato NCHW (batch, channels, height, width)
        for (c in 0..2) { // Para cada canal RGB
            for (h in 0 until INPUT_SIZE) {
                for (w in 0 until INPUT_SIZE) {
                    val pixelValue = pixels[h * INPUT_SIZE + w]

                    val channelValue = when (c) {
                        0 -> ((pixelValue shr 16) and 0xFF) / 255.0f // R
                        1 -> ((pixelValue shr 8) and 0xFF) / 255.0f  // G
                        else -> (pixelValue and 0xFF) / 255.0f        // B
                    }

                    // Aplicar normalização ImageNet
                    val normalized = (channelValue - MEAN[c]) / STD[c]
                    floatBuffer.put(normalized)
                }
            }
        }

        floatBuffer.rewind()

        // Criar tensor ONNX
        val shape = longArrayOf(1, 3, INPUT_SIZE.toLong(), INPUT_SIZE.toLong())
        return OnnxTensor.createTensor(environment!!, floatBuffer, shape)
    }

    private fun softmax(scores: FloatArray): FloatArray {
        val maxScore = scores.maxOrNull() ?: 0f
        val expScores = scores.map { exp((it - maxScore).toDouble()).toFloat() }
        val sumExp = expScores.sum()
        return expScores.map { it / sumExp }.toFloatArray()
    }

    private fun getTopPredictions(probabilities: FloatArray, k: Int): List<Prediction> {
        return probabilities
            .mapIndexed { index, score ->
                Prediction(
                    className = if (index < labels.size) labels[index] else "class_$index",
                    confidence = score,
                    classIndex = index
                )
            }
            .sortedByDescending { it.confidence }
            .take(k)
    }

    /**
     * Libera recursos do modelo
     * Chame quando não precisar mais do classificador
     */
    fun close() {
        session?.close()
        environment?.close()
        isInitialized = false
        println("✓ ONNXPestClassifier finalizado")
    }
}

/**
 * Resultado da classificação
 */
data class ClassificationResult(
    val className: String,
    val confidence: Float,
    val classIndex: Int,
    val allPredictions: List<Prediction>
) {
    fun getConfidencePercentage(): Int = (confidence * 100).toInt()

    fun isHighConfidence(): Boolean = confidence > 0.7f

    fun getConfidenceLevel(): String = when {
        confidence > 0.8f -> "Alta"
        confidence > 0.6f -> "Média"
        else -> "Baixa"
    }
}

/**
 * Predição individual
 */
data class Prediction(
    val className: String,
    val confidence: Float,
    val classIndex: Int
)