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
                    // Estado para controlar qué pantalla se está mostrando actualmente
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
                    var loggedUserRut by remember { mutableStateOf("") }

                    // Manejador central de navegación (Router) basado en Jetpack Compose
                    when (currentScreen) {
                        is Screen.Login -> {
                            LoginScreen(
                                onLoginSuccess = { rut, isAdmin ->
                                    loggedUserRut = rut
                                    // Redirección condicional según el rol devuelto (Admin vs Usuario Regular)
                                    currentScreen = if (isAdmin) {
                                        Screen.AdminPanel
                                    } else {
                                        Screen.Home
                                    }
                                }
                            )
                        }
                        is Screen.Home -> {
                            HomeScreen(
                                userRut = loggedUserRut,
                                onGenerateQrClick = {
                                    currentScreen = Screen.QrGenerator
                                },
                                onLogoutClick = {
                                    currentScreen = Screen.Login
                                }
                            )
                        }
                        is Screen.QrGenerator -> {
                            QrScreen(
                                userRut = loggedUserRut,
                                onBackClick = {
                                    currentScreen = Screen.Home
                                }
                            )
                        }
                        is Screen.AdminPanel -> {
                            AdminScreen(
                                onOpenScannerClick = {
                                    currentScreen = Screen.AdminScanner // Abre la cámara para escanear
                                },
                                onBackClick = {
                                    currentScreen = Screen.Login // Cierra sesión al volver
                                }
                            )
                        }
                        is Screen.AdminScanner -> {
                            ScannerScreen(
                                onScanSuccess = { scannedQrContent ->
                                    // Aquí puedes procesar el QR escaneado (ej. registrarlo o validarlo)
                                    // Y luego regresar al panel de administrador
                                    currentScreen = Screen.AdminPanel
                                },
                                onBackClick = {
                                    currentScreen = Screen.AdminPanel
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sealed class para definir de forma limpia y segura las rutas de navegación de la app.
 */
sealed class Screen {
    object Login : Screen()
    object Home : Screen()
    object QrGenerator : Screen()
    object AdminPanel : Screen()
    object AdminScanner : Screen()
}