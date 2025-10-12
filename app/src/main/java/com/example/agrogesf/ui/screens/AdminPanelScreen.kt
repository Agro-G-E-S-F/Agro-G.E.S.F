// ui/screens/AdminPanelScreen.kt
package com.example.agrogesf.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.agrogesf.data.models.AdminAction
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.PestType
import com.example.agrogesf.ui.theme.*
import com.example.agrogesf.ui.viewmodels.AdminViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onAddNew: () -> Unit,
    onEditPest: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val pragues by viewModel.pragues.collectAsState()
    val diseases by viewModel.diseases.collectAsState()
    val adminAction by viewModel.adminAction.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pestToDelete by remember { mutableStateOf<Pest?>(null) }

    // Observar ações admin
    LaunchedEffect(adminAction) {
        if (adminAction is AdminAction.Success) {
            kotlinx.coroutines.delay(3000)
            viewModel.resetAction()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Painel Admin",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    // Botão sincronizar
                    IconButton(onClick = { viewModel.syncFromFirebase() }) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar",
                            tint = Color.White
                        )
                    }

                    // Botão logout
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sair",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNew,
                containerColor = YellowAccent,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, "Adicionar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
        ) {
            // Status message
            AnimatedVisibility(
                visible = adminAction !is AdminAction.Idle,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                AdminActionCard(
                    action = adminAction,
                    onDismiss = { viewModel.resetAction() }
                )
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = GreenPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("PRAGAS (${pragues.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("DOENÇAS (${diseases.size})") }
                )
            }

            // Lista
            val currentList = if (selectedTab == 0) pragues else diseases

            if (currentList.isEmpty()) {
                EmptyAdminList(type = if (selectedTab == 0) "pragas" else "doenças")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList) { pest ->
                        PestAdminCard(
                            pest = pest,
                            onEdit = { onEditPest(pest.id) },
                            onDelete = {
                                pestToDelete = pest
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmação de exclusão
    if (showDeleteDialog && pestToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja realmente excluir '${pestToDelete!!.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePest(pestToDelete!!.id)
                        showDeleteDialog = false
                        pestToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AdminActionCard(
    action: AdminAction,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (action) {
                is AdminAction.Success -> SuccessGreen.copy(alpha = 0.9f)
                is AdminAction.Error -> ErrorRed.copy(alpha = 0.9f)
                is AdminAction.Loading -> GreenPrimary
                else -> Color.Transparent
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (action) {
                    is AdminAction.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Processando...", color = Color.White)
                    }
                    is AdminAction.Success -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(action.message, color = Color.White)
                    }
                    is AdminAction.Error -> {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(action.message, color = Color.White, fontSize = 14.sp)
                    }
                    else -> {}
                }
            }

            if (action !is AdminAction.Loading) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PestAdminCard(
    pest: Pest,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagem
            if (pest.images.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = File(pest.images.first())),
                    contentDescription = pest.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pest.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${pest.images.size} imagens",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = if (pest.type == PestType.PRAGA) "Praga" else "Doença",
                    fontSize = 12.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Ações
            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = GreenPrimary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = ErrorRed
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAdminList(type: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Inventory,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = GreenPrimary.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nenhuma $type cadastrada",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Clique no botão + para adicionar",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}