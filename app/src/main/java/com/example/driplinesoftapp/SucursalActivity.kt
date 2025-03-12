package com.example.driplinesoftapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Sucursal
import com.example.driplinesoftapp.data.SucursalResponse
import com.example.driplinesoftapp.databinding.ActivitySucursalBinding
import com.example.driplinesoftapp.ui.sucursal.SucursalAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SucursalActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySucursalBinding
    private lateinit var adapter: SucursalAdapter
    private val idCliente: Int by lazy { intent.getIntExtra("ID_CLIENTE", -1) }
    private val logoCliente: String? by lazy { intent.getStringExtra("LOGO_CLIENTE") }
    private val nombreComercial: String? by lazy { intent.getStringExtra("NOMBRE") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySucursalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarVista()
        verificarCliente()
    }

    private fun configurarVista() {
        binding.recyclerViewSucursales.layoutManager = LinearLayoutManager(this)
        mostrarLogoCliente()
    }

    private fun mostrarLogoCliente() {
        Glide.with(this)
            .load(logoCliente)
            .placeholder(R.drawable.ic_logo)
            .into(binding.ivLogoCliente)
    }

    private fun verificarCliente() {
        if (idCliente == -1) {
            mostrarMensaje("Error al obtener el cliente")
            finish()
        } else {
            cargarSucursales(idCliente)
        }
    }

    private fun cargarSucursales(idCliente: Int) {
        mostrarCargando(true)

        RetrofitClient.instance.obtenerSucursalesPorCliente(idCliente)
            .enqueue(object : Callback<SucursalResponse> {
                override fun onResponse(call: Call<SucursalResponse>, response: Response<SucursalResponse>) {
                    mostrarCargando(false)

                    if (response.isSuccessful && response.body()?.success == true) {
                        manejarListaSucursales(response.body()?.data ?: emptyList())
                    } else {
                        mostrarMensaje("No se encontraron sucursales")
                    }
                }

                override fun onFailure(call: Call<SucursalResponse>, t: Throwable) {
                    mostrarCargando(false)
                    Log.e("SucursalActivity", "Error de conexión: ${t.message}", t)
                    mostrarMensaje("Error de conexión: ${t.message}")
                }
            })
    }

    private fun manejarListaSucursales(sucursales: List<Sucursal>) {
        if (sucursales.isEmpty()) {
            binding.tvNoSucursales.mostrar()
        } else {
            binding.tvNoSucursales.ocultar()
            adapter = SucursalAdapter(sucursales, logoCliente ?: "",nombreComercial?:"no reconocido")
            binding.recyclerViewSucursales.adapter = adapter
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    // Extensiones para manejar visibilidad
    private fun View.mostrar() {
        visibility = View.VISIBLE
    }

    private fun View.ocultar() {
        visibility = View.GONE
    }
}
