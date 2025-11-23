package com.example.agrogesf.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.PestType
import com.example.agrogesf.data.repository.AgroRepository
import com.example.agrogesf.network.WifiDataTransferManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AgroRepository(application)
    private val wifiManager = WifiDataTransferManager(application, repository)


    val pragues: StateFlow<List<Pest>> = repository.getPestsByType(PestType.PRAGA)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diseases: StateFlow<List<Pest>> = repository.getPestsByType(PestType.DOENCA)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transferStatus = wifiManager.transferStatus
    val isConnectedToDataWifi = wifiManager.isConnectedToDataWifi

    fun startDataTransfer() {
        viewModelScope.launch {
            wifiManager.startDataTransfer()
        }
    }

    fun resetTransferStatus() {
        wifiManager.resetStatus()
    }

    override fun onCleared() {
        super.onCleared()
        wifiManager.unregister()
    }
}