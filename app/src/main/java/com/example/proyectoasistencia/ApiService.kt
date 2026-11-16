package com.example.proyectoasistencia

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// --- MODELOS DE PETICIÓN Y RESPUESTA ---

data class LoginRequest(
    val rut: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val token: String?
)

data class CreateUserRequest(
    val rut: String,
    val password: String
)

data class CreateUserResponse(
    val success: Boolean,
    val message: String
)

data class ValidateQrRequest(
    val rut: String
)

data class ValidateQrResponse(
    val success: Boolean,
    val message: String,
    val rut: String?
)

// --- INTERFAZ DE RETROFIT ---

interface ApiService {

    // Petición POST para el inicio de sesión (envía datos en el cuerpo JSON)
    @POST("api/login/")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // Petición POST para crear un nuevo usuario desde el panel de administrador
    @POST("api/create-user/")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): CreateUserResponse

    // Petición POST para validar el código QR escaneado con la cámara
    @POST("api/validate-qr/")
    suspend fun validateQr(
        @Body request: ValidateQrRequest
    ): ValidateQrResponse

}