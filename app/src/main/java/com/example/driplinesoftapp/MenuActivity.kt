package com.example.driplinesoftapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Menu
import com.example.driplinesoftapp.data.MenuResponse
import com.example.driplinesoftapp.databinding.ActivityMenuBinding
import com.example.driplinesoftapp.ui.restaurante.MenuAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
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
    private var listaMenusOriginal: List<Menu> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idSucursal = intent.getIntExtra("ID_SUCURSAL", -1)
        nombreSucursal = intent.getStringExtra("NOMBRE_SUCURSAL")
        nombreComercial = intent.getStringExtra("NOMBRE_COMERCIAL")
        logoCliente = intent.getStringExtra("LOGO_CLIENTE")

        binding.recyclerViewMenus.layoutManager = LinearLayoutManager(this)
        adapter = MenuAdapter(emptyList(), idSucursal, nombreSucursal, nombreComercial, logoCliente)
        binding.recyclerViewMenus.adapter = adapter

        if (idSucursal != -1) {
            cargarMenus(idSucursal)
        } else {
            mostrarSnackbarError("Error al obtener la sucursal")
            finish()
        }

        configurarSearchView()
        configurarTabLayout()
    }

    private fun cargarMenus(idSucursal: Int) {
        mostrarCargando(true)

        RetrofitClient.instance.obtenerMenusPorSucursal(idSucursal)
            .enqueue(object : Callback<MenuResponse> {
                override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                    mostrarCargando(false)

                    if (response.isSuccessful && response.body()?.success == true) {
                        listaMenusOriginal = response.body()?.data ?: emptyList()

                        if (listaMenusOriginal.isEmpty()) {
                            binding.tvNoMenus.visibility = View.VISIBLE
                            mostrarSnackbarInfo("No se encontraron menús")
                        } else {
                            binding.tvNoMenus.visibility = View.GONE
                            filtrarMenus()
                        }
                    } else {
                        mostrarSnackbarInfo("No se encontraron menús")
                    }
                }

                override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                    mostrarCargando(false)
                    mostrarSnackbarError("Error de conexión: ${t.message}")
                }
            })
    }

    private fun configurarSearchView() {
        binding.searchViewMenus.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarMenus(query?.trim() ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarMenus(newText?.trim() ?: "")
                return true
            }
        })
    }

    private fun configurarTabLayout() {
        binding.tabLayoutCategorias.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filtrarMenus(binding.searchViewMenus.query.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filtrarMenus(query: String = "") {
        val categoriaSeleccionada = binding.tabLayoutCategorias.selectedTabPosition
        val categorias = listOf(
            null, "bebidas calientes", "bebidas frías", "postres", "snacks", "promociones",
            "ensaladas", "entradas", "platos fuertes", "comida rápida", "carnes", "mariscos",
            "sopas y caldos", "comida mexicana", "comida italiana", "comida oriental", "vegetariano", "vegano"
        )
        val categoriaFiltro = categorias.getOrNull(categoriaSeleccionada)

        val menusFiltrados = listaMenusOriginal.filter { menu ->
            val coincideCategoria = categoriaFiltro == null || menu.categoria.equals(categoriaFiltro, ignoreCase = true)
            val coincideTexto = query.isEmpty() || menu.nombreMenu.contains(query, ignoreCase = true)
            coincideCategoria && coincideTexto
        }

        adapter.actualizarLista(menusFiltrados)
        binding.tvNoMenus.visibility = if (menusFiltrados.isEmpty()) View.VISIBLE else View.GONE
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
