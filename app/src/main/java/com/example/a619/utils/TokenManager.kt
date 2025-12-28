package com.example.a619.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "auth_token"
        private const val USER_ID_KEY = "user_id"
        private const val USERNAME_KEY = "username"
        private const val EMAIL_KEY = "email"
        private const val ROLE_KEY = "role"
        private const val LEVEL_KEY = "level"
        private const val NOM_COMPLET_KEY = "nom_complet"
    }

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(TOKEN_KEY, "Bearer $token").apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    fun saveUserInfo(
        userId: Long,
        username: String,
        email: String,
        nomComplet: String,
        role: String,
        level: String
    ) {
        sharedPreferences.edit().apply {
            putLong(USER_ID_KEY, userId)
            putString(USERNAME_KEY, username)
            putString(EMAIL_KEY, email)
            putString(NOM_COMPLET_KEY, nomComplet)
            putString(ROLE_KEY, role)
            putString(LEVEL_KEY, level)
            apply()
        }
    }

    fun getUserId(): Long = sharedPreferences.getLong(USER_ID_KEY, -1)
    fun getUsername(): String = sharedPreferences.getString(USERNAME_KEY, "") ?: ""
    fun getEmail(): String = sharedPreferences.getString(EMAIL_KEY, "") ?: ""
    fun getNomComplet(): String = sharedPreferences.getString(NOM_COMPLET_KEY, "") ?: ""
    fun getRole(): String = sharedPreferences.getString(ROLE_KEY, "") ?: ""
    fun getLevel(): String = sharedPreferences.getString(LEVEL_KEY, "") ?: ""

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }
}