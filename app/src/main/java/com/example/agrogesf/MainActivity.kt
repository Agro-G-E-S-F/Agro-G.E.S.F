package com.example.agrogesf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.agrogesf.data.models.PestType
import com.example.agrogesf.data.preferences.PreferencesManager
import com.example.agrogesf.ui.components.BottomNavItem
import com.example.agrogesf.ui.components.CustomBottomNavigation
import com.example.agrogesf.ui.screens.AuthScreen
import com.example.agrogesf.ui.screens.GPSScreen
import com.example.agrogesf.ui.screens.GlossaryScreen
import com.example.agrogesf.ui.screens.HomeScreen
import com.example.agrogesf.ui.screens.PestDetailScreen
import com.example.agrogesf.ui.screens.SettingsScreen
import com.example.agrogesf.ui.theme.AgroGESFTheme
import com.example.agrogesf.ui.theme.GreenPrimary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AgroGESFTheme {
                AgroGESFApp()
            }
        }
    }
}

@Composable
fun AgroGESFApp() {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var isLoggedIn by remember { mutableStateOf(false) }
    var isCheckingAuth by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userSession = preferencesManager.userSession.first()
            isLoggedIn = userSession != null
            isCheckingAuth = false
        }
    }

    if (isCheckingAuth) {
        LoadingScreen()
    } else {
        if (isLoggedIn) {
            MainScreenWithNavigation()
        } else {
            AuthScreen(
                onAuthSuccess = {
                    isLoggedIn = true
                }
            )
        }
    }
}

@Composable
fun MainScreenWithNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = when {
        currentRoute == null -> false
        currentRoute.startsWith("pest_detail") -> false
        else -> true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav) {
                val selectedIndex = when (currentRoute) {
                    "home" -> 0
                    "glossary_pragues", "glossary_diseases" -> 1
                    "gps" -> 2
                    "settings" -> 3
                    else -> 0
                }

                CustomBottomNavigation(
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        when (index) {
                            0 -> navController.navigate("home") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                            1 -> navController.navigate("glossary_pragues") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                            2 -> navController.navigate("gps") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                            3 -> navController.navigate("settings") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    items = listOf(
                        BottomNavItem(Icons.Default.Home, "home"),
                        BottomNavItem(Icons.Default.Book, "glossary"),
                        BottomNavItem(Icons.Default.LocationOn, "gps"),
                        BottomNavItem(Icons.Default.Info, "settings")
                    )
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    onPestClick = { pestId ->
                        navController.navigate("pest_detail/$pestId")
                    }
                )
            }

            composable("glossary_pragues") {
                GlossaryScreen(
                    pestType = PestType.PRAGA,
                    onPestClick = { pestId ->
                        navController.navigate("pest_detail/$pestId")
                    },
                    onNavigateToOtherType = {
                        navController.navigate("glossary_diseases") {
                            popUpTo("glossary_pragues") { inclusive = true }
                        }
                    }
                )
            }

            composable("glossary_diseases") {
                GlossaryScreen(
                    pestType = PestType.DOENCA,
                    onPestClick = { pestId ->
                        navController.navigate("pest_detail/$pestId")
                    },
                    onNavigateToOtherType = {
                        navController.navigate("glossary_pragues") {
                            popUpTo("glossary_diseases") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "pest_detail/{pestId}",
                arguments = listOf(navArgument("pestId") { type = NavType.StringType })
            ) { backStackEntry ->
                val pestId = backStackEntry.arguments?.getString("pestId") ?: return@composable
                PestDetailScreen(
                    pestId = pestId,
                    onBackPress = { navController.popBackStack() }
                )
            }

            composable("gps") {
                GPSScreen()
            }

            composable("settings") {
                val context = navController.context
                SettingsScreen(
                    onLogout = {
                        val preferencesManager = PreferencesManager(context)
                        kotlinx.coroutines.MainScope().launch {
                            preferencesManager.clearUserSession()
                            (context as? ComponentActivity)?.recreate()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_agro_gesf),
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}