package com.example.driplinesoftapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.*
import com.example.driplinesoftapp.databinding.ActivityCarritoBinding
import com.example.driplinesoftapp.models.CarritoDatabaseHelper
import com.example.driplinesoftapp.ui.restaurante.CarritoAdapter
import com.example.driplinesoftapp.utils.SessionManager
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private lateinit var dbHelper: CarritoDatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var btnRealizarPedido: Button
    private var subtotalActual: Double = 0.0
    private var idUsuario: Int = -1

    // Datos adicionales
    private var nombreMenu: String? = null
    private var idSucursal: Int = -1
    private var nombreSucursal: String? = null
    private var nombreComercial: String? = null
    private var logoCliente: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("CarritoActivity", "Iniciando CarritoActivity")

        dbHelper = CarritoDatabaseHelper(this)
        sessionManager = SessionManager(this)

        val usuario = sessionManager.getUser()
        if (usuario == null) {
            mostrarMensaje("Error: No hay usuario autenticado.")
            finish()
            return
        }

        idUsuario = usuario.idUsuario

        // Recibir datos del Intent
        nombreMenu = intent.getStringExtra("NOMBRE_MENU")
        idSucursal = intent.getIntExtra("ID_SUCURSAL", -1)
        nombreSucursal = intent.getStringExtra("NOMBRE_SUCURSAL")
        nombreComercial = intent.getStringExtra("NOMBRE_COMERCIAL")
        logoCliente = intent.getStringExtra("LOGO_CLIENTE")

        Log.d("CarritoActivity", "Datos recibidos: nombreMenu=$nombreMenu, idSucursal=$idSucursal, nombreSucursal=$nombreSucursal, nombreComercial=$nombreComercial, logoCliente=$logoCliente")

        binding.recyclerViewCarrito.layoutManager = LinearLayoutManager(this)

        btnRealizarPedido = binding.btnRealizarPedido
        btnRealizarPedido.setOnClickListener {
            enviarDatosAPedidoActivity()
        }

        cargarProductosCarrito()
    }

    private fun cargarProductosCarrito() {
        val productosDelCarrito = dbHelper.obtenerProductosPorUsuario(idUsuario)

        if (productosDelCarrito.isNotEmpty()) {
            val productosIds = productosDelCarrito.map { it.idProducto }

            Log.d("CarritoActivity", "Productos encontrados en el carrito: $productosIds")

            if (productosIds.isEmpty()) {
                mostrarMensaje("Error: No se encontraron productos válidos en el carrito.")
                return
            }

            if (nombreMenu == null || nombreSucursal == null || nombreComercial == null) {
                Log.d("CarritoActivity", "Datos del negocio no recibidos. Consultando desde API...")
                obtenerDatosNegocioDesdeAPI(productosIds)
            } else {
                Log.d("CarritoActivity", "Datos del negocio recibidos correctamente por Intent.")
            }

            val productosCarrito = productosDelCarrito.map { ProductoCarrito(it.idProducto, it.cantidad) }
            obtenerDetallesDeProductos(productosCarrito)  // ✅ CORREGIDO: Se llama correctamente
        } else {
            mostrarMensaje("No hay productos en el carrito.")
            binding.tvSubtotal.text = "Subtotal: $0.00"
            btnRealizarPedido.isEnabled = false
        }
    }

    private fun obtenerDatosNegocioDesdeAPI(idsProductos: List<Int>) {
        if (idsProductos.isEmpty()) {
            mostrarMensaje("No hay productos para obtener datos del negocio.")
            return
        }

        val requestBody = DatosPedidoRequest(idsProductos)

        // Log adicional para verificar los datos enviados
        Log.d("CarritoActivity", "➡️ Enviando datos a la API: ${Gson().toJson(requestBody)}")

        RetrofitClient.instance.obtenerDatosPedido(requestBody)
            .enqueue(object : Callback<DatosNegocioResponse> {
                override fun onResponse(call: Call<DatosNegocioResponse>, response: Response<DatosNegocioResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val datosNegocio = response.body()?.datos_negocio
                        nombreMenu = datosNegocio?.nombre_menu
                        nombreSucursal = datosNegocio?.nombre_sucursal
                        nombreComercial = datosNegocio?.nombre_comercial
                        logoCliente = datosNegocio?.logo_cliente

                        Log.d("CarritoActivity", "✅ Datos del negocio obtenidos correctamente del API")
                        Log.d("CarritoActivity", """
                        Datos del negocio:
                        - Nombre Menú: $nombreMenu
                        - ID Sucursal: $idSucursal
                        - Nombre Sucursal: $nombreSucursal
                        - Nombre Comercial: $nombreComercial
                        - Logo Cliente: $logoCliente
                    """.trimIndent())
                        mostrarMensaje("Datos del negocio obtenidos correctamente.")
                    } else {
                        Log.d("CarritoActivity", "❌ Error al obtener datos del negocio desde el API - Código: ${response.code()}")
                        Log.d("CarritoActivity", "Respuesta del servidor: ${response.errorBody()?.string()}")
                        mostrarMensaje("No se pudieron obtener los datos del negocio.")
                    }
                }

                override fun onFailure(call: Call<DatosNegocioResponse>, t: Throwable) {
                    Log.e("CarritoActivity", "❗ Error en la conexión al obtener datos del negocio", t)
                    mostrarMensaje("Error en la conexión: ${t.message}")
                }
            })
    }



    private fun obtenerDetallesDeProductos(productosIds: List<ProductoCarrito>) {
        val productosRequest = productosIds.map { ProductoCarritoRequest(it.idProducto) }
        val productoRequest = ProductoRequest(productosRequest)

        RetrofitClient.instance.obtenerDetallesProductosCarrito(productoRequest)
            .enqueue(object : Callback<ProductoResponse_2> {
                override fun onResponse(call: Call<ProductoResponse_2>, response: Response<ProductoResponse_2>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val productosConDetalles = response.body()?.data ?: emptyList()

                        Log.d("CarritoActivity", "Detalles de productos obtenidos correctamente.")

                        val adapter = CarritoAdapter(
                            productosConDetalles.toMutableList(),
                            dbHelper,
                            sessionManager,
                            nombreMenu,
                            nombreSucursal,
                            nombreComercial,
                            logoCliente
                        ) { nuevoSubtotal ->
                            actualizarSubtotalEnVista(nuevoSubtotal)
                        }

                        binding.recyclerViewCarrito.adapter = adapter
                        actualizarSubtotal(productosConDetalles)
                    } else {
                        Log.d("CarritoActivity", "Error al obtener detalles de productos.")
                        mostrarMensaje("Error al obtener detalles: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ProductoResponse_2>, t: Throwable) {
                    Log.e("CarritoActivity", "Error en la conexión al obtener detalles de productos", t)
                    mostrarMensaje("Error en la conexión: ${t.message}")
                }
            })
    }

    private fun actualizarSubtotalEnVista(nuevoSubtotal: Double) {
        subtotalActual = nuevoSubtotal
        binding.tvSubtotal.text = "Subtotal: $${"%.2f".format(nuevoSubtotal)}"
    }

    private fun enviarDatosAPedidoActivity() {
        val productos = dbHelper.obtenerProductosPorUsuario(idUsuario)

        val productosJson = Gson().toJson(productos)

        val intent = Intent(this, PedidoActivity::class.java).apply {
            putExtra("subtotal", subtotalActual)
            putExtra("productos", productosJson)
            putExtra("NOMBRE_MENU", nombreMenu)
            putExtra("ID_SUCURSAL", idSucursal)
            putExtra("NOMBRE_SUCURSAL", nombreSucursal)
            putExtra("NOMBRE_COMERCIAL", nombreComercial)
            putExtra("LOGO_CLIENTE", logoCliente)
        }
        startActivity(intent)
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
    private fun actualizarSubtotal(productos: List<Producto>) {
        val subtotal = productos.sumOf {
            it.precio * (dbHelper.obtenerProductoPorId(idUsuario, it.idProducto)?.cantidad ?: 0)
        }
        actualizarSubtotalEnVista(subtotal)
    }

}
