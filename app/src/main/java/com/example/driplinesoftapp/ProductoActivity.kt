package com.example.driplinesoftapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Producto
import com.example.driplinesoftapp.data.ProductoCarrito
import com.example.driplinesoftapp.data.ProductoResponse
import com.example.driplinesoftapp.databinding.ActivityProductoBinding
import com.example.driplinesoftapp.models.CarritoDatabaseHelper
import com.example.driplinesoftapp.ui.restaurante.ProductoAdapter
import com.example.driplinesoftapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductoBinding
    private lateinit var dbHelper: CarritoDatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ProductoAdapter
    private lateinit var btnConfirmarSeleccion: Button

    private var idMenu: Int = -1
    private var idUsuario: Int = -1

    private var nombreMenu: String? = null
    private var idSucursal: Int = -1
    private var nombreSucursal: String? = null
    private var nombreComercial: String? = null
    private var logoCliente: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = CarritoDatabaseHelper(this)
        sessionManager = SessionManager(this)

        val usuario = sessionManager.getUser()
        if (usuario == null) {
            mostrarSnackbarError("Error: No hay usuario autenticado")
            finish()
            return
        }

        idUsuario = usuario.idUsuario

        idMenu = intent.getIntExtra("ID_MENU", -1)
        nombreMenu = intent.getStringExtra("NOMBRE_MENU")
        idSucursal = intent.getIntExtra("ID_SUCURSAL", -1)
        nombreSucursal = intent.getStringExtra("NOMBRE_SUCURSAL")
        nombreComercial = intent.getStringExtra("NOMBRE_COMERCIAL")
        logoCliente = intent.getStringExtra("LOGO_CLIENTE")

        binding.recyclerViewProductos.layoutManager = LinearLayoutManager(this)

        btnConfirmarSeleccion = binding.btnConfirmarSeleccion
        btnConfirmarSeleccion.setOnClickListener {
            val productosCarrito = dbHelper.obtenerProductosPorUsuario(idUsuario)
            val intent = Intent(this, CarritoActivity::class.java).apply {
                val productosJson = Gson().toJson(productosCarrito)
                putExtra("productos_carrito", productosJson)
                putExtra("ID_MENU", idMenu)
                putExtra("NOMBRE_MENU", nombreMenu)
                putExtra("ID_SUCURSAL", idSucursal)
                putExtra("NOMBRE_SUCURSAL", nombreSucursal)
                putExtra("NOMBRE_COMERCIAL", nombreComercial)
                putExtra("LOGO_CLIENTE", logoCliente)
            }
            startActivity(intent)
        }

        verificarCantidadCarrito()
        configurarSearchView()
        cargarProductos(idMenu)
    }

    private fun configurarSearchView() {
        binding.searchViewProductos.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarProductos(query.orEmpty().trim())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarProductos(newText.orEmpty().trim())
                return true
            }
        })
    }

    private fun filtrarProductos(query: String) {
        val productosFiltrados = adapter.filtrarProductos(query.orEmpty().trim())

        // Manejar la visibilidad del mensaje "No hay productos disponibles"
        if (productosFiltrados.isEmpty()) {
            binding.tvNoProductos.visibility = View.VISIBLE
            binding.recyclerViewProductos.visibility = View.GONE
        } else {
            binding.tvNoProductos.visibility = View.GONE
            binding.recyclerViewProductos.visibility = View.VISIBLE
        }
    }


    private fun cargarProductos(idMenu: Int) {
        mostrarCargando(true)

        RetrofitClient.instance.obtenerProductosPorMenu(idMenu)
            .enqueue(object : Callback<ProductoResponse> {
                override fun onResponse(call: Call<ProductoResponse>, response: Response<ProductoResponse>) {
                    mostrarCargando(false)

                    if (response.isSuccessful && response.body()?.success == true) {
                        val productos = response.body()?.data?.toMutableList() ?: mutableListOf()

                        if (productos.isEmpty()) {
                            binding.tvNoProductos.visibility = View.VISIBLE
                            mostrarSnackbarInfo("No se encontraron productos")
                        } else {
                            binding.tvNoProductos.visibility = View.GONE
                            adapter = ProductoAdapter(productos, dbHelper, sessionManager, ::agregarProductoAlCarrito) {
                                actualizarCantidadCarrito()
                            }
                            binding.recyclerViewProductos.adapter = adapter
                        }
                    } else {
                        mostrarSnackbarInfo("No se encontraron productos")
                    }
                }

                override fun onFailure(call: Call<ProductoResponse>, t: Throwable) {
                    mostrarCargando(false)
                    mostrarSnackbarError("Error de conexión: ${t.message}")
                }
            })
    }

    private fun verificarCantidadCarrito() {
        val cantidadTotal = dbHelper.obtenerProductosPorUsuario(idUsuario).sumOf { it.cantidad }
        Log.d("ProductoActivity", "Cantidad total en el carrito al cargar: $cantidadTotal")

        if (cantidadTotal > 0) {
            btnConfirmarSeleccion.visibility = View.VISIBLE
            btnConfirmarSeleccion.text = "Confirmar Selección ($cantidadTotal)"
        } else {
            btnConfirmarSeleccion.visibility = View.GONE
        }
    }

    private fun actualizarCantidadCarrito() {
        val cantidadTotal = dbHelper.obtenerProductosPorUsuario(idUsuario).sumOf { it.cantidad }
        Log.d("ProductoActivity", "Cantidad total en el carrito: $cantidadTotal")

        if (cantidadTotal > 0) {
            btnConfirmarSeleccion.visibility = View.VISIBLE
            btnConfirmarSeleccion.text = "Confirmar Selección ($cantidadTotal)"
        } else {
            btnConfirmarSeleccion.visibility = View.GONE
        }
    }

    private fun agregarProductoAlCarrito(producto: Producto) {
        val productoExistente = dbHelper.obtenerProductoPorId(idUsuario, producto.idProducto)

        if (productoExistente != null) {
            val nuevaCantidad = productoExistente.cantidad + 1
            val productoCarrito = ProductoCarrito(idProducto = producto.idProducto, cantidad = nuevaCantidad)
            dbHelper.actualizarCantidad(idUsuario, productoCarrito)
        } else {
            dbHelper.agregarProducto(idUsuario, producto.idProducto, 1)
        }

        adapter.actualizarProducto(producto)
        actualizarCantidadCarrito()
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
