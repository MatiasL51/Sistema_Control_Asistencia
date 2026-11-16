package com.example.proyectoasistencia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

// Modelo de datos para simular el registro de QRs usados
data class QrUsageLog(
    val id: String,
    val rut: String,
    val time: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onOpenScannerClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val rojoPrincipal = Color(0xFFD32F2F)

    // Estados para la pestaña de Crear Usuario
    var newRut by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var createMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Coroutine scope necesario para ejecutar las peticiones de Retrofit
    val coroutineScope = rememberCoroutineScope()

    // Estado simulado para el historial de QRs usados
    val qrLogs = remember {
        mutableStateListOf(
            QrUsageLog("1", "12.345.678-9", "10:45 AM", "Usado Exitosamente"),
            QrUsageLog("2", "9.876.543-2", "11:15 AM", "Usado Exitosamente"),
            QrUsageLog("3", "15.432.109-K", "12:00 PM", "Caducado por Tiempo")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administrador") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = rojoPrincipal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Selector de Pestañas (Tabs)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = rojoPrincipal
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text("Crear Usuario") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    text = { Text("QR Usados") }
                )
            }

            // Contenido según la pestaña seleccionada
            when (selectedTabIndex) {
                0 -> {
                    // --- PESTAÑA 1: CREAR USUARIO ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Registrar Nuevo Usuario",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = rojoPrincipal
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = newRut,
                            onValueChange = {
                                newRut = it.filter { char -> char.isDigit() || char == 'k' || char == 'K' }
                                createMessage = null
                            },
                            label = { Text("RUT del usuario (ej: 123456789)") },
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

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                createMessage = null
                            },
                            label = { Text("Contraseña temporal") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = rojoPrincipal,
                                focusedLabelColor = rojoPrincipal,
                                cursorColor = rojoPrincipal
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (newRut.isBlank() || newPassword.isBlank()) {
                                    isError = true
                                    createMessage = "Por favor completa todos los campos."
                                } else if (newRut.length < 7) {
                                    isError = true
                                    createMessage = "El RUT ingresado no es válido."
                                } else {
                                    // Lanzamos la petición de red con Retrofit hacia Django
                                    coroutineScope.launch {
                                        try {
                                            val requestData = CreateUserRequest(
                                                rut = newRut,
                                                password = newPassword
                                            )

                                            // Llamada real al servicio backend
                                            val response = RetrofitClient.apiService.createUser(requestData)

                                            if (response.success) {
                                                isError = false
                                                createMessage = "¡Usuario con RUT $newRut creado y guardado en MySQL con éxito!"
                                                newRut = ""
                                                newPassword = ""
                                            } else {
                                                isError = true
                                                createMessage = response.message ?: "No se pudo crear el usuario."
                                            }
                                        } catch (e: HttpException) {
                                            isError = true
                                            val errorBody = e.response()?.errorBody()?.string()
                                            createMessage = "Error del servidor: $errorBody"
                                        } catch (e: Exception) {
                                            isError = true
                                            createMessage = "Error de red: ${e.localizedMessage}"
                                        }
                                    }
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
                            Text(text = "Crear Cuenta", fontSize = 16.sp)
                        }

                        if (createMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = createMessage!!,
                                color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                1 -> {
                    // --- PESTAÑA 2: HISTORIAL DE QR USADOS ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { onOpenScannerClick() },
                            colors = ButtonDefaults.buttonColors(containerColor = rojoPrincipal),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Text(text = "Escanear QR de Usuario con la Cámara")
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Historial de Escaneos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedButton(
                                onClick = {
                                    val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                    val randomRut = "${(10..25).random()}.${(100..999).random()}.${(100..999).random()}-K"
                                    qrLogs.add(0, QrUsageLog(id = UUID.randomUUID().toString(), rut = randomRut, time = currentTime, status = "Usado Exitosamente"))
                                }
                            ) {
                                Text("Simular Escaneo", color = rojoPrincipal)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(qrLogs) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "RUT: ${log.rut}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Hora: ${log.time}",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            color = if (log.status.contains("Exitosamente")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = log.status,
                                                color = if (log.status.contains("Exitosamente")) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminScreenPreview() {
    MaterialTheme {
        AdminScreen(onBackClick = {})
    }
}