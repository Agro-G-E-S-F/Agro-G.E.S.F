package com.example.agrogesf.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.agrogesf.ml.ONNXPestClassifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBackPress: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Estados
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var classificationResult by remember { mutableStateOf<com.example.agrogesf.ml.ClassificationResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var modelReady by remember { mutableStateOf(false) }  // ← ADICIONE

    // ✅ UMA ÚNICA INSTÂNCIA do classifier
    val classifier = remember { ONNXPestClassifier(context) }

    // Inicializar modelo ao abrir a tela
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val assets = context.assets.list("")
            println("📁 Arquivos encontrados em assets:")
            assets?.forEach {
                println("  ✓ $it")
            }

            // ✅ Inicializar a mesma instância
            val success = classifier.initialize()  // ← REMOVEU o "val classifier ="

            if (success) {
                println("✅ Modelo inicializado com SUCESSO!")
                modelReady = true  // ← ADICIONE
            } else {
                println("❌ Falha ao inicializar modelo")
                errorMessage = "Erro ao carregar modelo de IA"
            }
        }
    }

    // Limpar recursos ao sair
    DisposableEffect(Unit) {
        onDispose {
            classifier.close()
        }
    }

    // Permissões
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    // ✅ Mostrar loading enquanto carrega
    if (!modelReady) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Carregando modelo de IA...")
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        return  // ← Não continua até o modelo estar pronto
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = loadBitmapFromUri(context, it)
            if (bitmap != null) {
                capturedImage = bitmap
                scope.launch {
                    isProcessing = true
                    errorMessage = null

                    val result = withContext(Dispatchers.IO) {
                        classifier.classify(bitmap)
                    }

                    if (result != null) {
                        classificationResult = result
                    } else {
                        errorMessage = "Erro ao processar imagem"
                    }

                    isProcessing = false
                }
            } else {
                errorMessage = "Erro ao carregar imagem da galeria"
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Identificar Praga") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, "Galeria")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mostrar erro se houver
            errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }

            if (permissionsState.allPermissionsGranted) {
                if (capturedImage == null) {
                    // Modo câmera
                    CameraPreviewView(
                        onImageCaptured = { bitmap ->
                            capturedImage = bitmap
                            scope.launch {
                                isProcessing = true
                                errorMessage = null

                                val result = withContext(Dispatchers.IO) {
                                    classifier.classify(bitmap)
                                }

                                if (result != null) {
                                    classificationResult = result
                                } else {
                                    errorMessage = "Erro ao processar imagem"
                                }

                                isProcessing = false
                            }
                        }
                    )
                } else {
                    // Modo resultado
                    ResultView(
                        image = capturedImage!!,
                        result = classificationResult,
                        isProcessing = isProcessing,
                        onRetakeClick = {
                            capturedImage = null
                            classificationResult = null
                            errorMessage = null
                        },
                        onSaveClick = {
                            // TODO: Implementar salvamento no histórico
                            onBackPress()
                        }
                    )
                }
            } else {
                // Tela de permissões
                PermissionRequestView(
                    onRequestClick = {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewView(
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Botão de captura
        FloatingActionButton(
            onClick = {
                imageCapture?.let { capture ->
                    val outputFile = File(
                        context.cacheDir,
                        "IMG_${System.currentTimeMillis()}.jpg"
                    )

                    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                    capture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val bitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
                                val rotatedBitmap = rotateBitmap(bitmap, 90f)
                                onImageCaptured(rotatedBitmap)
                                outputFile.delete()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                exception.printStackTrace()
                            }
                        }
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(72.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "Capturar",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun ResultView(
    image: Bitmap,
    result: com.example.agrogesf.ml.ClassificationResult?,
    isProcessing: Boolean,
    onRetakeClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Imagem capturada
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "Imagem capturada",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resultado
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isProcessing) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Processando imagem...")
                } else if (result != null) {
                    Text(
                        text = "Resultado",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.className,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Confiança: ${result.getConfidencePercentage()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nível: ${result.getConfidenceLevel()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    Text(
                        text = "Aguardando classificação...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botões
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onRetakeClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nova Foto")
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier.weight(1f),
                enabled = result != null
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar")
            }
        }
    }
}

@Composable
private fun PermissionRequestView(
    onRequestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Permissão necessária",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Precisamos acessar sua câmera para identificar pragas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestClick) {
            Text("Permitir Câmera")
        }
    }
}

// Funções auxiliares
private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}