package com.example.driplinesoftapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.LoginRequest
import com.example.driplinesoftapp.data.LoginResponse
import com.example.driplinesoftapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvRegister: TextView
    private lateinit var sessionManager: SessionManager

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tilEmail = findViewById(R.id.tilEmail) // TextInputLayout del email
        tilPassword = findViewById(R.id.tilPassword) // TextInputLayout de la contraseña
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        tvRegister = findViewById(R.id.tvRegister)

        sessionManager = SessionManager(this)

        // Mostrar mensaje si se recibe del registro
        intent.getStringExtra("REGISTRO_EXITOSO")?.let { mensaje ->
            mostrarSnackbar(mensaje)
        }

        if (sessionManager.isLoggedIn()) {
            navigateToMainActivity(sessionManager.getUser()?.rol ?: "")
        }

        // Validación en tiempo real
        etEmail.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val contraseña = etPassword.text.toString().trim()

            if (email.isEmpty() || contraseña.isEmpty()) {
                mostrarError("Por favor completa todos los campos")
                return@setOnClickListener
            }

            showLoading(true)

            RetrofitClient.instance.login(LoginRequest(email, contraseña))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        showLoading(false)

                        if (response.isSuccessful && response.body()?.success == true) {
                            val usuario = response.body()?.data
                            if (usuario != null) {
                                sessionManager.saveUser(usuario)
                                val savedUser = sessionManager.getUser()
                                Log.d("SESSION", "Usuario guardado en sesión: $savedUser")
                                navigateToMainActivity(usuario.rol)
                            } else {
                                mostrarError("Usuario no encontrado")
                            }
                        } else {
                            mostrarError("Credenciales incorrectas")
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        showLoading(false)
                        mostrarError("Error de conexión: ${t.message}")
                        Log.d("DEBUG", "${t.message}")
                    }
                })
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            validarCampos()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No se requiere implementación
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // No se requiere implementación
        }
    }

    private fun validarCampos() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        val esEmailValido = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val esPasswordValida = password.length in 8..16

        if (!esEmailValido) {
            tilEmail.error = "Correo electrónico no válido"
        } else {
            tilEmail.error = null
        }

        if (!esPasswordValida) {
            tilPassword.error = "La contraseña debe tener entre 8 y 16 caracteres"
        } else {
            tilPassword.error = null
        }

        val habilitarBoton = esEmailValido && esPasswordValida
        btnLogin.isEnabled = habilitarBoton

        btnLogin.setBackgroundColor(
            if (habilitarBoton) ContextCompat.getColor(this, android.R.color.background_dark)
            else Color.parseColor("#A0A0A0")
        )
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            btnLogin.text = ""
            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false
        } else {
            btnLogin.text = "Iniciar Sesión"
            progressBar.visibility = View.GONE
            validarCampos()  // Validar el estado del botón tras finalizar la solicitud
        }
    }

    private fun mostrarError(mensaje: String) {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, mensaje, Snackbar.LENGTH_LONG)
            .setDuration(5000)
            .setBackgroundTint(Color.RED)
            .setTextColor(Color.WHITE)
            .setAction("OK") { }
            .show()
    }

    private fun navigateToMainActivity(rol: String) {
        if (rol == "admin_cliente") {
            startActivity(Intent(this@Login, NegocioActivity::class.java))
        } else {
            startActivity(Intent(this@Login, MainActivity::class.java))
        }
        finish()
    }

    private fun mostrarSnackbar(mensaje: String) {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, mensaje, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.GREEN)
            .setTextColor(Color.WHITE)
            .setAction("OK") { }
            .show()
    }
}
