package com.example.a619.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val nomComplet: String,
    val role: String? = "STUDENT",
    val level: String? = "BEGINNER"
)