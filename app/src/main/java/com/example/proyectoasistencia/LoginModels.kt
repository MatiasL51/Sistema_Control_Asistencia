package com.example.proyectoasistencia

// Datos que envías al backend
data class LoginRequest(
    val rut: String,
    val password: String
)

// Datos que te responde el backend (incluyendo el rol o si es admin)
data class LoginResponse(
    val token: String,
    val is_admin: Boolean,
    val message: String?
)
// Lo que envías a Django para validar
data class ValidateQrRequest(
    val qr_token: String
)

// Lo que te responde Django tras la validación
data class ValidateQrResponse(
    val success: Boolean,
    val message: String,
    val rut: String?,
    val timestamp: String?
)