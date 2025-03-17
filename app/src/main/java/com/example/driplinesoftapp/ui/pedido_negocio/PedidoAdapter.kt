package com.example.driplinesoftapp.ui.pedido_negocio

import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.data_negocio.PedidoNegocio
import com.example.driplinesoftapp.databinding.ItemPedidoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PedidoNegocioAdapter(
    private var pedidos: List<PedidoNegocio>,
    private var pedidoIdResaltado: Int? = null,
    private val onCardClick: (PedidoNegocio) -> Unit
) : RecyclerView.Adapter<PedidoNegocioAdapter.PedidoViewHolder>() {

    private var pedidosOriginales: List<PedidoNegocio> = pedidos.toList()

    inner class PedidoViewHolder(val binding: ItemPedidoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        with(holder.binding) {

            // Mostrar el número de pedido en grande y en negritas
            tvNumeroPedido.text = Html.fromHtml("<b>Pedido #${pedido.idPedido}</b>")
            tvNumeroPedido.visibility = View.VISIBLE

            btnCancelarPedido.visibility = View.GONE;

            tvNombreComercial.text = Html.fromHtml("<b>${pedido.nombreComercial}</b>")
            tvNombreSucursal.text = Html.fromHtml("<b>Sucursal:</b> ${pedido.nombreSucursal}")
            tvFechaPedido.text = Html.fromHtml("<b>Fecha:</b> ${formatearFecha(pedido.fechaPedido)}")
            tvMetodoPago.text = Html.fromHtml("<b>Método de pago:</b> ${pedido.metodoPago}")
            tvEstadoPedido.text = Html.fromHtml("<b>Estado:</b> ${pedido.estado}")
            tvTotalPedido.text = Html.fromHtml("<b>Total:</b> $${pedido.total}")

            // Tiempo de entrega estimado (resaltado)
            tvTiempoEntrega.text = Html.fromHtml("<b>Tiempo de Entrega:</b> ${pedido.tiempoEntregaEstimado ?: "N/A"}")
            if (pedido.tiempoEntregaEstimado!! > 0) {
                tvTiempoEntrega.text = Html.fromHtml("<b>Tiempo de Entrega:</b> ${pedido.tiempoEntregaEstimado} min")
                tvTiempoEntrega.visibility = View.VISIBLE
            } else {
                tvTiempoEntrega.visibility = View.GONE
            }

            if (!pedido.fechaEntregado.isNullOrEmpty() && pedido.fechaEntregado != "No entregado") {
                tvFechaEntregado.text = Html.fromHtml("<b>Fecha de Entrega:</b> ${formatearFecha(pedido.fechaEntregado)}")
                tvFechaEntregado.visibility = View.VISIBLE
            } else {
                tvFechaEntregado.visibility = View.GONE
            }

            val detalles = pedido.detalles.joinToString("<br>") {
                "<b>${it.nombreProducto}</b> x${it.cantidad} - $${it.subtotal}"
            }
            tvDetallesPedido.text = Html.fromHtml(detalles)

            // 🔹 Resaltar el pedido si es el que se acaba de crear
            if (pedido.idPedido == pedidoIdResaltado) {
                root.setBackgroundResource(R.drawable.bg_resaltado)
                root.post {
                    root.requestFocus() // Hacer foco en la tarjeta
                }
            } else {
                root.setBackgroundResource(R.drawable.bg_normal)
            }

            // Activar SearchView al presionar el CardView
            root.setOnClickListener {
                onCardClick(pedido)
            }
        }
    }

    override fun getItemCount(): Int = pedidos.size

    fun actualizarLista(nuevaLista: List<PedidoNegocio>) {
        pedidosOriginales = nuevaLista.toList()
        pedidos = nuevaLista
        notifyDataSetChanged()
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
