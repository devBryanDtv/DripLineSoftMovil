package com.example.driplinesoftapp

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
import com.example.driplinesoftapp.data.ProductoCarritoRequest
import com.example.driplinesoftapp.data.ProductoRequest
import com.example.driplinesoftapp.data.ProductoResponse_2
import com.example.driplinesoftapp.databinding.ActivityCarritoBinding
import com.example.driplinesoftapp.models.CarritoDatabaseHelper
import com.example.driplinesoftapp.ui.restaurante.CarritoAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private lateinit var dbHelper: CarritoDatabaseHelper
    private lateinit var btnRealizarPedido: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = CarritoDatabaseHelper(this)

        binding.recyclerViewCarrito.layoutManager = LinearLayoutManager(this)

        btnRealizarPedido = binding.btnRealizarPedido
        btnRealizarPedido.setOnClickListener {
            // Redirigir a la pantalla de realizar pedido
        }

        cargarProductosCarrito()
    }

    private fun cargarProductosCarrito() {
        val productosDelCarrito = dbHelper.obtenerProductosPorUsuario(1)

        if (productosDelCarrito.isNotEmpty()) {
            val productosIds = productosDelCarrito.map { ProductoCarrito(it.idProducto, it.cantidad) }
            obtenerDetallesDeProductos(productosIds)
        } else {
            Toast.makeText(this, "No hay productos en el carrito", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerDetallesDeProductos(productosIds: List<ProductoCarrito>) {
        Log.d("CarritoActivity", "Iniciando solicitud para obtener detalles de productos...")

        val productosRequest = productosIds.map { ProductoCarritoRequest(it.idProducto) }
        val productoRequest = ProductoRequest(productosRequest)

        RetrofitClient.instance.obtenerDetallesProductosCarrito(productoRequest)
            .enqueue(object : Callback<ProductoResponse_2> {
                override fun onResponse(call: Call<ProductoResponse_2>, response: Response<ProductoResponse_2>) {
                    Log.d("CarritoActivity", "Petición realizada. Código de respuesta: ${response.code()}")

                    if (response.isSuccessful && response.body()?.success == true) {
                        val productosConDetalles = response.body()?.data ?: emptyList()
                        Log.d("CarritoActivity", "Productos con detalles: ${productosConDetalles.size}")

                        productosConDetalles.forEach { producto ->
                            producto.cantidad = dbHelper.obtenerProductoPorId(1, producto.idProducto)?.cantidad ?: 0
                        }

                        val adapter = CarritoAdapter(
                            productosConDetalles.toMutableList(),
                            dbHelper,
                            ::actualizarCantidadCarrito
                        ) { nuevoSubtotal ->
                            actualizarSubtotalEnVista(nuevoSubtotal)
                        }

                        binding.recyclerViewCarrito.adapter = adapter
                        actualizarSubtotal(productosConDetalles)

                    } else {
                        Log.e("CarritoActivity", "Error en la respuesta: ${response.message()}")
                        Toast.makeText(this@CarritoActivity, "Error al obtener detalles: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProductoResponse_2>, t: Throwable) {
                    Log.e("CarritoActivity", "Error en la petición: ${t.message}")
                    Toast.makeText(this@CarritoActivity, "Error al obtener detalles: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ➤ Nueva función para actualizar el subtotal en la vista
    private fun actualizarSubtotalEnVista(nuevoSubtotal: Double) {
        binding.tvSubtotal.text = "Subtotal: $${"%.2f".format(nuevoSubtotal)}"
    }


    private fun actualizarSubtotal(productos: List<Producto>) {
        val subtotal = productos.sumOf {
            it.precio * (dbHelper.obtenerProductoPorId(1, it.idProducto)?.cantidad ?: 0)
        }
        binding.tvSubtotal.text = "Subtotal: $${"%.2f".format(subtotal)}"
    }

    private fun actualizarCantidadCarrito() {
        val cantidadTotal = dbHelper.obtenerProductosPorUsuario(1).sumOf { it.cantidad }
        if (cantidadTotal > 0) {
            btnRealizarPedido.visibility = View.VISIBLE
            btnRealizarPedido.text = "Realizar Pedido ($cantidadTotal)"
        } else {
            btnRealizarPedido.visibility = View.GONE
        }
    }
}
