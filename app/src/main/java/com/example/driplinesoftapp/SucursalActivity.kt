package com.example.driplinesoftapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Sucursal
import com.example.driplinesoftapp.data.SucursalResponse
import com.example.driplinesoftapp.databinding.ActivitySucursalBinding
import com.example.driplinesoftapp.ui.restaurante.SucursalAdapter
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SucursalActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySucursalBinding
    private lateinit var adapter: SucursalAdapter
    private var listaSucursalesOriginal: List<Sucursal> = emptyList()

    private val idCliente: Int by lazy { intent.getIntExtra("ID_CLIENTE", -1) }
    private val logoCliente: String? by lazy { intent.getStringExtra("LOGO") }
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
            .placeholder(R.drawable.ic_sucursales)
            .into(binding.ivLogoCliente)
    }

    private fun verificarCliente() {
        if (idCliente == -1) {
            mostrarSnackbarError("Error al obtener el cliente")
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

                        // 🔹 Mostrar el mensaje informativo si no hay sucursales
                        if (listaSucursalesOriginal.isEmpty()) {
                            binding.tvNoSucursales.visibility = View.VISIBLE
                            binding.recyclerViewSucursales.visibility = View.GONE
                        } else {
                            binding.tvNoSucursales.visibility = View.GONE
                            binding.recyclerViewSucursales.visibility = View.VISIBLE
                        }

                    } else {
                        mostrarSnackbarInfo("No se encontraron sucursales")
                        binding.tvNoSucursales.visibility = View.VISIBLE
                        binding.recyclerViewSucursales.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<SucursalResponse>, t: Throwable) {
                    mostrarCargando(false)
                    Log.e("SucursalActivity", "Error de conexión: ${t.message}", t)
                    mostrarSnackbarError("Error de conexión: ${t.message}")
                    binding.tvNoSucursales.visibility = View.VISIBLE
                    binding.recyclerViewSucursales.visibility = View.GONE
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

        // 🔹 Mostrar el mensaje informativo si no hay coincidencias
        if (sucursalesFiltradas.isEmpty()) {
            binding.tvNoSucursales.visibility = View.VISIBLE
            binding.recyclerViewSucursales.visibility = View.GONE
        } else {
            binding.tvNoSucursales.visibility = View.GONE
            binding.recyclerViewSucursales.visibility = View.VISIBLE
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    // ✅ Mensaje de Error en Rojo
    private fun mostrarSnackbarError(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.RED)
            .setTextColor(Color.WHITE)
            .setAction("Cerrar") { }
            .show()
    }

    // ✅ Mensaje Informativo en color normal
    private fun mostrarSnackbarInfo(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
            .setAction("OK") { }
            .show()
    }
}
