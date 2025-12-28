package com.example.a619

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.a619.model.AuthResponse
import com.example.a619.model.RegisterRequest
import com.example.a619.retrofit.ApiClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {

    private val roles = listOf("STUDENT", "PROF", "ADMIN")
    private val levels = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameEditText = findViewById<EditText>(R.id.username)
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirm_password)
        val nomCompletEditText = findViewById<EditText>(R.id.nom_complet)
        val roleSpinner = findViewById<Spinner>(R.id.role_spinner)
        val levelSpinner = findViewById<Spinner>(R.id.level_spinner)
        val signupButton = findViewById<Button>(R.id.signup_btn)
        val loginLink = findViewById<TextView>(R.id.login_link)

        // Setup spinners
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = roleAdapter

        val levelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levels)
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        levelSpinner.adapter = levelAdapter

        signupButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val nomComplet = nomCompletEditText.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()
            val level = levelSpinner.selectedItem.toString()

            if (validateInputs(username, email, password, confirmPassword, nomComplet)) {
                showLoading(true)

                val registerRequest = RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    nomComplet = nomComplet,
                    role = role,
                    level = level
                )

                ApiClient.apiService.register(registerRequest).enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        showLoading(false)

                        if (response.isSuccessful) {
                            val authResponse = response.body()

                            if (authResponse != null) {
                                // Save user to preferences
                                val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()

                                editor.putLong("user_id", authResponse.userId)
                                editor.putString("user_email", authResponse.email)
                                editor.putString("user_username", authResponse.username)
                                editor.putString("user_nom_complet", authResponse.nomComplet)
                                editor.putString("user_role", authResponse.role)
                                editor.putString("user_level", authResponse.level)
                                editor.putString("user_token", authResponse.token)

                                val gson = Gson()
                                val userJson = gson.toJson(authResponse)
                                editor.putString("user_object", userJson)

                                editor.putBoolean("is_logged_in", true)
                                editor.apply()

                                Toast.makeText(
                                    this@SignupActivity,
                                    "Compte créé avec succès! Bienvenue ${authResponse.nomComplet}",
                                    Toast.LENGTH_LONG
                                ).show()

                                val intent = Intent(this@SignupActivity, HomeActivity::class.java)
                                intent.putExtra("USER_ROLE", authResponse.role)
                                intent.putExtra("USER_ID", authResponse.userId)
                                intent.putExtra("USER_NAME", authResponse.nomComplet)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            handleRegisterError(response.code())
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        showLoading(false)
                        Toast.makeText(
                            this@SignupActivity,
                            "Erreur de connexion: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
        }

        loginLink.setOnClickListener {
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInputs(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        nomComplet: String
    ): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            findViewById<EditText>(R.id.username).error = "Le nom d'utilisateur est requis"
            isValid = false
        } else if (username.length < 3) {
            findViewById<EditText>(R.id.username).error = "Minimum 3 caractères"
            isValid = false
        }

        if (email.isEmpty()) {
            findViewById<EditText>(R.id.email).error = "L'email est requis"
            isValid = false
        } else if (!isValidEmail(email)) {
            findViewById<EditText>(R.id.email).error = "Format d'email invalide"
            isValid = false
        }

        if (password.isEmpty()) {
            findViewById<EditText>(R.id.password).error = "Le mot de passe est requis"
            isValid = false
        } else if (password.length < 6) {
            findViewById<EditText>(R.id.password).error = "Minimum 6 caractères"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            findViewById<EditText>(R.id.confirm_password).error = "Confirmez votre mot de passe"
            isValid = false
        } else if (password != confirmPassword) {
            findViewById<EditText>(R.id.confirm_password).error = "Les mots de passe ne correspondent pas"
            isValid = false
        }

        if (nomComplet.isEmpty()) {
            findViewById<EditText>(R.id.nom_complet).error = "Le nom complet est requis"
            isValid = false
        } else if (nomComplet.length < 2) {
            findViewById<EditText>(R.id.nom_complet).error = "Minimum 2 caractères"
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }

    private fun handleRegisterError(errorCode: Int) {
        when (errorCode) {
            400 -> {
                Toast.makeText(this, "Données invalides ou utilisateur existant", Toast.LENGTH_SHORT).show()
            }
            409 -> {
                Toast.makeText(this, "Utilisateur déjà existant", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Erreur serveur: $errorCode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val signupButton = findViewById<Button>(R.id.signup_btn)

        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        signupButton.isEnabled = !show
        findViewById<TextView>(R.id.login_link).isEnabled = !show
    }
}