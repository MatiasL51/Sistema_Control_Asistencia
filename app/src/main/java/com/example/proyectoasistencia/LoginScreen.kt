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

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Definición de un tono de rojo moderno (Material Red 700)
    val rojoPrincipal = Color(0xFFD32F2F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título en color rojo
        Text(
            text = "Bienvenido",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = rojoPrincipal
        )

        Text(
            text = "Ingresa tu RUT de para continuar",
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("12345678-9") },
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

        // Mensaje de error
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Iniciar Sesión en rojo
        Button(
            onClick = {
                if (rut.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor, completa todos los campos."
                } else if (rut.length < 7) {
                    errorMessage = "El RUT ingresado no es válido."
                } else {
                    onLoginSuccess(rut)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = rojoPrincipal,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Iniciar Sesión", fontSize = 16.sp)
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
            LoginScreen(onLoginSuccess = {})
        }
    }
}