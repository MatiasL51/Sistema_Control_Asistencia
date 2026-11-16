package com.example.proyectoasistencia

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import androidx.camera.core.ExperimentalGetImage

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onScanSuccess: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val rojoPrincipal = Color(0xFFD32F2F)

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isVerifying by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessResponse by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validar QR de Acceso") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                // Vista de la cámara con CameraX
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val executor = Executors.newSingleThreadExecutor()

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setTargetResolution(Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            val scanner = BarcodeScanning.getClient()

                            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && !isVerifying) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                barcode.rawValue?.let { qrValue ->
                                                    if (!isVerifying) {
                                                        isVerifying = true
                                                        coroutineScope.launch {
                                                            try {
                                                                // Petición HTTP a Django a través de Retrofit
                                                                val request = ValidateQrRequest(qr_token = qrValue)
                                                                val response = RetrofitClient.apiService.validateQr(request)

                                                                if (response.success) {
                                                                    isSuccessResponse = true
                                                                    statusMessage = response.message
                                                                    onScanSuccess(response.rut ?: "Usuario")
                                                                } else {
                                                                    isSuccessResponse = false
                                                                    statusMessage = response.message
                                                                }
                                                            } catch (e: Exception) {
                                                                isSuccessResponse = false
                                                                statusMessage = "Error de conexión con el servidor"
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            // Error en la lectura del barcode
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (exc: Exception) {
                                exc.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Marco visual de enfoque
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(4.dp, rojoPrincipal, MaterialTheme.shapes.medium)
                )

                // Capa inferior con mensajes de estado o indicador de carga
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isVerifying && statusMessage == null) {
                        CircularProgressIndicator(color = rojoPrincipal)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Validando código con el servidor...",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else if (statusMessage != null) {
                        Surface(
                            color = if (isSuccessResponse) Color(0xFF2E7D32).copy(alpha = 0.9f) else Color(0xFFC62828).copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = statusMessage!!,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (!isSuccessResponse) {
                            Button(
                                onClick = {
                                    statusMessage = null
                                    isVerifying = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = rojoPrincipal)
                            ) {
                                Text("Escanear Otro QR")
                            }
                        }
                    } else {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Apunta la cámara al código QR del usuario",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Se requiere acceso a la cámara para escanear y validar códigos QR.",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = rojoPrincipal)
                    ) {
                        Text("Conceder Permiso")
                    }
                }
            }
        }
    }
}