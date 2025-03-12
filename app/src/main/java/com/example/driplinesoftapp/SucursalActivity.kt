package com.example.driplinesoftapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    private var listaSucursalesOriginal: List<Sucursal> = emptyList()

    private val idCliente: Int by lazy { intent.getIntExtra("ID_CLIENTE", -1) }
    private val logoCliente: String? by lazy { intent.getStringExtra("LOGO_CLIENTE") }
    private val nombreComercial: String? by lazy { intent.getStringExtra("NOMBRE") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySucursalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarVista()
        verificarCliente()
        configurarSearchView()
    }

    private fun configurarVista() {
        binding.recyclerViewSucursales.layoutManager = LinearLayoutManager(this)
        adapter = SucursalAdapter(emptyList(), logoCliente ?: "", nombreComercial ?: "No reconocido")
        binding.recyclerViewSucursales.adapter = adapter
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
                        listaSucursalesOriginal = response.body()?.data ?: emptyList()
                        adapter.actualizarLista(listaSucursalesOriginal)
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

    private fun configurarSearchView() {
        binding.searchViewSucursales.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarSucursales(query?.trim() ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarSucursales(newText?.trim() ?: "")
                return true
            }
        })
    }

    private fun filtrarSucursales(query: String) {
        val sucursalesFiltradas = listaSucursalesOriginal.filter { sucursal ->
            sucursal.nombreSucursal.contains(query, ignoreCase = true) ||
                    sucursal.direccion?.contains(query, ignoreCase = true) == true ||
                    sucursal.telefono?.contains(query, ignoreCase = true) == true
        }

        adapter.actualizarLista(sucursalesFiltradas)
    }

    private fun mostrarCargando(mostrar: Boolean) {
        binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
