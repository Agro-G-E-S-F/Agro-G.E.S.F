// ui/screens/GPSScreen.kt
package com.example.agrogesf.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.agrogesf.R
import com.example.agrogesf.ui.theme.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File

@Composable
fun GPSScreen() {
    val context = LocalContext.current
    var currentLatitude by remember { mutableStateOf<Double?>(null) }
    var currentLongitude by remember { mutableStateOf<Double?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var heatMapBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Callback para atualizações de localização
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                }
            }
        }
    }

    // Launcher para permissão de localização
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Verificar permissões e iniciar atualizações de localização
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            startLocationUpdates(fusedLocationClient, locationCallback)
        }

        // Carregar mapa de calor (se existir)
        loadHeatMap(context)?.let { bitmap ->
            heatMapBitmap = bitmap
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header GPS
            GPSHeader()

            // Mapa de calor
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (heatMapBitmap != null) {
                        Image(
                            bitmap = heatMapBitmap!!.asImageBitmap(),
                            contentDescription = "Mapa de Calor",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        // Indicador de posição do usuário
                        if (currentLatitude != null && currentLongitude != null) {
                            UserLocationIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        // Estado vazio - sem mapa carregado
                        EmptyMapState()
                    }

                    // Informações de coordenadas
                    if (currentLatitude != null && currentLongitude != null) {
                        CoordinatesCard(
                            latitude = currentLatitude!!,
                            longitude = currentLongitude!!,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        )
                    } else if (!hasLocationPermission) {
                        PermissionDeniedCard(
                            onRequestPermission = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp)
                        )
                    } else {
                        LoadingLocationCard(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GPSHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = CardGold)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_gps),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "GPS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun UserLocationIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .shadow(8.dp, CircleShape)
    ) {
        // Círculo externo pulsante
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GreenPrimary.copy(alpha = 0.3f), CircleShape)
        )

        // Círculo central
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(GreenPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location_arrow),
                contentDescription = "Sua localização",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CoordinatesCard(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Coordenadas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lat: ${String.format("%.6f", latitude)}",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = "Lon: ${String.format("%.6f", longitude)}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun LoadingLocationCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = GreenPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Obtendo localização...",
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun EmptyMapState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_map),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = GreenPrimary.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mapa não carregado",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Conecte-se ao WiFi AGRO_GESF_DATA para receber o mapa de calor",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PermissionDeniedCard(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location_off),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = ErrorRed
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Permissão de localização necessária",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Para usar o GPS, precisamos acessar sua localização",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Permitir acesso")
            }
        }
    }
}

private fun startLocationUpdates(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    locationCallback: LocationCallback
) {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L // Atualizar a cada 5 segundos
    ).apply {
        setMinUpdateIntervalMillis(2000L)
    }.build()

    try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

private fun loadHeatMap(context: android.content.Context): Bitmap? {
    return try {
        val heatMapFile = File(context.filesDir, "heatmap/heatmap.png")
        if (heatMapFile.exists()) {
            BitmapFactory.decodeFile(heatMapFile.absolutePath)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}