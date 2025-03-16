package com.example.driplinesoftapp.ui.pedido

import android.app.AlertDialog
import android.content.Context
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Pedido
import com.example.driplinesoftapp.databinding.ItemPedidoBinding
import com.example.driplinesoftapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PedidoAdapter(
    private val context: Context,
    private var pedidos: List<Pedido>,
    private val onCardClick: () -> Unit,     // 🔹 Este parámetro va primero
    private val onPedidoCancelado: () -> Unit // 🔹 Este parámetro va después
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    private var pedidosOriginales: List<Pedido> = pedidos.toList()
    private var pedidoIdResaltado: Int? = null

    inner class PedidoViewHolder(val binding: ItemPedidoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        with(holder.binding) {

            tvNumeroPedido.text = Html.fromHtml("<b>Pedido #${pedido.idPedido}</b>")
            tvNumeroPedido.visibility = View.VISIBLE

            tvNombreComercial.text = Html.fromHtml("<b>${pedido.nombreComercial}</b>")
            tvNombreSucursal.text = Html.fromHtml("<b>Sucursal:</b> ${pedido.nombreSucursal}")
            tvFechaPedido.text = Html.fromHtml("<b>Fecha:</b> ${formatearFecha(pedido.fechaPedido)}")
            tvMetodoPago.text = Html.fromHtml("<b>Método de pago:</b> ${pedido.metodoPago}")
            tvEstadoPedido.text = Html.fromHtml("<b>Estado:</b> ${pedido.estado}")
            tvTotalPedido.text = Html.fromHtml("<b>Total:</b> $${pedido.total}")

            Log.d("PedidoAdapter", "➡️ Pedido cargado: ${pedido.idPedido}, Estado: ${pedido.estado}")

            if (!pedido.fechaEntregado.isNullOrEmpty() && pedido.fechaEntregado != "No entregado") {
                tvFechaEntregado.text = Html.fromHtml("<b>Fecha de Entrega:</b> ${formatearFecha(pedido.fechaEntregado)}")
                tvFechaEntregado.visibility = View.VISIBLE
            } else {
                tvFechaEntregado.visibility = View.GONE
            }

            tvTiempoEntrega.text = Html.fromHtml("<b>Tiempo de Entrega:</b> ${pedido.tiempoEntregaEstimado ?: "N/A"}")
            if (pedido.tiempoEntregaEstimado!! > 0) {
                tvTiempoEntrega.text = Html.fromHtml("<b>Tiempo de Entrega:</b> ${pedido.tiempoEntregaEstimado} min")
                tvTiempoEntrega.visibility = View.VISIBLE
            } else {
                tvTiempoEntrega.visibility = View.GONE
            }

            val detalles = pedido.detalles.joinToString("<br>") {
                "<b>${it.nombreProducto}</b> x${it.cantidad} - $${it.subtotal}"
            }
            tvDetallesPedido.text = Html.fromHtml(detalles)

            val sessionManager = SessionManager(context)
            pedidoIdResaltado = sessionManager.obtenerPedidoResaltado()

            if (pedido.idPedido == pedidoIdResaltado) {
                root.setBackgroundResource(R.drawable.bg_resaltado)
                Log.d("PedidoAdapter", "🔹 Pedido resaltado: ${pedido.idPedido}")

                root.postDelayed({
                    root.setBackgroundResource(R.drawable.bg_normal)
                    sessionManager.limpiarPedidoResaltado()
                }, 3000)
            } else {
                root.setBackgroundResource(R.drawable.bg_normal)
            }

            if (pedido.estado.equals("pendiente", ignoreCase = true)) {
                btnCancelarPedido.visibility = View.VISIBLE
                btnCancelarPedido.setOnClickListener {
                    mostrarDialogoConfirmacion(pedido.idPedido, position, holder)
                }
                Log.d("PedidoAdapter", "🟠 Botón de cancelar mostrado para pedido: ${pedido.idPedido}")
            } else {
                btnCancelarPedido.visibility = View.GONE
                Log.d("PedidoAdapter", "🟢 Botón de cancelar oculto para pedido: ${pedido.idPedido}")
            }

            root.setOnClickListener {
                onCardClick()
            }
        }
    }

    override fun getItemCount(): Int = pedidos.size

    private fun mostrarDialogoConfirmacion(idPedido: Int, position: Int, holder: PedidoViewHolder) {
        AlertDialog.Builder(context)
            .setTitle("Confirmar Cancelación")
            .setMessage("¿Estás seguro de que deseas cancelar este pedido?")
            .setPositiveButton("Sí") { _, _ ->
                cancelarPedido(idPedido, position, holder)
            }
            .setNegativeButton("No", null)
            .show()
        Log.d("PedidoAdapter", "📋 Dialogo de confirmación mostrado para el pedido: $idPedido")
    }

    private fun cancelarPedido(idPedido: Int, position: Int, holder: PedidoViewHolder) {
        Log.d("PedidoAdapter", "➡️ Iniciando solicitud para cancelar pedido: $idPedido")

        RetrofitClient.instance.cancelarPedido(idPedido).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful && response.body()?.get("exito") == true) {
                    Snackbar.make(holder.binding.root, "Pedido cancelado correctamente", Snackbar.LENGTH_LONG).show()
                    Log.d("PedidoAdapter", "✅ Pedido cancelado con éxito: $idPedido")

                    pedidos = pedidos.filterNot { it.idPedido == idPedido }
                    pedidosOriginales = pedidosOriginales.map { pedido ->
                        if (pedido.idPedido == idPedido) pedido.copy(estado = "cancelado") else pedido
                    }

                    notifyDataSetChanged()

                    // 🔹 Añade esta línea para refrescar la vista del fragmento
                    onPedidoCancelado.invoke()

                } else {
                    val mensaje = response.body()?.get("mensaje") ?: "Error desconocido"
                    Snackbar.make(holder.binding.root, "❌ $mensaje", Snackbar.LENGTH_LONG).show()
                    Log.e("PedidoAdapter", "❌ Error al cancelar pedido: $mensaje")
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Snackbar.make(holder.binding.root, "Error de conexión: ${t.message}", Snackbar.LENGTH_LONG).show()
                Log.e("PedidoAdapter", "❗ Error de conexión al intentar cancelar el pedido: ${t.message}")
            }
        })
    }


    fun actualizarLista(nuevaLista: List<Pedido>) {
        pedidosOriginales = nuevaLista.toList()
        pedidos = nuevaLista.sortedByDescending { it.idPedido }
        notifyDataSetChanged()
        Log.d("PedidoAdapter", "🔄 Lista de pedidos actualizada correctamente")
    }

    private fun formatearFecha(fecha: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("d 'de' MMMM, yyyy 'a las' h:mm a", Locale("es", "ES"))
            formatoSalida.format(formatoEntrada.parse(fecha) ?: Date())
        } catch (e: Exception) {
            Log.e("PedidoAdapter", "❌ Error formateando fecha: ${e.message}")
            "Fecha no disponible"
        }
    }
}