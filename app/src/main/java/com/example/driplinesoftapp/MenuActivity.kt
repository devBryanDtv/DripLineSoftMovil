package com.example.driplinesoftapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.MenuResponse
import com.example.driplinesoftapp.databinding.ActivityMenuBinding
import com.example.driplinesoftapp.ui.restaurante.MenuAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var adapter: MenuAdapter
    private var idSucursal: Int = -1
    private var nombreSucursal: String? = null
    private var nombreComercial: String? = null
    private var logoCliente: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos del Intent
        idSucursal = intent.getIntExtra("ID_SUCURSAL", -1)
        nombreSucursal = intent.getStringExtra("NOMBRE_SUCURSAL")
        nombreComercial = intent.getStringExtra("NOMBRE_COMERCIAL")
        logoCliente = intent.getStringExtra("LOGO_CLIENTE")

        binding.recyclerViewMenus.layoutManager = LinearLayoutManager(this)

        if (idSucursal != -1) {
            cargarMenus(idSucursal)
        } else {
            Toast.makeText(this, "Error al obtener la sucursal", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cargarMenus(idSucursal: Int) {
        binding.progressBar.visibility = View.VISIBLE

        RetrofitClient.instance.obtenerMenusPorSucursal(idSucursal)
            .enqueue(object : Callback<MenuResponse> {
                override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body()?.success == true) {
                        val menus = response.body()?.data ?: emptyList()

                        if (menus.isEmpty()) {
                            binding.tvNoMenus.visibility = View.VISIBLE
                        } else {
                            binding.tvNoMenus.visibility = View.GONE
                            adapter = MenuAdapter(
                                menus,
                                idSucursal,
                                nombreSucursal,
                                nombreComercial,
                                logoCliente
                            )
                            binding.recyclerViewMenus.adapter = adapter
                        }
                    } else {
                        Toast.makeText(this@MenuActivity, "No se encontraron menús", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@MenuActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
