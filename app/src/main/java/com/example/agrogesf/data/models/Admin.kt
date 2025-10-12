// data/models/Admin.kt
package com.example.agrogesf.data.models

data class Admin(
    val email: String = "agrogesf@gmail.com",
    val password: String = "B4tata_4zul"
) {
    fun isValid(inputEmail: String, inputPassword: String): Boolean {
        return email == inputEmail && password == inputPassword
    }
}