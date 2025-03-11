package com.example.driplinesoftapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.LoginRequest
import com.example.driplinesoftapp.data.LoginResponse
import com.example.driplinesoftapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)


        sessionManager = SessionManager(this)

        // Verificar si el usuario ya está logueado
        if (sessionManager.isLoggedIn()) {
            navigateToMainActivity(sessionManager.getUser()?.rol ?: "")
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val contraseña = etPassword.text.toString().trim()

            if (email.isEmpty() || contraseña.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.login(LoginRequest(email, contraseña))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            val usuario = response.body()?.data
                            if (usuario != null) {
                                // Guardar datos en las preferencias
                                sessionManager.saveUser(usuario)
                                // Mostrar log para confirmar que se guardaron los datos en SharedPreferences
                                val savedUser = sessionManager.getUser()
                                Log.d("SESSION", "Usuario guardado en sesión: $savedUser")

                                // Redirigir según el rol del usuario
                                navigateToMainActivity(usuario.rol)
                            } else {
                                Toast.makeText(this@Login, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@Login, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@Login, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.d("DEBUG", "${t.message}")
                    }
                })
        }
        // Agregar efecto de clic al TextView
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java) // Cambia Register por el nombre correcto de tu actividad de registro
            startActivity(intent)
        }
    }

    private fun navigateToMainActivity(rol: String) {
        if (rol == "admin_cliente") {
            startActivity(Intent(this@Login, NegocioActivity::class.java))
        } else {
            startActivity(Intent(this@Login, MainActivity::class.java))
        }
        finish()
    }
}
