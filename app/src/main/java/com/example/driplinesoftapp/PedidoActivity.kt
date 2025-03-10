package com.example.driplinesoftapp

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.PedidoRequest
import com.example.driplinesoftapp.data.PedidoResponse
import com.example.driplinesoftapp.data.ProductoCarrito
import com.example.driplinesoftapp.databinding.ActivityPedidoBinding
import com.example.driplinesoftapp.utils.SessionManager
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPedidoBinding
    private lateinit var btnRealizarPedido: Button
    private lateinit var edtNota: EditText
    private lateinit var edtDescuento: EditText
    private lateinit var spinnerMetodoPago: EditText  // Ahora es un EditText

    private var productos: List<ProductoCarrito> = mutableListOf()
    private var subtotal: Double = 0.0
    private var idUsuarioCliente: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el idUsuarioCliente desde la sesión
        val sessionManager = SessionManager(this)
        val usuario = sessionManager.getUser()

        if (usuario != null) {
            idUsuarioCliente = usuario.idUsuario
            Log.d("PedidoActivity", "Usuario logueado: $idUsuarioCliente")
        } else {
            Log.e("PedidoActivity", "No estás logueado, redirigiendo al login.")
            Toast.makeText(this, "No estás logueado", Toast.LENGTH_SHORT).show()
            finish()  // Cierra la actividad si el usuario no está logueado
        }

        // Obtener datos del Intent
        val productosJson = intent.getStringExtra("productos")
        subtotal = intent.getDoubleExtra("subtotal", 0.0)

        // Convertir productos JSON a lista
        val gson = Gson()
        productos = gson.fromJson(productosJson, Array<ProductoCarrito>::class.java).toList()

        // Mostrar el subtotal en la interfaz
        binding.tvSubtotal.text = "Subtotal: $${"%.2f".format(subtotal)}"

        // Inicializar vistas
        edtNota = binding.edtNota
        edtDescuento = binding.edtDescuento
        btnRealizarPedido = binding.btnRealizarPedido
        spinnerMetodoPago = binding.spinnerMetodoPago

        // Deshabilitar el EditText, ya que usaremos un Dialog
        spinnerMetodoPago.isFocusable = false
        spinnerMetodoPago.isClickable = true

        // Llenar el AutoCompleteTextView con los métodos de pago
        val methods = listOf("Efectivo", "Tarjeta de credito/debito", "Transferencia bancaria")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, methods)
        spinnerMetodoPago.setText("Selecciona el método de pago")

        // Al presionar el EditText se abrirá el Dialog
        spinnerMetodoPago.setOnClickListener {
            mostrarDialogMetodosPago(methods)
        }

        btnRealizarPedido.setOnClickListener {
            realizarPedido()
        }
    }

    private fun mostrarDialogMetodosPago(methods: List<String>) {
        // Crear el Dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona el método de pago")
        builder.setItems(methods.toTypedArray()) { _, which ->
            val selectedMethod = methods[which]
            spinnerMetodoPago.setText(selectedMethod)  // Establecer el valor en el EditText
        }
        builder.show()  // Mostrar el Dialog
    }

    private fun realizarPedido() {
        val metodoPago = spinnerMetodoPago.text.toString()

        // Solo continuar si el método de pago es Efectivo
        if (metodoPago != "Efectivo") {
            Toast.makeText(this, "Solo se puede realizar el pedido con Efectivo", Toast.LENGTH_SHORT).show()
            return  // No realizar el pedido si el método de pago no es Efectivo
        }

        val nota = edtNota.text.toString()
        val descuento = edtDescuento.text.toString().toDoubleOrNull() ?: 0.0

        // Log de los datos antes de enviar la solicitud
        Log.d("PedidoActivity", "Metodo de pago seleccionado: $metodoPago")
        Log.d("PedidoActivity", "Nota: $nota")
        Log.d("PedidoActivity", "Descuento: $descuento")

        // Crear el objeto de envío
        val pedidoRequest = PedidoRequest(
            idUsuarioCliente = idUsuarioCliente,
            metodoPago = metodoPago,
            productos = productos,
            nota = nota,
            descuento = descuento
        )

        // Log de la solicitud
        Log.d("PedidoActivity", "Solicitud de pedido: $pedidoRequest")

        // Realizar la petición con Retrofit
        RetrofitClient.instance.crearPedido(pedidoRequest)
            .enqueue(object : Callback<PedidoResponse> {
                override fun onResponse(call: Call<PedidoResponse>, response: Response<PedidoResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Log.d("PedidoActivity", "Pedido creado con éxito: ${response.body()}")
                        Toast.makeText(this@PedidoActivity, "Pedido realizado con éxito", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMessage = response.body()?.message ?: "Error desconocido"
                        Log.e("PedidoActivity", "Error al realizar el pedido: $errorMessage")
                        Toast.makeText(this@PedidoActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PedidoResponse>, t: Throwable) {
                    Log.e("PedidoActivity", "Error en la conexión: ${t.message}")
                    Toast.makeText(this@PedidoActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
