package com.example.agrogesf.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.preferences.PreferencesManager
import com.example.agrogesf.data.repository.AgroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PestDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AgroRepository(application)
    private val preferencesManager = PreferencesManager(application)

    private val _pest = MutableStateFlow<Pest?>(null)
    val pest: StateFlow<Pest?> = _pest

    private val _currentImageIndex = MutableStateFlow(0)
    val currentImageIndex: StateFlow<Int> = _currentImageIndex

    fun loadPest(pestId: String) {
        viewModelScope.launch {
            val pestData = repository.getPestById(pestId)
            _pest.value = pestData
        }
    }

    fun setImageIndex(index: Int) {
        _currentImageIndex.value = index
    }

    fun recordFalseAlarm(pestId: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val userSession = preferencesManager.userSession.first()
            if (userSession != null) {
                // Pode ser usado para analytics, mas não incrementa contagem
            }
        }
    }

    fun recordPestDetection(pestId: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val userSession = preferencesManager.userSession.first()
            if (userSession != null) {
                repository.recordDetection(pestId, userSession.userId, latitude, longitude)
            }
        }
    }
}