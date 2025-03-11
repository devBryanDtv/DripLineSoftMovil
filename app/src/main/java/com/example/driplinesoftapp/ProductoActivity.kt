package com.example.driplinesoftapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Producto
import com.example.driplinesoftapp.data.ProductoCarrito
import com.example.driplinesoftapp.data.ProductoResponse
import com.example.driplinesoftapp.databinding.ActivityProductoBinding
import com.example.driplinesoftapp.models.CarritoDatabaseHelper
import com.example.driplinesoftapp.ui.restaurante.ProductoAdapter
import com.example.driplinesoftapp.utils.SessionManager
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductoBinding
    private lateinit var dbHelper: CarritoDatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var btnConfirmarSeleccion: Button
    private var idMenu: Int = -1
    private var idUsuario: Int = -1  // ID del usuario autenticado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = CarritoDatabaseHelper(this)
        sessionManager = SessionManager(this)

        // Obtener el usuario autenticado
        val usuario = sessionManager.getUser()
        if (usuario == null) {
            Toast.makeText(this, "Error: No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        idUsuario = usuario.idUsuario  // Asignar el ID del usuario autenticado

        idMenu = intent.getIntExtra("ID_MENU", -1)
        binding.recyclerViewProductos.layoutManager = LinearLayoutManager(this)

        // Inicializar el botón para confirmar selección
        btnConfirmarSeleccion = binding.btnConfirmarSeleccion
        btnConfirmarSeleccion.setOnClickListener {
            val productosCarrito = dbHelper.obtenerProductosPorUsuario(idUsuario)
            val intent = Intent(this, CarritoActivity::class.java)
            val productosJson = Gson().toJson(productosCarrito)
            intent.putExtra("productos_carrito", productosJson)
            startActivity(intent)
        }

        // Verificar la cantidad de productos en el carrito al cargar
        verificarCantidadCarrito()

        // Cargar los productos del menú
        if (idMenu != -1) {
            cargarProductos(idMenu)
        } else {
            Toast.makeText(this, "Error al obtener el menú", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cargarProductos(idMenu: Int) {
        binding.progressBar.visibility = View.VISIBLE

        RetrofitClient.instance.obtenerProductosPorMenu(idMenu)
            .enqueue(object : Callback<ProductoResponse> {
                override fun onResponse(call: Call<ProductoResponse>, response: Response<ProductoResponse>) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        val productos = response.body()?.data?.toMutableList() ?: mutableListOf()

                        if (productos.isEmpty()) {
                            binding.tvNoProductos.visibility = View.VISIBLE
                        } else {
                            binding.tvNoProductos.visibility = View.GONE
                            val adapter = ProductoAdapter(productos, dbHelper, sessionManager, ::agregarProductoAlCarrito) {
                                actualizarCantidadCarrito()
                            }

                            binding.recyclerViewProductos.adapter = adapter
                        }
                    } else {
                        Toast.makeText(this@ProductoActivity, "No se encontraron productos", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProductoResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ProductoActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
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

            Log.d("ProductoActivity", "Cantidad actualizada para el producto ${producto.nombreProducto}")
        } else {
            dbHelper.agregarProducto(idUsuario, producto.idProducto, 1)
            Log.d("ProductoActivity", "Producto agregado al carrito: ${producto.nombreProducto}")
        }

        (binding.recyclerViewProductos.adapter as? ProductoAdapter)?.actualizarProducto(producto)

        actualizarCantidadCarrito()
    }
}
