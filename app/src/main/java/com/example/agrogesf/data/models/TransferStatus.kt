package com.example.agrogesf.data.models

sealed class TransferStatus {
    object Idle : TransferStatus()

    object Connecting : TransferStatus()
    data class Transferring(
        val progress: Int,
        val message: String
    ) : TransferStatus()
    data class Success(
        val itemCount: Int
    ) : TransferStatus()    data class Error(val message: String) : TransferStatus()
}