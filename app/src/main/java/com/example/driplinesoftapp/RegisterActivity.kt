package com.example.driplinesoftapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.RegisterRequest
import com.example.driplinesoftapp.data.RegisterResponse
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        tilName = findViewById(R.id.tilName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBar)

        etName.addTextChangedListener(textWatcher)
        etEmail.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
        etConfirmPassword.addTextChangedListener(textWatcher)

        btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val nombre = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (!validarCampos()) return

        showLoading(true)

        val request = RegisterRequest(nombre, email, password)

        RetrofitClient.instance.register(request)
            .enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    showLoading(false)

                    if (response.isSuccessful && response.body()?.exito == true) {
                        val intent = Intent(this@RegisterActivity, Login::class.java).apply {
                            putExtra("REGISTRO_EXITOSO", "Registro exitoso. Ahora puedes iniciar sesión.")
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        mostrarSnackbar(response.body()?.mensaje ?: "Error en el registro")
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    showLoading(false)
                    mostrarSnackbar("Error de conexión: ${t.message}")
                }
            })
    }

    private fun validarCampos(): Boolean {
        val nombre = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        var esValido = true

        if (nombre.isEmpty()) {
            tilName.error = "El nombre es obligatorio"
            esValido = false
        } else {
            tilName.error = null
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Correo electrónico no válido"
            esValido = false
        } else {
            tilEmail.error = null
        }

        if (password.length < 8) {
            tilPassword.error = "La contraseña debe tener al menos 8 caracteres"
            esValido = false
        } else {
            tilPassword.error = null
        }

        if (password != confirmPassword) {
            tilConfirmPassword.error = "Las contraseñas no coinciden"
            esValido = false
        } else {
            tilConfirmPassword.error = null
        }

        btnRegister.isEnabled = esValido
        btnRegister.setBackgroundColor(
            if (esValido) ContextCompat.getColor(this, android.R.color.background_dark)
            else Color.parseColor("#A0A0A0")
        )

        return esValido
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            validarCampos()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            btnRegister.text = ""
            progressBar.visibility = View.VISIBLE
            btnRegister.isEnabled = false
        } else {
            btnRegister.text = "Registrarse"
            progressBar.visibility = View.GONE
            validarCampos() // Para revalidar el estado del botón
        }
    }

    private fun mostrarSnackbar(mensaje: String) {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, mensaje, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.RED)
            .setTextColor(Color.WHITE)
            .setAction("OK") { }
            .show()
    }
}
