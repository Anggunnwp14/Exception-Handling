package com.example.ukonnect2.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// API Service sesuai dengan server Node.js kamu
interface ApiService {

    // Endpoint untuk login
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Endpoint untuk register
    @POST("register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>
}

// === Request & Response Models ===

// Data class untuk request login
data class LoginRequest(
    val username: String,
    val password: String
)

// Data class untuk response login
data class LoginResponse(
    val message: String,
    val user: String? = null
)

// Data class untuk request register
data class RegisterRequest(
    val username: String,
    val password: String
)

// Data class untuk response register
data class RegisterResponse(
    val message: String,
    val userId: Int? = null
)
