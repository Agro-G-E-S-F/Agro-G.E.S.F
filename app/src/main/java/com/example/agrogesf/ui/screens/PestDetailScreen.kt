// ui/screens/PestDetailScreen.kt
package com.example.agrogesf.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agrogesf.R
import com.example.agrogesf.ui.components.CustomButton
import com.example.agrogesf.ui.theme.*
import com.example.agrogesf.ui.viewmodels.PestDetailViewModel
import com.google.android.gms.location.LocationServices
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PestDetailScreen(
    pestId: String,
    isFromGlossary: Boolean = false,
    onBackPress: () -> Unit,
    viewModel: PestDetailViewModel = viewModel()
) {
    val pest by viewModel.pest.collectAsState(initial = null)
    val currentImageIndex by viewModel.currentImageIndex.collectAsState()
    val context = LocalContext.current

    var currentLatitude by remember { mutableStateOf(0.0) }
    var currentLongitude by remember { mutableStateOf(0.0) }
    var showConfirmation by remember { mutableStateOf(false) }

    // Mapeamento de IDs para links da EMBRAPA
    val embrapaLinks = remember {
        mapOf(
            // Doenças - Adicione os links reais da EMBRAPA aqui
            "sarna" to "https://www.cnpuv.embrapa.br/uzum/pessego/sarna.html",
            "septoria_leaf_spot" to "https://www.embrapa.br/",
            "black_beasles" to "https://www.embrapa.br/",
            "mancha-foliar" to "https://www.embrapa.br/",
            "mancha-foliar-do-milho" to "https://www.embrapa.br/",
            "ferrugem" to "https://www.embrapa.br/",
            "huanglongbing" to "https://www.embrapa.br/",
            "va­rus_do_enrolamento_da_folha" to "https://www.embrapa.br/",
            "mofo-foliar" to "https://www.embrapa.br/",
            "mancha-de-cercospora" to "https://www.embrapa.br/",
            "ma­ldio" to "https://www.embrapa.br/",
            "requeima" to "https://www.embrapa.br/",
            "podrida£o" to "https://www.embrapa.br/",
            "podrida£o-vermelha" to "https://www.embrapa.br/",
            "crestamento_bacteriano" to "https://www.embrapa.br/",
            "antracnose" to "https://www.embrapa.br/",
            "brusone" to "https://www.embrapa.br/busca-de-publicacoes/-/publicacao/1178280/brusone-sob-manejo",
            "mancha-alvo" to "https://www.embrapa.br/",
            "va­rus_do_mosaico" to "https://www.embrapa.br/",
            "pinta-preta" to "https://www.embrapa.br/",
            "mancha-tardia" to "https://www.embrapa.br/",
            "oa­dio" to "https://www.embrapa.br/",

             "aphids" to "https://www.embrapa.br/",
            "spider_mites" to "https://www.embrapa.br/",
            "pragas_diversas" to "https://www.embrapa.br/"
        )
    }

    fun getResourceNameFromPath(path: String): String {
        val filename = path.substringAfterLast('/')
        val nameWithoutExtension = filename.substringBeforeLast('.')
        return nameWithoutExtension.lowercase(Locale.ROOT)
    }

    fun openEmbrapaLink(pestId: String) {
        val url = embrapaLinks[pestId] ?: "https://www.embrapa.br/"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

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
                // Carrossel de imagens - Agora ocupa mais espaço
                if (currentPest.images.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { currentPest.images.size })

                    LaunchedEffect(pagerState.currentPage) {
                        viewModel.setImageIndex(pagerState.currentPage)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp) // Aumentado de 400dp para 500dp
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val imagePath = currentPest.images[page]
                            val resourceName = getResourceNameFromPath(imagePath)

                            val imageResId = context.resources.getIdentifier(
                                resourceName,
                                "drawable",
                                context.packageName
                            )

                            Box(modifier = Modifier.fillMaxSize()) {
                                if (imageResId != 0) {
                                    Image(
                                        painter = painterResource(id = imageResId),
                                        contentDescription = currentPest.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GreenPrimary.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(text = "📷", fontSize = 64.sp)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Imagem não encontrada",
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    BackgroundLight.copy(alpha = 0.7f)
                                                )
                                            )
                                        )
                                )
                            }
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

                        if (currentPest.images.size > 1) {
                            Row(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(currentPest.images.size) { iteration ->
                                    val isSelected = pagerState.currentPage == iteration

                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clip(if (isSelected) RoundedCornerShape(12.dp) else CircleShape)
                                            .background(
                                                if (isSelected) YellowAccent else Color.White.copy(alpha = 0.6f)
                                            )
                                            .size(
                                                width = if (isSelected) 32.dp else 8.dp,
                                                height = 8.dp
                                            )
                                            .animateContentSize()
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .offset(y = 48.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = if (currentPest.type.name == "DOENCA")
                                ErrorRed.copy(alpha = 0.9f)
                            else
                                YellowAccent.copy(alpha = 0.9f),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = if (currentPest.type.name == "DOENCA") "DOENÇA" else "PRAGA",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Card de informações - Agora com menos padding e mais espaço
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 0.dp), // Sem padding lateral
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Nome da praga/doença
                        Text(
                            text = currentPest.name,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                            lineHeight = 38.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Divider decorativo
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .background(
                                    YellowAccent,
                                    RoundedCornerShape(2.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Descrição
                        Text(
                            text = currentPest.description,
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Justify
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botão para mais informações da EMBRAPA
                        OutlinedButton(
                            onClick = { openEmbrapaLink(pestId) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GreenPrimary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(GreenPrimary, GreenDark)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mais informações na EMBRAPA",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Espaço para os botões de ação
                        if (!isFromGlossary) {
                            Spacer(modifier = Modifier.height(120.dp))
                        } else {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }

            // Botões de ação fixos - Agora mais proeminentes
            if (!isFromGlossary) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CustomButton(
                            text = " ALARME FALSO",
                            onClick = {
                                viewModel.recordFalseAlarm(pestId, currentLatitude, currentLongitude)
                                onBackPress()
                            },
                            modifier = Modifier.weight(1f),
                            backgroundColor = ErrorRed
                        )

                        CustomButton(
                            text = "✓ CONFIRMAR PRAGA",
                            onClick = {
                                viewModel.recordPestDetection(pestId, currentLatitude, currentLongitude)
                                showConfirmation = true
                            },
                            modifier = Modifier.weight(1f),
                            backgroundColor = GreenDark
                        )
                    }
                }
            }

            // Diálogo de confirmação melhorado
            if (showConfirmation) {
                AlertDialog(
                    onDismissRequest = { showConfirmation = false },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(GreenPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✓", fontSize = 32.sp, color = GreenPrimary)
                        }
                    },
                    title = {
                        Text(
                            text = "Detecção Registrada!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = GreenPrimary
                        )
                    },
                    text = {
                        Column {
                            Text(
                                "A detecção de ${currentPest.name} foi registrada com sucesso.",
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Os dados serão sincronizados em breve.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirmation = false
                                onBackPress()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("OK", fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(24.dp)
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