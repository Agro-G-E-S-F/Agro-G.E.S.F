// DiseaseTranslation.kt
package com.example.agrogesf.utils

object DiseaseTranslation {

    // Mapeamento simples EN -> PT-BR
    private val translations = mapOf(
        // Pragas
        "Aphids" to "Afídeos (Pulgões)",
        "Spider Mites" to "Ácaros-Rajados",
        "Slug Sawfly" to "Pragas Diversas",
        "pragas_diversas" to "Pragas Diversas",

        // Doenças
        "Rust" to "Ferrugem",
        "Bacterial Spot" to "Antracnose",
        "Powdery Mildew" to "Oídio",
        "Leaf Scorch" to "Mancha-Foliar",
        "Black Rot" to "Podridão",
        "Mosaic Disease" to "Míldio",
        "Bacterial Blight" to "Crestamento Bacteriano",
        "Black Measles" to "Esca da Videira",
        "Blast" to "Brusone",
        "Cercospora Leaf Blight" to "Mancha-de-Cercospora",
        "Curl Virus" to "Vírus do Enrolamento da Folha",
        "Yellow Leaf Curl" to "Vírus do Enrolamento da Folha",
        "Early Blight" to "Pinta-Preta",
        "Greening" to "Huanglongbing (Greening)",
        "Late Blight" to "Requeima",
        "Late Leaf Spot" to "Mancha-Tardia",
        "Leaf Mold" to "Mofo-Foliar",
        "Mosaic Virus" to "Vírus do Mosaico",
        "Northern Leaf Blight" to "Mancha-Foliar-do-Milho",
        "Red Rot" to "Podridão-Vermelha",
        "Scab" to "Sarna",
        "Septoria Leaf Spot" to "Mancha-de-Septória",
        "Target Spot" to "Mancha-Alvo",

        // Categorias
        "fungos_diversas" to "Fungos Diversos",
        "nutricional_diversas" to "Deficiências Nutricionais",
        "outros_diversas" to "Outros Problemas",
        "virus_diversas" to "Vírus Diversos",
        "Healthy" to "Saudável"
    )

    /**
     * Traduz nome de doença EN -> PT-BR
     */
    fun translate(englishName: String): String {
        return translations[englishName] ?: englishName
    }

    /**
     * Obtém a chave do banco local correspondente (para buscar detalhes)
     * Retorna o nome em inglês normalizado para buscar no JSON local
     */
    fun getLocalKey(englishName: String): String {
        return when (englishName) {
            "Rust" -> "Ferrugem"
            "Bacterial Spot" -> "Antracnose"
            "Powdery Mildew" -> "Oídio"
            "Leaf Scorch" -> "Mancha-Foliar"
            "Black Rot" -> "Podridão"
            "Mosaic Disease" -> "Míldio"
            "Bacterial Blight" -> "Crestamento Bacteriano"
            "Black Measles" -> "Esca da Videira"
            "Blast" -> "Brusone"
            "Cercospora Leaf Blight" -> "Mancha-de-Cercospora"
            "Curl Virus", "Yellow Leaf Curl" -> "Vírus do Enrolamento da Folha"
            "Early Blight" -> "Pinta-Preta"
            "Greening" -> "Huanglongbing"
            "Late Blight" -> "Requeima"
            "Late Leaf Spot" -> "Mancha-Tardia"
            "Leaf Mold" -> "Mofo-Foliar"
            "Mosaic Virus" -> "Vírus do Mosaico"
            "Northern Leaf Blight" -> "Mancha-Foliar-do-Milho"
            "Red Rot" -> "Podridão-Vermelha"
            "Scab" -> "Sarna"
            "Septoria Leaf Spot" -> "Mancha-de-Septória"
            "Target Spot" -> "Mancha-Alvo"
            "Aphids" -> "Afídeos (Pulgões)"
            "Spider Mites" -> "Ácaros-Rajados (Spider Mites)"
            else -> englishName
        }
    }
}

// Extension function para facilitar uso
fun String.toPtBr(): String = DiseaseTranslation.translate(this)