package com.example.ukonnect2.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 🔧 Ganti IP sesuai hasil ipconfig laptop kamu
    // Pastikan HP & laptop di jaringan WiFi yang sama
    private const val BASE_URL = "http://10.235.190.197:3000"

    // Retrofit instance untuk koneksi ke server Node.js
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
