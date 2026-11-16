package com.example.proyectoasistencia

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(
    userRut: String,
    onBackClick: () -> Unit
) {
    val rojoPrincipal = Color(0xFFD32F2F)
    val totalTimeSeconds = 120

    var timeLeft by remember { mutableStateOf(totalTimeSeconds) }
    var isExpired by remember { mutableStateOf(false) }
    var isUsed by remember { mutableStateOf(false) }

    // Genera el código QR automáticamente usando el RUT del usuario al cargar la pantalla
    val qrBitmap = remember(userRut) {
        generateQRCode("ACCESS_TOKEN_RUT:$userRut")
    }

    // Temporizador automático de cuenta regresiva
    LaunchedEffect(key1 = isExpired, key2 = isUsed) {
        if (!isExpired && !isUsed) {
            timeLeft = totalTimeSeconds
            while (timeLeft > 0 && !isExpired && !isUsed) {
                delay(1000L)
                timeLeft--
            }
            if (timeLeft == 0) {
                isExpired = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Código QR de Acceso") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RUT: $userRut",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = rojoPrincipal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Muestra este código para validar tu acceso",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            qrBitmap?.let { bitmap ->
                // Formato de tiempo MM:SS
                val minutes = timeLeft / 60
                val seconds = timeLeft % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (timeLeft < 30) Color.Red else rojoPrincipal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!isExpired && !isUsed) "Expira en: $timeFormatted" else "Código Caducado",
                        fontWeight = FontWeight.Bold,
                        color = if (timeLeft < 30) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }

                LinearProgressIndicator(
                    progress = { timeLeft.toFloat() / totalTimeSeconds.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (timeLeft < 30) Color.Red else rojoPrincipal,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.size(240.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isExpired && !isUsed) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Código QR de Acceso",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color.Yellow,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isExpired) "TIEMPO\nAGOTADO" else "QR YA UTILIZADO",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de acción inferior
                if (isExpired || isUsed) {
                    Button(
                        onClick = {
                            isExpired = false
                            isUsed = false
                            timeLeft = totalTimeSeconds
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = rojoPrincipal,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(text = "Generar Nuevo Código QR", fontSize = 16.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { isUsed = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Simular Uso del QR", color = rojoPrincipal)
                    }
                }
            }
        }
    }
}

/**
 * Función auxiliar de ZXing
 */
fun generateQRCode(text: String, width: Int = 512, height: Int = 512): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            width,
            height
        )
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QrScreenPreview() {
    MaterialTheme {
        QrScreen(userRut = "12.345.678-9", onBackClick = {})
    }
}