// ui/screens/HomeScreen.kt
package com.example.agrogesf.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.TransferStatus
import com.example.agrogesf.ui.theme.*
import com.example.agrogesf.R
import com.example.agrogesf.data.models.RaspberryDetection
import com.example.agrogesf.data.repository.AgroRepository
import com.example.agrogesf.network.WifiDataTransferManager
import com.example.agrogesf.ui.components.DetectionDetailsDialog
import com.example.agrogesf.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.launch
import java.io.File
import com.example.agrogesf.ui.components.RaspberryDetectionCard
import com.example.agrogesf.utils.JSONHelper.findBestMatch


@Composable
fun TransferStatusCard(
    status: TransferStatus,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                is TransferStatus.Success -> SuccessGreen.copy(alpha = 0.9f)
                is TransferStatus.Error -> ErrorRed.copy(alpha = 0.9f)
                else -> GreenPrimary
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                when (status) {
                    is TransferStatus.Connecting -> {
                        Text(
                            text = "Conectando...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 8.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    is TransferStatus.Transferring -> {
                        Text(
                            text = status.message,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = status.progress / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = YellowAccent,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "${status.progress}%",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    is TransferStatus.Success -> {
                        Text(
                            text = "Transferência concluída!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${status.itemCount} itens recebidos",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                    is TransferStatus.Error -> {
                        Text(
                            text = "Erro na transferência",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = status.message,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                    else -> {}
                }
            }

            if (status is TransferStatus.Success || status is TransferStatus.Error) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Fechar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onPestClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val repository = remember { AgroRepository(context) }
    val pragues by viewModel.pragues.collectAsState()
    val transferStatus by viewModel.transferStatus.collectAsState()
    val isConnectedToDataWifi by viewModel.isConnectedToDataWifi.collectAsState()
    val wifiManager = remember { WifiDataTransferManager(context, repository) }
    val isConnected by wifiManager.isConnectedToDataWifi.collectAsState()
    val isRunning by wifiManager.isScriptRunning.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val raspberryDetections by repository.getAllRaspberryDetections().collectAsState(initial = emptyList())
    var selectedDetection by remember { mutableStateOf<RaspberryDetection?>(null) }
    val diseases by viewModel.diseases.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
        ) {
            // ✅ MUDANÇA: Adicionar LazyColumn para scroll
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botão e Status de Transferência
                if (isConnected) {
                    item {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    wifiManager.fetchRaspberryDetections()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Buscar Detecções do Raspberry Pi")
                        }
                    }

                    item {
                        when (transferStatus) {
                            is TransferStatus.Transferring -> {
                                val status = transferStatus as TransferStatus.Transferring
                                Column {
                                    LinearProgressIndicator(
                                        progress = status.progress / 100f,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(status.message, fontSize = 12.sp)
                                }
                            }

                            is TransferStatus.Success -> {
                                val count = (transferStatus as TransferStatus.Success).itemCount
                                Text("✓ $count detecções baixadas!", color = Color.Green)
                            }

                            is TransferStatus.Error -> {
                                val error = (transferStatus as TransferStatus.Error).message
                                Text("✗ Erro: $error", color = Color.Red)
                            }

                            else -> {}
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (raspberryDetections.isNotEmpty()) {
                    item {
                        Text(
                            text = "Detecções Recentes (${raspberryDetections.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(raspberryDetections) { detection ->
                        RaspberryDetectionCard(
                            detection = detection,
                            onClick = {
                                selectedDetection = detection
                            }
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "Nenhuma detecção ainda. Conecte ao WiFi e busque as detecções.",
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Remote Control Card

            }

            // FAB permanece no Box como overlay
            if (isConnectedToDataWifi) {
                FloatingActionButton(
                    onClick = { viewModel.startDataTransfer() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = GreenPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Download, // ou outro ícone
                        contentDescription = "Iniciar transferência"
                    )
                }
            }

        }
        selectedDetection?.let { detection ->
            val context = LocalContext.current

            // ✅ Busca as informações usando o helper
            val pestInfo = remember(detection) {
                // Tenta buscar pelo name_en (que vem do servidor)
                val searchName = detection.pestName
                findBestMatch(context, searchName)
            }

            DetectionDetailsDialog(
                detection = detection,
                pestInfo = pestInfo,
                onDismiss = { selectedDetection = null }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))


}


@Composable
private fun StatusBar(isConnected: Boolean, isRunning: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatusItem(
            icon = Icons.Default.Wifi,
            label = "Conexão",
            value = if (isConnected) "OK" else "OFF",
            color = if (isConnected) Color.White else Color.Gray
        )

        Divider(
            modifier = Modifier
                .height(40.dp)
                .width(1.dp),
            color = if (isConnected) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f)
        )

        StatusItem(
            icon = if (isRunning) Icons.Default.PlayArrow else Icons.Default.Stop,
            label = "Status",
            value = if (isRunning) "Rodando" else "Parado",
            color = if (isConnected) Color.White else Color.Gray
        )
    }
}

@Composable
private fun StatusItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            color = color.copy(alpha = 0.7f)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(10.dp)
            .background(Color.White.copy(alpha = alpha), CircleShape)
    )
}