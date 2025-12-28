package com.example.a619.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("nomComplet") val nomComplet: String,
    @SerializedName("role") val role: String,
    @SerializedName("level") val level: String,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("message") val message: String
)