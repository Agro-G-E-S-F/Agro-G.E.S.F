// ui/viewmodels/PestDetectionViewModel.kt
package com.example.agrogesf.ui.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrogesf.data.repository.AgroRepository
import com.example.agrogesf.ml.ONNXPestClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DetectionResult(
    val pestId: String,
    val pestName: String,
    val confidence: Float,
    val confidenceLevel: String  // "Alta", "Média", "Baixa"
)

data class PestDetectionUiState(
    val isLoading: Boolean = false,
    val detectionResults: DetectionResult? = null,
    val errorMessage: String? = null
)

class PestDetectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgroRepository(application)

    // ✅ MUDANÇA: Use ONNXPestClassifier em vez de PestClassifier
    private val classifier = ONNXPestClassifier(application)

    private val _uiState = MutableStateFlow(PestDetectionUiState())
    val uiState: StateFlow<PestDetectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val success = classifier.initialize()
            if (!success) {
                println("❌ Erro ao carregar modelo de IA")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erro ao carregar modelo de IA"
                )
            } else {
                println("✅ Modelo ONNX carregado com sucesso")
            }
        }
    }

    fun analyzePest(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    detectionResults = null
                )

                // 1. Processar imagem com o modelo ONNX (em background thread)
                val result = withContext(Dispatchers.IO) {
                    classifier.classify(bitmap)
                }

                if (result == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Erro ao processar imagem"
                    )
                    return@launch
                }

                // 2. Buscar informações da praga no banco de dados
                val pestInfo = repository.getPestById(result.className) // ou use result.classIndex

                // 3. Criar resultado da detecção
                val detectionResult = DetectionResult(
                    pestId = pestInfo?.id ?: result.className,
                    pestName = pestInfo?.name ?: result.className,
                    confidence = result.confidence,
                    confidenceLevel = result.getConfidenceLevel()
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    detectionResults = detectionResult
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao analisar imagem: ${e.message}"
                )
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
    }
}