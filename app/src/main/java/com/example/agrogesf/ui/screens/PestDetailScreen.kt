// ui/screens/PestDetailScreen.kt
package com.example.agrogesf.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.agrogesf.R
import com.example.agrogesf.ui.components.CustomButton
import com.example.agrogesf.ui.theme.*
import com.example.agrogesf.ui.viewmodels.PestDetailViewModel
import com.google.android.gms.location.LocationServices
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PestDetailScreen(
    pestId: String,
    onBackPress: () -> Unit,
    viewModel: PestDetailViewModel = viewModel()
) {
    val pest by viewModel.pest.collectAsState()
    val currentImageIndex by viewModel.currentImageIndex.collectAsState()
    val context = LocalContext.current

    var currentLatitude by remember { mutableStateOf(0.0) }
    var currentLongitude by remember { mutableStateOf(0.0) }
    var showConfirmation by remember { mutableStateOf(false) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getLocation(context) { lat, lon ->
                currentLatitude = lat
                currentLongitude = lon
            }
        }
    }

    LaunchedEffect(pestId) {
        viewModel.loadPest(pestId)

        // Verificar permissão de localização
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation(context) { lat, lon ->
                currentLatitude = lat
                currentLongitude = lon
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    pest?.let { currentPest ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight)
                    .verticalScroll(rememberScrollState())
            ) {
                // Carrossel de imagens
                if (currentPest.images.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { currentPest.images.size })

                    LaunchedEffect(pagerState.currentPage) {
                        viewModel.setImageIndex(pagerState.currentPage)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            Image(
                                painter = rememberAsyncImagePainter(model = File(currentPest.images[page])),
                                contentDescription = currentPest.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Botão voltar
                        IconButton(
                            onClick = onBackPress,
                            modifier = Modifier
                                .padding(16.dp)
                                .size(48.dp)
                                .shadow(4.dp, CircleShape)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                contentDescription = "Voltar",
                                tint = GreenPrimary
                            )
                        }

                        // Indicadores
                        if (currentPest.images.size > 1) {
                            Row(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(currentPest.images.size) { iteration ->
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
                    }
                }

                // Informações detalhadas
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(6.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardPrague)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = currentPest.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = currentPest.description,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Link para mais informações (placeholder)
                        TextButton(
                            onClick = { /* TODO: Abrir página web ou documento */ },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = YellowAccent
                            )
                        ) {
                            Text(
                                text = "→ Ver mais sobre",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Espaço para os botões
            }

            // Botões de ação fixos na parte inferior
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(BackgroundLight)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomButton(
                    text = "ALARME FALSO",
                    onClick = {
                        viewModel.recordFalseAlarm(pestId, currentLatitude, currentLongitude)
                        onBackPress()
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = ErrorRed
                )

                CustomButton(
                    text = "PRAGA",
                    onClick = {
                        viewModel.recordPestDetection(pestId, currentLatitude, currentLongitude)
                        showConfirmation = true
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = GreenDark
                )
            }

            // Diálogo de confirmação
            if (showConfirmation) {
                AlertDialog(
                    onDismissRequest = { showConfirmation = false },
                    title = {
                        Text(
                            text = "Detecção Registrada!",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text("A detecção de ${currentPest.name} foi registrada com sucesso.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showConfirmation = false
                                onBackPress()
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

private fun getLocation(
    context: android.content.Context,
    onLocationReceived: (Double, Double) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationReceived(it.latitude, it.longitude)
            }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}