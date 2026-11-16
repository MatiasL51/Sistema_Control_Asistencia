package com.example.proyectoasistencia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (String, Boolean) -> Unit
) {
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val rojoPrincipal = Color(0xFFD32F2F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = rojoPrincipal
        )

        Text(
            text = "Ingresa tus credenciales para continuar",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Campo de RUT
        OutlinedTextField(
            value = rut,
            onValueChange = { input ->
                rut = input.filter { it.isDigit() || it == 'k' || it == 'K' }
                errorMessage = null
            },
            label = { Text("RUT (ej: 12345678-9)") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = rojoPrincipal,
                focusedLabelColor = rojoPrincipal,
                cursorColor = rojoPrincipal
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar/Ocultar contraseña")
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = rojoPrincipal,
                focusedLabelColor = rojoPrincipal,
                cursorColor = rojoPrincipal
            )
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Iniciar Sesión conectado a Retrofit
        Button(
            onClick = {
                if (rut.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor, completa todos los campos."
                } else {
                    isLoading = true
                    errorMessage = null

                    // Credenciales fijas de prueba para el Administrador
                    val adminRutValido = "123456789"
                    val adminPasswordValida = "admin123"

                    if (rut == adminRutValido && password == adminPasswordValida) {
                        isLoading = false
                        // Acceso concedido como Administrador de prueba local
                        onLoginSuccess(rut, true)
                    } else {
                        coroutineScope.launch {
                            try {
                                // Preparamos el objeto con los datos solicitados por Django
                                val request = LoginRequest(rut = rut, password = password)

                                // Ejecutamos la petición HTTP en segundo plano mediante Retrofit
                                val response = RetrofitClient.apiService.login(request)

                                isLoading = false

                                // 👈 Aquí utilizamos "response" para evaluar si el backend aceptó el login
                                if (response.success) {
                                    // Si el login es exitoso, avanzamos de pantalla (false = usuario normal o admin según tu backend)
                                    onLoginSuccess(rut, false)
                                } else {
                                    errorMessage = response.message ?: "RUT o contraseña incorrectos."
                                }

                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Error de conexión o credenciales inválidas."
                            }
                        }
                    }
                }
            },
            enabled = !isLoading, // Se desactiva mientras carga para evitar múltiples clics
            colors = ButtonDefaults.buttonColors(
                containerColor = rojoPrincipal,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Iniciar Sesión", fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginScreen(onLoginSuccess = { _, _ -> })
        }
    }
}