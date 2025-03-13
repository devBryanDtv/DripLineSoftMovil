package com.example.driplinesoftapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieAnimationView
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.PedidoData
import com.example.driplinesoftapp.data.PedidoRequest
import com.example.driplinesoftapp.data.PedidoResponse
import com.example.driplinesoftapp.data.ProductoCarrito
import com.example.driplinesoftapp.databinding.ActivityPedidoBinding
import com.example.driplinesoftapp.utils.SessionManager
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.animation.Animator
import android.view.View
import android.widget.ImageView
import com.airbnb.lottie.LottieDrawable


class PedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPedidoBinding
    private lateinit var btnRealizarPedido: Button
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

        // Obtener el idUsuarioCliente desde la sesión
        val sessionManager = SessionManager(this)
        val usuario = sessionManager.getUser()

        if (usuario != null) {
            idUsuarioCliente = usuario.idUsuario
            Log.d("PedidoActivity", "Usuario logueado: $idUsuarioCliente")
        } else {
            Log.e("PedidoActivity", "No estás logueado, redirigiendo al login.")
            Toast.makeText(this, "No estás logueado", Toast.LENGTH_SHORT).show()
            finish()
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

        // Definir los métodos de pago con los valores correctos
        val metodoPagoMap = mapOf(
            "Efectivo" to "efectivo",
            "Tarjeta de credito/debito" to "tarjeta",
            "Transferencia bancaria" to "transferencia"
        )

        val metodoPagoNombres = metodoPagoMap.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, metodoPagoNombres)
        spinnerMetodoPago.setText("Selecciona el método de pago")

        // Al presionar el EditText se abrirá el Dialog
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
            spinnerMetodoPago.setText(selectedMethod)  // Establecer el valor en el EditText
        }
        builder.show()
    }

    private fun realizarPedido(metodoPagoMap: Map<String, String>) {
        val metodoPagoSeleccionado = spinnerMetodoPago.text.toString()

        // Verificar que el método de pago seleccionado sea válido
        val metodoPago = metodoPagoMap[metodoPagoSeleccionado]
        if (metodoPago == null) {
            Toast.makeText(this, "Método de pago no válido", Toast.LENGTH_SHORT).show()
            return
        }

        val nota = edtNota.text.toString()
        val descuento = edtDescuento.text.toString().toDoubleOrNull() ?: 0.0

        // Log de los datos antes de enviar la solicitud
        Log.d("PedidoActivity", "Método de pago seleccionado: $metodoPago")
        Log.d("PedidoActivity", "Nota: $nota")
        Log.d("PedidoActivity", "Descuento: $descuento")

        // Crear el objeto de envío
        val pedidoRequest = PedidoRequest(
            idUsuarioCliente = idUsuarioCliente,
            metodoPago = metodoPago, // Se envía el valor correcto
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
                        val pedido = response.body()?.data

                        // ✅ Mostrar animación y AlertDialog con los detalles del pedido
                        mostrarDialogoExito(pedido)
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
    private fun mostrarDialogoExito(pedido: PedidoData?) {
        if (pedido == null) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_pedido_exito, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // 🔹 Obtener las vistas
        val animationView: LottieAnimationView = dialogView.findViewById(R.id.lottieSuccess)
        val imgPlaceholder: ImageView = dialogView.findViewById(R.id.imgCheckPlaceholder) // 🔹 Nueva imagen fija

        // 🔹 Configurar el Placeholder de la palomita (inicialmente oculto)
        imgPlaceholder.visibility = View.INVISIBLE

        // 🔹 Configurar la animación
        animationView.setAnimation(R.raw.success)
        animationView.repeatCount = 0
        animationView.playAnimation()

        // 🔹 Listener para pausar antes del final y mostrar la imagen estática
        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                animationView.visibility = View.GONE  // Ocultar animación
                imgPlaceholder.visibility = View.GONE // Mostrar la imagen de la palomita
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
            // ✅ Navegar a PedidoFragment y resaltar el pedido pendiente
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("resaltarPedidoId", pedido.id_pedido)
            intent.putExtra("estado", "pendiente")
            startActivity(intent)
        }

        dialog.show()
    }



}
