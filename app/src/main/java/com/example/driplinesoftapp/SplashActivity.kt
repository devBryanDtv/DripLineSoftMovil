package com.example.driplinesoftapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.driplinesoftapp.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var tvBienvenida: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sessionManager = SessionManager(this)
        tvBienvenida = findViewById(R.id.tvBienvenida)

        // Verificar si se pasó un mensaje personalizado
        val mensajeSplash = intent.getStringExtra("MENSAJE_SPLASH")

        // Si se recibió un mensaje, mostrarlo; de lo contrario, mostrar el mensaje predeterminado
        if (!mensajeSplash.isNullOrEmpty()) {
            tvBienvenida.visibility = View.VISIBLE
            tvBienvenida.text = mensajeSplash
        } else if (!sessionManager.isLoggedIn()) {
            tvBienvenida.visibility = View.VISIBLE
            tvBienvenida.text = "Bienvenido(a) a DripLine Soft"
        } else {
            tvBienvenida.visibility = View.GONE
        }

        // Simular una carga de 2 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isLoggedIn()) {
                val rol = sessionManager.getUser()?.rol
                when (rol) {
                    "admin_cliente" -> startActivity(Intent(this, NegocioActivity::class.java))
                    "cliente_final" -> startActivity(Intent(this, MainActivity::class.java))
                    else -> startActivity(Intent(this, Login::class.java))
                }
            } else {
                startActivity(Intent(this, Login::class.java))
            }
            finish()
        }, 2000)
    }
}
