package com.example.agrogesf.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrogesf.data.models.User
import com.example.agrogesf.data.preferences.PreferencesManager
import com.example.agrogesf.data.repository.AgroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AgroRepository(application)
    private val preferencesManager = PreferencesManager(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode

    fun toggleAuthMode() {
        _isLoginMode.value = !_isLoginMode.value
        _authState.value = AuthState.Idle
    }

    fun login(email: String, password: String) {
        if (!validateLoginInput(email, password)) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = repository.login(email, password)
                if (user != null) {
                    preferencesManager.saveUserSession(user.id, user.email, user.name)
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Email ou com.example.agrogesf incorretos")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Erro ao fazer login")
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (!validateRegisterInput(name, email, password, confirmPassword)) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = repository.registerUser(name, email, password)
                result.onSuccess { user ->
                    preferencesManager.saveUserSession(user.id, user.email, user.name)
                    _authState.value = AuthState.Success(user)
                }.onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Erro ao cadastrar")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Erro ao cadastrar")
            }
        }
    }

    private fun validateLoginInput(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _authState.value = AuthState.Error("Email não pode estar vazio")
                false
            }
            password.isBlank() -> {
                _authState.value = AuthState.Error("com.example.agrogesf não pode estar vazia")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _authState.value = AuthState.Error("Email inválido")
                false
            }
            else -> true
        }
    }

    private fun validateRegisterInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        return when {
            name.isBlank() -> {
                _authState.value = AuthState.Error("Nome não pode estar vazio")
                false
            }
            email.isBlank() -> {
                _authState.value = AuthState.Error("Email não pode estar vazio")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _authState.value = AuthState.Error("Email inválido")
                false
            }
            password.length < 6 -> {
                _authState.value = AuthState.Error("Senha deve ter no mínimo 6 caracteres")
                false
            }
            password != confirmPassword -> {
                _authState.value = AuthState.Error("As senhas não coincidem")
                false
            }
            else -> true
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}