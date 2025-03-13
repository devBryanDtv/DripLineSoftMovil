package com.example.driplinesoftapp.ui.pedido

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.data.Pedido
import com.example.driplinesoftapp.databinding.ItemPedidoBinding

class PedidoAdapter(
    private var pedidos: List<Pedido>,
    private var pedidoIdResaltado: Int? = null,
    private val onCardClick: () -> Unit
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    private var pedidosOriginales: List<Pedido> = pedidos.toList()

    inner class PedidoViewHolder(val binding: ItemPedidoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        with(holder.binding) {
            tvNombreComercial.text = pedido.nombreComercial
            tvNombreSucursal.text = pedido.nombreSucursal
            tvFechaPedido.text = pedido.fechaPedido
            tvMetodoPago.text = "Método de pago: ${pedido.metodoPago}"
            tvEstadoPedido.text = "Estado: ${pedido.estado}"
            tvTotalPedido.text = "Total: $${pedido.total}"

            // Tiempo de entrega estimado (resaltado)
            tvTiempoEntrega.text = "Tiempo de Entrega: ${pedido.tiempoEntregaEstimado ?: "N/A"}"
            // Mostrar el tiempo de entrega solo si es mayor a 0
            if (pedido.tiempoEntregaEstimado!! > 0) {
                tvTiempoEntrega.text = "Tiempo de Entrega: ${pedido.tiempoEntregaEstimado} min"
                tvTiempoEntrega.visibility = View.VISIBLE
            } else {
                tvTiempoEntrega.visibility = View.GONE
            }

            val detalles = pedido.detalles.joinToString("\n") {
                "${it.nombreProducto} x${it.cantidad} - $${it.subtotal}"
            }
            tvDetallesPedido.text = detalles

            // 🔹 Resaltar el pedido si es el que se acaba de crear
            if (pedido.idPedido == pedidoIdResaltado) {
                root.setBackgroundResource(R.drawable.bg_resaltado) // Fondo resaltado
                root.post {
                    root.requestFocus() // Hacer foco en la tarjeta
                }
            } else {
                root.setBackgroundResource(R.drawable.bg_normal) // Fondo normal
            }


            // Activar SearchView al presionar el CardView
            root.setOnClickListener {
                onCardClick()
            }
        }
    }

    override fun getItemCount(): Int = pedidos.size

    fun actualizarLista(nuevaLista: List<Pedido>) {
        pedidosOriginales = nuevaLista.toList()
        pedidos = nuevaLista
        notifyDataSetChanged()
    }
    // Función para resaltar el pedido. Esto cambia la variable 'pedidoIdResaltado' y actualiza la vista.
    fun resaltarPedido(idPedido: Int) {
        pedidoIdResaltado = idPedido
        notifyDataSetChanged()  // Esto fuerza la actualización del RecyclerView para reflejar el cambio
    }

    // Obtener la posición de un pedido específico, lo utilizamos para hacer scroll al pedido resaltado.
    fun obtenerPosicionPedido(idPedido: Int): Int {
        return pedidos.indexOfFirst { it.idPedido == idPedido }
    }

    // 🛑 Este método asegura que el RecyclerView haga scroll al pedido resaltado.
    fun scrollToHighlightedPosition(recyclerView: RecyclerView) {
        val position = obtenerPosicionPedido(pedidoIdResaltado ?: return)
        if (position != -1) {
            recyclerView.post {
                recyclerView.smoothScrollToPosition(position)
            }
        }
    }


}
