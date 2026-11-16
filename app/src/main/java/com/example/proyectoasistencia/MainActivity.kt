package com.example.proyectoasistencia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Estado para controlar qué pantalla se muestra
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
                    var loggedUserRut by remember { mutableStateOf("") }

                    when (val screen = currentScreen) {
                        is Screen.Login -> {
                            LoginScreen(
                                onLoginSuccess = { rut ->
                                    loggedUserRut = rut
                                    currentScreen = Screen.Home
                                }
                            )
                        }
                        is Screen.Home -> {
                            HomeScreen(
                                userRut = loggedUserRut,
                                onGenerateQrClick = {
                                    currentScreen = Screen.QrGenerator // Cambiamos a la pantalla de QR
                                },
                                onLogoutClick = {
                                    currentScreen = Screen.Login
                                }
                            )
                        }
                        is Screen.QrGenerator -> {
                            QrScreen(
                                userRut = loggedUserRut, // Le pasamos el RUT del usuario logueado
                                onBackClick = {
                                    currentScreen = Screen.Home // Volver al panel principal
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sealed class auxiliar para manejar las rutas de las pantallas de forma sencilla
sealed class Screen {
    object Login : Screen()
    object Home : Screen()

    object QrGenerator : Screen()
}
