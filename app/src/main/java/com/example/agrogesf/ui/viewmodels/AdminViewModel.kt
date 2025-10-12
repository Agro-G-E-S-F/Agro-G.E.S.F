// ui/viewmodels/AdminViewModel.kt
package com.example.agrogesf.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrogesf.data.models.*
import com.example.agrogesf.data.repository.AdminRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminRepository(application)

    private val _adminAction = MutableStateFlow<AdminAction>(AdminAction.Idle)
    val adminAction: StateFlow<AdminAction> = _adminAction

    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState

    private val _currentPestForm = MutableStateFlow(PestForm())
    val currentPestForm: StateFlow<PestForm> = _currentPestForm

    private val _uploadedImages = MutableStateFlow<List<String>>(emptyList())
    val uploadedImages: StateFlow<List<String>> = _uploadedImages

    val pragues = repository.getAllPests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diseases = repository.getAllDiseases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Verificar login admin
    fun verifyAdminLogin(email: String, password: String): Boolean {
        return repository.isAdmin(email, password)
    }

    // Atualizar formulário
    fun updateForm(update: (PestForm) -> PestForm) {
        _currentPestForm.value = update(_currentPestForm.value)
    }

    // Limpar formulário
    fun clearForm() {
        _currentPestForm.value = PestForm()
        _uploadedImages.value = emptyList()
        _adminAction.value = AdminAction.Idle
    }

    // Carregar praga para edição
    fun loadPestForEdit(pestId: String) {
        viewModelScope.launch {
            try {
                val pest = repository.getPestById(pestId)
                if (pest != null) {
                    _currentPestForm.value = PestForm(
                        id = pest.id,
                        name = pest.name,
                        description = pest.description,
                        type = pest.type,
                        images = pest.images
                    )
                    _uploadedImages.value = pest.images
                }
            } catch (e: Exception) {
                _adminAction.value = AdminAction.Error("Erro ao carregar praga: ${e.message}")
            }
        }
    }

    // Upload de imagem
    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            try {
                _imageUploadState.value = ImageUploadState.Uploading(0, uri.lastPathSegment ?: "imagem")

                val pestId = _currentPestForm.value.id.ifEmpty {
                    java.util.UUID.randomUUID().toString()
                }

                val result = repository.uploadImage(uri, pestId)
                result.onSuccess { localPath ->
                    _uploadedImages.value = _uploadedImages.value + localPath
                    _imageUploadState.value = ImageUploadState.Success(localPath)

                    // Atualizar form com nova imagem
                    updateForm { it.copy(images = _uploadedImages.value, id = pestId) }
                }.onFailure { error ->
                    _imageUploadState.value = ImageUploadState.Error(error.message ?: "Erro no upload")
                }
            } catch (e: Exception) {
                _imageUploadState.value = ImageUploadState.Error(e.message ?: "Erro no upload")
            }
        }
    }

    // Remover imagem
    fun removeImage(imagePath: String) {
        _uploadedImages.value = _uploadedImages.value.filter { it != imagePath }
        updateForm { it.copy(images = _uploadedImages.value) }
    }

    // Salvar praga/doença
    fun savePest() {
        val form = _currentPestForm.value

        // Validação
        if (form.name.isBlank()) {
            _adminAction.value = AdminAction.Error("Nome é obrigatório")
            return
        }

        if (form.description.isBlank()) {
            _adminAction.value = AdminAction.Error("Descrição é obrigatória")
            return
        }

        if (form.images.isEmpty()) {
            _adminAction.value = AdminAction.Error("Adicione pelo menos uma imagem")
            return
        }

        viewModelScope.launch {
            _adminAction.value = AdminAction.Loading
            try {
                val result = if (form.id.isBlank()) {
                    repository.addPest(form)
                } else {
                    repository.updatePest(form)
                }

                result.onSuccess {
                    _adminAction.value = AdminAction.Success(
                        if (form.id.isBlank()) "Praga adicionada com sucesso!"
                        else "Praga atualizada com sucesso!"
                    )
                    clearForm()
                }.onFailure { error ->
                    _adminAction.value = AdminAction.Error(error.message ?: "Erro ao salvar")
                }
            } catch (e: Exception) {
                _adminAction.value = AdminAction.Error(e.message ?: "Erro ao salvar")
            }
        }
    }

    // Deletar praga/doença
    fun deletePest(pestId: String) {
        viewModelScope.launch {
            _adminAction.value = AdminAction.Loading
            try {
                val result = repository.deletePest(pestId)
                result.onSuccess {
                    _adminAction.value = AdminAction.Success("Praga deletada com sucesso!")
                }.onFailure { error ->
                    _adminAction.value = AdminAction.Error(error.message ?: "Erro ao deletar")
                }
            } catch (e: Exception) {
                _adminAction.value = AdminAction.Error(e.message ?: "Erro ao deletar")
            }
        }
    }

    // Sincronizar do Firebase
    fun syncFromFirebase() {
        viewModelScope.launch {
            _adminAction.value = AdminAction.Loading
            try {
                val result = repository.syncPestsFromFirebase()
                result.onSuccess { count ->
                    _adminAction.value = AdminAction.Success("$count pragas sincronizadas!")
                }.onFailure { error ->
                    _adminAction.value = AdminAction.Error(error.message ?: "Erro ao sincronizar")
                }
            } catch (e: Exception) {
                _adminAction.value = AdminAction.Error(e.message ?: "Erro ao sincronizar")
            }
        }
    }

    fun resetAction() {
        _adminAction.value = AdminAction.Idle
    }

    fun resetImageUploadState() {
        _imageUploadState.value = ImageUploadState.Idle
    }
}