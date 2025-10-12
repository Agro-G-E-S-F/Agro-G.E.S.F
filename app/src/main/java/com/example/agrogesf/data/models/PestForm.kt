package com.example.agrogesf.data.models

data class PestForm(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val type: PestType = PestType.PRAGA,
    val images: List<String> = emptyList(),
    val scientificName: String = "",
    val symptoms: String = "",
    val prevention: String = "",
    val treatment: String = ""
)