package com.example.agrogesf.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.agrogesf.data.models.RaspberryDetection
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
    fun RaspberryDetectionCard(
    detection: RaspberryDetection,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = Date(detection.detectedAt)

    val confidencePercentage = (detection.confidence * 100).toInt()

    // Cor baseada no tipo
    val cardColor = if (detection.pestType == "PRAGA") {
        Color(0xFFFF6B6B) // Vermelho para pragas
    } else {
        Color(0xFFFFA726) // Laranja para doenças
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========================================
            // IMAGEM (1/3 do espaço - esquerda)
            // ========================================
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(cardColor.copy(alpha = 0.1f))
            ) {
                if (detection.imagePath.isNotEmpty() && File(detection.imagePath).exists()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = File(detection.imagePath)),
                        contentDescription = detection.pestName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder se não houver imagem
                    Icon(
                        imageVector = if (detection.pestType == "PRAGA")
                            Icons.Default.BugReport
                        else
                            Icons.Default.Coronavirus,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        tint = cardColor.copy(alpha = 0.5f)
                    )
                }

                // Badge do tipo no canto superior esquerdo
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = cardColor
                ) {
                    Text(
                        text = if (detection.pestType == "PRAGA") "PRAGA" else "DOENÇA",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // ========================================
            // INFORMAÇÕES (2/3 do espaço - direita)
            // ========================================
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Nome da praga/doença
                Text(
                    text = detection.pestName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50),
                    maxLines = 2,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Data e Hora
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = dateFormat.format(date),
                            fontSize = 12.sp,
                            color = Color(0xFF7F8C8D),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = timeFormat.format(date),
                            fontSize = 11.sp,
                            color = Color(0xFF95A5A6)
                        )
                    }

                    // Porcentagem de confiança
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = getConfidenceColor(detection.confidence)
                    ) {
                        Text(
                            text = "$confidencePercentage%",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// Função auxiliar para cor baseada na confiança
private fun getConfidenceColor(confidence: Float): Color {
    return when {
        confidence >= 0.8f -> Color(0xFF4CAF50) // Verde - Alta confiança
        confidence >= 0.6f -> Color(0xFFFFA726) // Laranja - Média confiança
        else -> Color(0xFFEF5350) // Vermelho - Baixa confiança
    }
}

