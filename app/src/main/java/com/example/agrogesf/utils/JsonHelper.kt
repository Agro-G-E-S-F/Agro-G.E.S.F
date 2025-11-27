package com.example.agrogesf.utils

import android.content.Context
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.PestType
import org.json.JSONObject

object JSONHelper {

    // ⭐ MAPEAMENTO: ID do servidor → Key do JSON
    private val ID_TO_KEY_MAP = mapOf(
        // Pragas
        "aphids" to "Aphids",
        "spider_mites" to "Spider Mites",
        "pragas_diversas" to "pragas_diversas",

        // Doenças
        "ferrugem" to "Ferrugem",
        "antracnose" to "Antracnose",
        "mildio" to "Míldio",
        "mancha_foliar" to "Mancha-Foliar",
        "podridao" to "Podridão",
        "oidio" to "Oídio",
        "bacterial_blight" to "Crestamento Bacteriano",
        "black_measles" to "Esca da Videira",
        "brusone" to "Brusone",
        "cercospora_leaf_blight" to "Mancha-de-Cercospora",
        "curl_virus" to "Vírus do Enrolamento da Folha",
        "early_blight" to "Pinta-Preta",
        "greening" to "Huanglongbing",
        "late_blight" to "Requeima",
        "late_leaf_spot" to "Mancha-Tardia",
        "leaf_mold" to "Mofo-Foliar",
        "mosaic_virus" to "Vírus do Mosaico",
        "northern_leaf_blight" to "Mancha-Foliar-do-Milho",
        "red_rot" to "Podridão-Vermelha",
        "sarna" to "Sarna",
        "septoria_leaf_spot" to "Mancha-de-Septória",
        "target_spot" to "Mancha-Alvo"
    )

    fun loadPestInfoFromJson(
        context: Context,
        pestIdOrName: String
    ): Pest? {
        return try {
            // ⭐ Primeiro tenta mapear o ID
            val searchKey = ID_TO_KEY_MAP[pestIdOrName.lowercase()] ?: pestIdOrName

            // Tenta em pragas
            val pragasJson = context.assets.open("pragas.json")
                .bufferedReader().use { it.readText() }
            val pragasObj = JSONObject(pragasJson)

            if (pragasObj.has(searchKey)) {
                val pestData = pragasObj.getJSONObject(searchKey)
                return Pest(
                    id = pestIdOrName,
                    name = pestData.getString("nome"),
                    description = pestData.getString("descricao"),
                    controlMethod = pestData.optString("metodo_controle", ""),
                    images = listOf(pestData.optString("imagem", "")),
                    type = PestType.PRAGA
                )
            }

            // Tenta em doenças
            val doencasJson = context.assets.open("doencas.json")
                .bufferedReader().use { it.readText() }
            val doencasObj = JSONObject(doencasJson)

            if (doencasObj.has(searchKey)) {
                val pestData = doencasObj.getJSONObject(searchKey)
                return Pest(
                    id = pestIdOrName,
                    name = pestData.getString("nome"),
                    description = pestData.getString("descricao"),
                    controlMethod = pestData.optString("metodo_controle", ""),
                    images = listOf(pestData.optString("imagem", "")),
                    type = PestType.DOENCA
                )
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun findBestMatch(
        context: Context,
        searchName: String
    ): Pest? {
        // ⭐ Primeiro tenta buscar pelo ID direto
        loadPestInfoFromJson(context, searchName)?.let { return it }

        // Se não encontrou, tenta fuzzy matching
        return try {
            val pragasJson = context.assets.open("pragas.json")
                .bufferedReader().use { it.readText() }
            val pragasObj = JSONObject(pragasJson)

            val doencasJson = context.assets.open("doencas.json")
                .bufferedReader().use { it.readText() }
            val doencasObj = JSONObject(doencasJson)

            val allKeys = mutableListOf<String>()
            pragasObj.keys().forEach { allKeys.add(it) }
            doencasObj.keys().forEach { allKeys.add(it) }

            // Busca por similaridade
            val bestMatch = allKeys.find { key ->
                key.equals(searchName, ignoreCase = true) ||
                        key.contains(searchName, ignoreCase = true) ||
                        searchName.contains(key, ignoreCase = true)
            }

            bestMatch?.let { loadPestInfoFromJson(context, it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}