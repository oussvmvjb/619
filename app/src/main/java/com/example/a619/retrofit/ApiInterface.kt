package com.example.a619.retrofit

import com.example.a619.model.AuthResponse
import com.example.a619.model.LoginRequest
import com.example.a619.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiInterface {

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<AuthResponse>

    @POST("auth/register")
    fun register(@Body registerRequest: RegisterRequest): Call<AuthResponse>

    @POST("auth/validate")
    fun validateToken(@Header("Authorization") token: String): Call<Map<String, Any>>
}