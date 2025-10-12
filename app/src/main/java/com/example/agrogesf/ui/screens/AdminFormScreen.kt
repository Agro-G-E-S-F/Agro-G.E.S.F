package com.example.agrogesf.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.agrogesf.data.models.ImageUploadState
import com.example.agrogesf.data.models.PestType
import com.example.agrogesf.ui.components.CustomButton
import com.example.agrogesf.ui.components.CustomTextField
import com.example.agrogesf.ui.theme.*
import com.example.agrogesf.ui.viewmodels.AdminViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFormScreen(
    pestId: String? = null,
    onSaveSuccess: () -> Unit,
    onBackPress: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val form by viewModel.currentPestForm.collectAsState()
    val uploadedImages by viewModel.uploadedImages.collectAsState()
    val imageUploadState by viewModel.imageUploadState.collectAsState()

    val isEditMode = pestId != null

    // Carregar praga para edição
    LaunchedEffect(pestId) {
        if (pestId != null) {
            viewModel.loadPestForEdit(pestId)
        } else {
            viewModel.clearForm()
        }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Editar Praga" else "Nova Praga")
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Tipo de praga/doença
            Text(
                text = "Tipo *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypeChip(
                    text = "Praga",
                    isSelected = form.type == PestType.PRAGA,
                    onClick = { viewModel.updateForm { currentForm -> currentForm.copy(type = PestType.PRAGA) } }
                )

                TypeChip(
                    text = "Doença",
                    isSelected = form.type == PestType.DOENCA,
                    onClick = { viewModel.updateForm { currentForm -> currentForm.copy(type = PestType.DOENCA) } }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nome
            CustomTextField(
                value = form.name,
                onValueChange = { newValue ->
                    viewModel.updateForm { currentForm -> currentForm.copy(name = newValue) }
                },
                label = "Nome da Praga/Doença *"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nome Científico
            CustomTextField(
                value = form.scientificName,
                onValueChange = { newValue ->
                    viewModel.updateForm { currentForm -> currentForm.copy(scientificName = newValue) }
                },
                label = "Nome Científico (Opcional)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Descrição
            OutlinedTextField(
                value = form.description,
                onValueChange = { newValue ->
                    viewModel.updateForm { currentForm -> currentForm.copy(description = newValue) }
                },
                label = { Text("Descrição Geral *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFFF5F5DC),
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sintomas
            OutlinedTextField(
                value = form.symptoms,
                onValueChange = { newValue ->
                    viewModel.updateForm { currentForm -> currentForm.copy(symptoms = newValue) }
                },
                label = { Text("Sintomas (Opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFFF5F5DC),
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prevenção
            OutlinedTextField(
                value = form.prevention,
                onValueChange = { newValue ->
                    viewModel.updateForm { currentForm -> currentForm.copy(prevention = newValue) }
                },
                label = { Text("Prevenção (Opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFFF5F5DC),
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tratamento
            OutlinedTextField(
                value = form.treatment,
                onValueChange = { newValue ->
                    viewModel.updateForm { currentForm -> currentForm.copy(treatment = newValue) }
                },
                label = { Text("Tratamento (Opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFFF5F5DC),
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Imagens
            Text(
                text = "Imagens * (mínimo 1)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Upload state
            AnimatedVisibility(
                visible = imageUploadState is ImageUploadState.Uploading ||
                        imageUploadState is ImageUploadState.Error
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (imageUploadState is ImageUploadState.Error)
                            ErrorRed.copy(alpha = 0.1f)
                        else
                            GreenPrimary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (val state = imageUploadState) {
                            is ImageUploadState.Uploading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = GreenPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Enviando ${state.fileName}...",
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                            is ImageUploadState.Error -> {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Erro no upload",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorRed
                                    )
                                    Text(
                                        state.message,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.resetImageUploadState() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Fechar",
                                        tint = TextSecondary
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Lista de imagens
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botão adicionar
                item {
                    Card(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GreenPrimary.copy(alpha = 0.1f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Adicionar imagem",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Adicionar",
                                    fontSize = 12.sp,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }
                }

                // Imagens carregadas
                items(uploadedImages) { imagePath ->
                    ImageItem(
                        imagePath = imagePath,
                        onRemove = { viewModel.removeImage(imagePath) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botão salvar
            CustomButton(
                text = if (isEditMode) "ATUALIZAR" else "SALVAR",
                onClick = { viewModel.savePest() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TypeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GreenPrimary else Color(0xFFF5F5DC)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ImageItem(
    imagePath: String,
    onRemove: () -> Unit
) {
    Box(modifier = Modifier.size(120.dp)) {
        Image(
            painter = rememberAsyncImagePainter(model = File(imagePath)),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        // Botão remover
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(28.dp)
                .background(ErrorRed, CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remover",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}