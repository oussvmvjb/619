package com.example.a619

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a619.model.AuthResponse
import com.example.a619.model.LoginRequest
import com.example.a619.retrofit.ApiClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginBtn)
        val createAccount = findViewById<TextView>(R.id.createAccount)

        checkAutoLogin()

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(username, password)

            ApiClient.apiService.login(loginRequest).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val authResponse = response.body()

                        if (authResponse != null) {
                            saveUserToPrefs(authResponse)

                            Toast.makeText(
                                this@LoginActivity,
                                "Bienvenue: ${authResponse.nomComplet}",
                                Toast.LENGTH_LONG
                            ).show()

                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.putExtra("USER_ROLE", authResponse.role)
                            intent.putExtra("USER_ID", authResponse.userId)
                            intent.putExtra("USER_NAME", authResponse.nomComplet)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Erreur: données utilisateur non disponibles",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        when (response.code()) {
                            401 -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Nom d'utilisateur ou mot de passe incorrect",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            400 -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Données invalides",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Erreur serveur: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Erreur de connexion au serveur: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        createAccount.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUserToPrefs(authResponse: AuthResponse) {
        val editor = sharedPreferences.edit()
        editor.putLong("user_id", authResponse.userId)
        editor.putString("user_email", authResponse.email)
        editor.putString("user_username", authResponse.username)
        editor.putString("user_nom_complet", authResponse.nomComplet)
        editor.putString("user_role", authResponse.role)
        editor.putString("user_level", authResponse.level)
        editor.putString("user_token", authResponse.token)

        // Save the whole object as JSON
        val gson = Gson()
        val userJson = gson.toJson(authResponse)
        editor.putString("user_object", userJson)

        editor.putBoolean("is_logged_in", true)
        editor.apply()
    }

    private fun checkAutoLogin() {
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        val token = sharedPreferences.getString("user_token", null)

        if (isLoggedIn && token != null) {
            val intent = Intent(this, HomeActivity::class.java)
            val userRole = sharedPreferences.getString("user_role", "STUDENT") ?: "STUDENT"
            intent.putExtra("USER_ROLE", userRole)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        fun getCurrentUser(context: Context): AuthResponse? {
            val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
            val userJson = sharedPref.getString("user_object", null)

            return if (userJson != null) {
                val gson = Gson()
                gson.fromJson(userJson, AuthResponse::class.java)
            } else {
                null
            }
        }

        fun getUserId(context: Context): Long {
            val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
            return sharedPref.getLong("user_id", 0)
        }

        fun getUserEmail(context: Context): String {
            val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
            return sharedPref.getString("user_email", "") ?: ""
        }

        fun getUserFullName(context: Context): String {
            val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
            return sharedPref.getString("user_nom_complet", "") ?: ""
        }

        fun getUserRole(context: Context): String {
            val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
            return sharedPref.getString("user_role", "STUDENT") ?: "STUDENT"
        }

        fun getUserLevel(context: Context): String {
            val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
            return sharedPref.getString("user_level", "BEGINNER") ?: "BEGINNER"
        }

        fun getToken(context: Context): String? {
            val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
            return sharedPref.getString("user_token", null)
        }

        fun isLoggedIn(context: Context): Boolean {
            val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
            return sharedPref.getBoolean("is_logged_in", false) &&
                    sharedPref.getString("user_token", null) != null
        }

        fun logout(context: Context) {
            val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()
        }
    }
}