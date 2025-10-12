// ui/screens/AdminLoginScreen.kt
package com.example.agrogesf.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agrogesf.R
import com.example.agrogesf.ui.components.CustomButton
import com.example.agrogesf.ui.components.CustomTextField
import com.example.agrogesf.ui.theme.*
import com.example.agrogesf.ui.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onAdminLoginSuccess: () -> Unit,
    onBackPress: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary)
    ) {
        // Botão voltar
        IconButton(
            onClick = onBackPress,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_agro_gesf),
                contentDescription = "Logo AGRO G.E.S.F",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "PAINEL ADMIN",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = YellowAccent
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Acesso Restrito",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            CustomTextField(
                value = email,
                onValueChange = {
                    email = it
                    showError = false
                },
                label = "Email Admin:"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = password,
                onValueChange = {
                    password = it
                    showError = false
                },
                label = "Senha:",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (showError) {
                Text(
                    text = errorMessage,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            CustomButton(
                text = "ACESSAR PAINEL",
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        showError = true
                        errorMessage = "Preencha todos os campos"
                        return@CustomButton
                    }

                    if (viewModel.verifyAdminLogin(email, password)) {
                        onAdminLoginSuccess()
                    } else {
                        showError = true
                        errorMessage = "Credenciais inválidas"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}