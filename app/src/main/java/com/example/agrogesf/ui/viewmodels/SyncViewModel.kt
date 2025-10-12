package com.example.agrogesf.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrogesf.data.preferences.PreferencesManager
import com.example.agrogesf.data.repository.AgroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SyncViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AgroRepository(application)
    private val preferencesManager = PreferencesManager(application)
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    val lastSyncTime = preferencesManager.lastSyncTime

    fun syncToFirebase() {
        if (!isInternetAvailable()) {
            _syncState.value = SyncState.Error("Sem conexão com a internet")
            return
        }

        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            try {
                val result = repository.syncDataToFirebase()
                result.onSuccess {
                    preferencesManager.updateLastSyncTime()
                    _syncState.value = SyncState.Success
                }.onFailure { error ->
                    _syncState.value = SyncState.Error(error.message ?: "Erro ao sincronizar")
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: "Erro ao sincronizar")
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }
}

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    data class Error(val message: String) : SyncState()
}