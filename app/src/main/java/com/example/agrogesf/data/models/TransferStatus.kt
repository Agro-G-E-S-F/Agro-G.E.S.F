package com.example.agrogesf.data.models

sealed class TransferStatus {
    object Idle : TransferStatus()
    object Connecting : TransferStatus()
    data class Transferring(val progress: Int, val current: String) : TransferStatus()
    data class Success(val itemsReceived: Int) : TransferStatus()
    data class Error(val message: String) : TransferStatus()
}