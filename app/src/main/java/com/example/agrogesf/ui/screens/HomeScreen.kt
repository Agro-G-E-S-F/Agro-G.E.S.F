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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.agrogesf.ui.viewmodels.HomeViewModel
import java.io.File


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
                            text = status.current,
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
                            text = "${status.itemsReceived} itens recebidos",
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
    val pragues by viewModel.pragues.collectAsState()
    val transferStatus by viewModel.transferStatus.collectAsState()
    val isConnectedToDataWifi by viewModel.isConnectedToDataWifi.collectAsState()

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
            // Status de transferência
            AnimatedVisibility(
                visible = transferStatus !is TransferStatus.Idle,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                TransferStatusCard(
                    status = transferStatus,
                    onDismiss = { viewModel.resetTransferStatus() }
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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_wifi),
                        contentDescription = "Transferir dados",
                        tint = Color.White
                    )
                }
            }
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
    fun EmptyStateCard() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_wifi_off),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = GreenPrimary.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Nenhum dado carregado",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Conecte-se ao WiFi AGRO_GESF_DATA para receber informações sobre pragas e doenças",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Carrossel de pragas com ViewPager
    if (pragues.isNotEmpty()) {
        val pagerState = rememberPagerState(pageCount = { pragues.size })

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ViewPager para as imagens
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val pest = pragues[page]
                        PestImageCard(
                            pest = pest,
                            onPestClick = { onPestClick(pest.id) }
                        )
                    }

                    // Indicadores de página
                    Row(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pragues.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration)
                                YellowAccent
                            else
                                Color.White.copy(alpha = 0.5f)

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(if (pagerState.currentPage == iteration) 12.dp else 8.dp)
                                    .animateContentSize()
                            )
                        }
                    }
                }

                // Informações da praga atual
                val currentPest = pragues[pagerState.currentPage]
                PestInfoSection(
                    pest = currentPest,
                    onMoreClick = { onPestClick(currentPest.id) }
                )
            }
        }
    } else {
        // Estado vazio
        EmptyStateCard()
    }
}