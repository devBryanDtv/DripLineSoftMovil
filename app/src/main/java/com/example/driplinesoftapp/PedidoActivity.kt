package com.example.driplinesoftapp

import android.animation.Animator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.*
import com.example.driplinesoftapp.databinding.ActivityPedidoBinding
import com.example.driplinesoftapp.models.CarritoDatabaseHelper
import com.example.driplinesoftapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPedidoBinding
    private lateinit var btnRealizarPedido: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var edtNota: EditText
    private lateinit var edtDescuento: EditText
    private lateinit var spinnerMetodoPago: EditText

    private var productos: List<ProductoCarrito> = mutableListOf()
    private var subtotal: Double = 0.0
    private var idUsuarioCliente: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)
        val usuario = sessionManager.getUser()

        if (usuario != null) {
            idUsuarioCliente = usuario.idUsuario
        } else {
            mostrarSnackbarError("No estás logueado. Redirigiendo al login.")
            finish()
        }

        val productosJson = intent.getStringExtra("productos")
        subtotal = intent.getDoubleExtra("subtotal", 0.0)

        val gson = Gson()
        productos = gson.fromJson(productosJson, Array<ProductoCarrito>::class.java).toList()

        binding.tvSubtotal.text = "Subtotal: $${"%.2f".format(subtotal)}"

        edtNota = binding.edtNota
        edtDescuento = binding.edtDescuento
        btnRealizarPedido = binding.btnRealizarPedido
        progressBar = binding.progressBar
        spinnerMetodoPago = binding.spinnerMetodoPago

        spinnerMetodoPago.isFocusable = false
        spinnerMetodoPago.isClickable = true

        val metodoPagoMap = mapOf(
            "Efectivo" to "efectivo",
            "Tarjeta de credito/debito" to "tarjeta",
            "Transferencia bancaria" to "transferencia"
        )

        val metodoPagoNombres = metodoPagoMap.keys.toList()
        spinnerMetodoPago.setText("Selecciona el método de pago")

        spinnerMetodoPago.setOnClickListener {
            mostrarDialogMetodosPago(metodoPagoNombres)
        }

        btnRealizarPedido.setOnClickListener {
            realizarPedido(metodoPagoMap)
        }
    }

    private fun mostrarDialogMetodosPago(methods: List<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona el método de pago")
        builder.setItems(methods.toTypedArray()) { _, which ->
            val selectedMethod = methods[which]
            spinnerMetodoPago.setText(selectedMethod)
        }
        builder.show()
    }

    private fun realizarPedido(metodoPagoMap: Map<String, String>) {
        val metodoPagoSeleccionado = spinnerMetodoPago.text.toString()
        val metodoPago = metodoPagoMap[metodoPagoSeleccionado]

        if (metodoPago == null) {
            mostrarSnackbarError("Método de pago no válido.")
            return
        }

        val nota = edtNota.text.toString()
        val descuento = edtDescuento.text.toString().toDoubleOrNull() ?: 0.0

        val pedidoRequest = PedidoRequest(
            idUsuarioCliente = idUsuarioCliente,
            metodoPago = metodoPago,
            productos = productos,
            nota = nota,
            descuento = descuento
        )

        // 🔹 Mostrar ProgressBar y desactivar el botón
        mostrarCargando(true)

        RetrofitClient.instance.crearPedido(pedidoRequest)
            .enqueue(object : Callback<PedidoResponse> {
                override fun onResponse(call: Call<PedidoResponse>, response: Response<PedidoResponse>) {
                    mostrarCargando(false)

                    if (response.isSuccessful && response.body()?.success == true) {
                        val pedido = response.body()?.data
                        mostrarDialogoExito(pedido)
                    } else {
                        val errorMessage = response.body()?.message ?: "Error desconocido"
                        mostrarSnackbarError("Error al realizar el pedido: $errorMessage")
                    }
                }

                override fun onFailure(call: Call<PedidoResponse>, t: Throwable) {
                    mostrarCargando(false)
                    mostrarSnackbarError("Error de conexión: ${t.message}")
                }
            })
    }

    private fun mostrarDialogoExito(pedido: PedidoData?) {
        if (pedido == null) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_pedido_exito, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val animationView: LottieAnimationView = dialogView.findViewById(R.id.lottieSuccess)
        val imgPlaceholder: ImageView = dialogView.findViewById(R.id.imgCheckPlaceholder)

        imgPlaceholder.visibility = View.INVISIBLE

        animationView.setAnimation(R.raw.success)
        animationView.repeatCount = 0
        animationView.playAnimation()

        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                animationView.visibility = View.GONE
                imgPlaceholder.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        val tvPedidoId: TextView = dialogView.findViewById(R.id.tvPedidoId)
        val tvTotal: TextView = dialogView.findViewById(R.id.tvTotal)
        val tvTiempoEntrega: TextView = dialogView.findViewById(R.id.tvTiempoEntrega)
        val btnAceptar: Button = dialogView.findViewById(R.id.btnAceptar)

        tvPedidoId.text = "Pedido #${pedido.id_pedido}"
        tvTotal.text = "Total: $${pedido.total}"
        tvTiempoEntrega.text = "Tiempo estimado: ${pedido.tiempo_entrega_estimado} min"

        btnAceptar.setOnClickListener {
            dialog.dismiss()

            // Guardar el ID del pedido en el SessionManager
            val sessionManager = SessionManager(this)
            sessionManager.guardarPedidoResaltado(pedido.id_pedido)

            // 🔹 Vaciar el carrito después de que se realice el pedido
            val dbHelper = CarritoDatabaseHelper(this)
            val usuario = sessionManager.getUser()
            val idUsuario = usuario?.idUsuario
            if (idUsuario != null) {
                dbHelper.vaciarCarrito(idUsuario)
            }

            // Enviar mensaje de éxito mediante Intent
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("mensaje_exito", "✅ Pedido realizado con éxito")
                putExtra("estado", "pendiente")
            }
            startActivity(intent)
            finish()  // Cierra el PedidoActivity para evitar regresar
        }




        dialog.show()
    }

    // 🔹 Mostrar ProgressBar y Deshabilitar el botón
    private fun mostrarCargando(mostrar: Boolean) {
        if (mostrar) {
            btnRealizarPedido.text = ""
            progressBar.visibility = View.VISIBLE
            btnRealizarPedido.isEnabled = false
        } else {
            btnRealizarPedido.text = "Realizar Pedido"
            progressBar.visibility = View.GONE
            btnRealizarPedido.isEnabled = true
        }
    }

    // ✅ Mensaje de Error en Rojo
    private fun mostrarSnackbarError(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.RED)
            .setTextColor(Color.WHITE)
            .setAction("Cerrar") { }
            .show()
    }

    // ✅ Mensaje de Éxito en Verde
    private fun mostrarSnackbarExito(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.GREEN)
            .setTextColor(Color.BLACK)
            .setAction("OK") { }
            .show()
    }
}
