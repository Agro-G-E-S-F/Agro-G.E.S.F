// ui/screens/SettingsScreen.kt
package com.example.agrogesf.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agrogesf.R
import com.example.agrogesf.data.preferences.PreferencesManager
import com.example.agrogesf.ui.theme.*
import com.example.agrogesf.ui.viewmodels.SyncState
import com.example.agrogesf.ui.viewmodels.SyncViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SyncViewModel = viewModel()
) {
    val syncState by viewModel.syncState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState(initial = 0L)
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val userSession by preferencesManager.userSession.collectAsState(initial = null)

    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        SettingsHeader()

        Spacer(modifier = Modifier.height(16.dp))

        // Informações do usuário
        userSession?.let { session ->
            UserInfoCard(
                name = session.name,
                email = session.email
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seção de Sincronização
        SyncSection(
            lastSyncTime = lastSyncTime,
            syncState = syncState,
            onSync = { viewModel.syncToFirebase() },
            onDismissError = { viewModel.resetSyncState() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sobre o App
        AboutSection()

        Spacer(modifier = Modifier.height(16.dp))

        // Botão de Logout
        LogoutButton(onClick = { showLogoutDialog = true })

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Diálogo de confirmação de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Sair da conta",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Tem certeza que deseja sair?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Text("Sair")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SettingsHeader() {
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
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Configurações",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun UserInfoCard(
    name: String,
    email: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(GreenPrimary.copy(alpha = 0.2f), RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SyncSection(
    lastSyncTime: Long,
    syncState: SyncState,
    onSync: () -> Unit,
    onDismissError: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sincronizar dados",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (lastSyncTime > 0) {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        Text(
                            text = "Última sync: ${dateFormat.format(Date(lastSyncTime))}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    } else {
                        Text(
                            text = "Nunca sincronizado",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                when (syncState) {
                    is SyncState.Syncing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = GreenPrimary,
                            strokeWidth = 3.dp
                        )
                    }

                    else -> {
                        IconButton(
                            onClick = onSync,
                            enabled = syncState !is SyncState.Syncing
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sincronizar",
                                tint = GreenPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Mensagem de status
            AnimatedVisibility(
                visible = syncState is SyncState.Success || syncState is SyncState.Error,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (syncState) {
                            is SyncState.Success -> SuccessGreen.copy(alpha = 0.1f)
                            is SyncState.Error -> ErrorRed.copy(alpha = 0.1f)
                            else -> Color.Transparent
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (syncState is SyncState.Success)
                                    Icons.Default.CheckCircle
                                else
                                    Icons.Default.Error,
                                contentDescription = null,
                                tint = if (syncState is SyncState.Success) SuccessGreen else ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (syncState) {
                                    is SyncState.Success -> "Dados sincronizados!"
                                    is SyncState.Error -> syncState.message
                                    else -> ""
                                },
                                fontSize = 13.sp,
                                color = if (syncState is SyncState.Success) SuccessGreen else ErrorRed
                            )
                        }

                        IconButton(
                            onClick = onDismissError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sincronize seus dados com a nuvem quando tiver conexão com a internet",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}


@Composable
private fun AboutSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Sobre o Aplicativo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingItem(
                icon = Icons.Default.Info,
                title = "Versão",
                subtitle = "1.0.0"
            )

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.Gray.copy(alpha = 0.2f)
            )

            SettingItem(
                icon = Icons.Default.Description,
                title = "AGRO G.E.S.F",
                subtitle = "Sistema de Gerenciamento de Pragas e Doenças Agrícolas"
            )

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.Gray.copy(alpha = 0.2f)
            )

            SettingItem(
                icon = Icons.Default.People,
                title = "Desenvolvido para",
                subtitle = "Agricultores em áreas rurais sem acesso constante à internet"
            )
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GreenPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ErrorRed)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Sair",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Sair da Conta",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}