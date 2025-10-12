package com.example.agrogesf.data.models

sealed class AdminAction {
    object Idle : AdminAction()
    object Loading : AdminAction()
    data class Success(val message: String) : AdminAction()
    data class Error(val message: String) : AdminAction()
}