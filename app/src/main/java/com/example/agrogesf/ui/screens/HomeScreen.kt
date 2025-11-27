// ui/screens/HomeScreen.kt
package com.example.agrogesf.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.agrogesf.data.repository.AgroRepository
import com.example.agrogesf.network.WifiDataTransferManager
import com.example.agrogesf.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.launch
import java.io.File
import com.example.agrogesf.ui.components.RaspberryDetectionCard


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


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isConnected) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            wifiManager.fetchRaspberryDetections()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Buscar Detecções do Raspberry Pi")
                }
                when (transferStatus) {
                    is TransferStatus.Transferring -> {
                        val status = transferStatus as TransferStatus.Transferring
                        LinearProgressIndicator(
                            progress = status.progress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        Text(status.message, fontSize = 12.sp)
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

                Spacer(modifier = Modifier.height(16.dp))
            }
            if (raspberryDetections.isNotEmpty()) {
            Text(
                text = "Detecções Recentes (${raspberryDetections.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            raspberryDetections.forEach { detection ->
                RaspberryDetectionCard(
                    detection = detection,
                    modifier = Modifier.padding(vertical = 8.dp),
                    onClick = {
                        // Navegar para detalhes
                    }
                )
            }
        } else {
            Text(
                text = "Nenhuma detecção ainda. Conecte ao WiFi e busque as detecções.",
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }


            // Indicador de WiFi conectado
            if (isConnectedToDataWifi) {
                FloatingActionButton(
                    onClick = { viewModel.startDataTransfer() },
                    modifier = Modifier
                        .padding(16.dp),
                    containerColor = GreenPrimary
                ) {

                }
            }
            RemoteControlCard(wifiManager = wifiManager)

        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun PestImageCard(
        pest: Pest,
        onPestClick: () -> Unit
    ) {
        val imagePagerState = rememberPagerState(pageCount = { pest.images.size })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onPestClick() }
        ) {
            HorizontalPager(
                state = imagePagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val imagePath = pest.images[page]
                Image(
                    painter = rememberAsyncImagePainter(model = File(imagePath)),
                    contentDescription = pest.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Indicador de múltiplas imagens
            if (pest.images.size > 1) {
                Row(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${imagePagerState.currentPage + 1}/${pest.images.size}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    fun PestInfoSection(
        pest: Pest,
        onMoreClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardPrague)
                .padding(20.dp)
        ) {
            Text(
                text = pest.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = pest.description,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onMoreClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = YellowAccent
                )
            ) {
                Text(
                    text = "→ Ver mais sobre",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }


    @Composable
    fun PestCard(
        name: String,
        description: String,
        id: String,
        imageRes: Int,
        onPestClick: (String) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable { onPestClick(id) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFCEF)), // fundo amarelo clarinho bonito
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {

                // --- IMAGEM ---
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(120.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(14.dp))

                // --- TEXTOS ---
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46) // verde escuro bonito
                    )

                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563), // cinza elegante
                        maxLines = 2
                    )

                    // Barrinha colorida de rodapé
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFC727)) // amarelo
                    )
                }
            }
        }
    }




    Spacer(modifier = Modifier.height(16.dp))


}

@Composable
fun RemoteControlCard(
    wifiManager: WifiDataTransferManager,
    modifier: Modifier = Modifier
) {
    val isConnected by wifiManager.isConnectedToDataWifi.collectAsState()
    val isRunning by wifiManager.isScriptRunning.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) {
                if (isRunning) Color(0xFF4CAF50) else Color(0xFFFF9800)
            } else {
                Color(0xFFE0E0E0)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header com ícone e título
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Controle Remoto",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) Color.White else Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Indicador de conexão piscando
                        if (isConnected && isRunning) {
                            PulsingDot()
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = when {
                                !isConnected -> "Desconectado"
                                isRunning -> "Executando"
                                else -> "Conectado (Parado)"
                            },
                            fontSize = 14.sp,
                            color = if (isConnected) Color.White.copy(alpha = 0.9f) else Color.Gray
                        )
                    }
                }

                // Ícone de status
                Icon(
                    imageVector = when {
                        !isConnected -> Icons.Default.WifiOff
                        isRunning -> Icons.Default.CheckCircle
                        else -> Icons.Default.Cancel
                    },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isConnected) Color.White else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status detalhado
            StatusBar(isConnected = isConnected, isRunning = isRunning)

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de controle
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null
                        showSuccess = false

                        val result = wifiManager.toggleScript()

                        result.onSuccess {
                            showSuccess = true
                            // Esconde a mensagem de sucesso após 2 segundos
                            kotlinx.coroutines.delay(2000)
                            showSuccess = false
                        }

                        result.onFailure { error ->
                            errorMessage = error.message
                        }

                        isLoading = false
                    }
                },
                enabled = isConnected && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFD32F2F) else Color(0xFF2196F3),
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isRunning) "PARAR SCRIPT" else "INICIAR SCRIPT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Mensagens de feedback
            AnimatedVisibility(visible = showSuccess) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Comando enviado com sucesso!",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFCDD2)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Erro: $error",
                            color = Color(0xFFC62828),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Mensagem de ajuda quando desconectado
            if (!isConnected) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Conecte-se ao WiFi AGRO_GESF_DATA para controlar o script",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
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