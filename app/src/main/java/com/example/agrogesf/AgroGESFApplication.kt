package com.example.agrogesf

import android.app.Application
import com.example.agrogesf.data.local.AppDatabase
import com.google.firebase.FirebaseApp

class AgroGESFApplication : Application() {

    // Lazy initialization do database
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}