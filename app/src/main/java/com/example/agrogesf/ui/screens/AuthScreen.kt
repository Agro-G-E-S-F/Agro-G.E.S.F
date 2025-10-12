// ui/screens/AuthScreen.kt
package com.example.agrogesf.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.agrogesf.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agrogesf.ui.components.CustomButton
import com.example.agrogesf.ui.components.CustomTextField
import com.example.agrogesf.ui.theme.*
import com.example.agrogesf.ui.viewmodels.AuthState
import com.example.agrogesf.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onAdminClick: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val isLoginMode by viewModel.isLoginMode.collectAsState()
    val authState by viewModel.authState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Observar estado de autenticação
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary)
    ) {
        // Imagem de fundo com overlay
        Image(
            painter = painterResource(id = R.drawable.auth_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        // Botão Admin no canto superior direito
        IconButton(
            onClick = onAdminClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Painel Admin",
                tint = YellowAccent,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_agro_gesf),
                contentDescription = "Logo AGRO G.E.S.F",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "AGRO G.E.S.F",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tabs de Login/Cadastro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabButton(
                    text = "LOGIN",
                    isSelected = isLoginMode,
                    onClick = {
                        if (!isLoginMode) {
                            viewModel.toggleAuthMode()
                            name = ""
                            confirmPassword = ""
                        }
                    }
                )

                TabButton(
                    text = "CADASTRO",
                    isSelected = !isLoginMode,
                    onClick = {
                        if (isLoginMode) {
                            viewModel.toggleAuthMode()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Formulário com animação de transição
            AnimatedContent(
                targetState = isLoginMode,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { if (targetState) -it else it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)) with
                            slideOutHorizontally(
                                targetOffsetX = { if (targetState) it else -it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                },
                label = "auth_animation"
            ) { loginMode ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!loginMode) {
                        CustomTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Nome:"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    CustomTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = if (loginMode) "Nome:" else "Email:"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Senha:",
                        isPassword = true
                    )

                    if (!loginMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Repetir senha:",
                            isPassword = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mensagem de erro
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Botão de ação
            CustomButton(
                text = if (isLoginMode) "ENTRAR" else "CADASTRAR",
                onClick = {
                    if (isLoginMode) {
                        viewModel.login(email, password)
                    } else {
                        viewModel.register(name, email, password, confirmPassword)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            )

            // Loading indicator
            if (authState is AuthState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    color = YellowAccent,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedOffset by animateFloatAsState(
        targetValue = if (isSelected) 0f else 10f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tab_offset"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.offset(y = animatedOffset.dp)
        ) {
            Text(
                text = text,
                color = if (isSelected) YellowAccent else Color.White,
                fontSize = if (isSelected) 24.sp else 20.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(3.dp)
                    .background(YellowAccent)
            )
        }
    }
}