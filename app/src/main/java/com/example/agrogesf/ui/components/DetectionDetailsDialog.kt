package com.example.agrogesf.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.PestType
import com.example.agrogesf.data.models.RaspberryDetection
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetectionDetailsDialog(
    detection: RaspberryDetection,
    pestInfo: Pest?, // Informações completas da praga do JSON
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    val date = Date(detection.detectedAt)
    val confidencePercentage = String.format(Locale("pt", "BR"), "%.2f", detection.confidence)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ========================================
                // HEADER COM IMAGEM
                // ========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    // Imagem de fundo
                    if (detection.imagePath.isNotEmpty() && File(detection.imagePath).exists()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = File(detection.imagePath)),
                            contentDescription = detection.pestName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2C3E50))
                        )
                    }

                    // Overlay escuro
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )

                    // Botão fechar
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Badge do tipo e confiança
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (detection.pestType == "PRAGA")
                                Color(0xFFFF6B6B)
                            else
                                Color(0xFFFFA726)
                        ) {
                            Text(
                                text = if (detection.pestType == "PRAGA") "PRAGA" else "DOENÇA",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = getConfidenceColor(detection.confidence)
                        ) {
                            Text(
                                text = "Confiança: $confidencePercentage%",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // ========================================
                // CONTEÚDO SCROLLÁVEL
                // ========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Nome da Praga/Doença
                    Text(
                        text = detection.pestName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Data e Hora da Detecção
                    Text(
                        text = "Detectado em ${dateFormat.format(date)}",
                        fontSize = 14.sp,
                        color = Color(0xFF7F8C8D),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Divider(color = Color(0xFFE0E0E0))

                    Spacer(modifier = Modifier.height(24.dp))

                    // ========================================
                    // INFORMAÇÕES DA PRAGA (do JSON)
                    // ========================================
                    if (pestInfo != null) {
                        Text(
                            text = "Sobre esta ${if (detection.pestType == "PRAGA") "praga" else "doença"}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = pestInfo.description,
                            fontSize = 16.sp,
                            color = Color(0xFF34495E),
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Galeria de imagens de referência (se houver)
                        if (pestInfo.images.isNotEmpty()) {
                            Text(
                                text = "Imagens de Referência",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C3E50)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pestInfo.images.take(3).forEach { imagePath ->
                                    if (File(imagePath).exists()) {
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(model = File(imagePath)),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Caso não encontre informações no JSON
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3CD)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ℹ️ Informações detalhadas não disponíveis para esta detecção.",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp,
                                color = Color(0xFF856404)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun getConfidenceColor(confidence: Float): Color {
    return when {
        confidence >= 80f -> Color(0xFF4CAF50)
        confidence >= 60f -> Color(0xFFFFA726)
        else -> Color(0xFFEF5350)
    }
}