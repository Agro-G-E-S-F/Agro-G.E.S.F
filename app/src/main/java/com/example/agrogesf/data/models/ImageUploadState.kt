package com.example.agrogesf.data.models

sealed class ImageUploadState {
    object Idle : ImageUploadState()
    data class Uploading(val progress: Int, val fileName: String) : ImageUploadState()
    data class Success(val imageUrl: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}