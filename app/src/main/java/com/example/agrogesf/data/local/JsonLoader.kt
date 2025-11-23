package com.example.agrogesf.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonLoader {

    fun loadDoencas(context: Context): Map<String, DoencaJson> {
        return try {
            val json = context.assets.open("doencas.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<Map<String, DoencaJson>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    fun loadPragas(context: Context): Map<String, PragaJson> {
        return try {
            val json = context.assets.open("pragas.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<Map<String, PragaJson>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }
}

data class DoencaJson(
    val nome: String,
    val descricao: String,
    val metodo_controle: String,
    val imagem: String
)

data class PragaJson(
    val nome: String,
    val nome_cientifico: String? = null,
    val descricao: String,
    val sintomas: List<String>? = null,
    val controle: List<String>? = null,
    val culturas_afetadas: List<String>? = null,
    val imagem: String
)