package com.example.proyectoasistencia

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/login/") // Endpoint de tu API en Django
    suspend fun login(@Body request: LoginRequest): LoginResponse
}