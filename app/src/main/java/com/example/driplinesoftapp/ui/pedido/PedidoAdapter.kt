package com.example.driplinesoftapp.ui.pedido

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.driplinesoftapp.data.Pedido
import com.example.driplinesoftapp.databinding.ItemPedidoBinding

class PedidoAdapter(
    private var pedidos: List<Pedido>,
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

            val detalles = pedido.detalles.joinToString("\n") {
                "${it.nombreProducto} x${it.cantidad} - $${it.subtotal}"
            }
            tvDetallesPedido.text = detalles

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

    fun filtrarPedidos(consulta: String) {
        val consultaLimpia = consulta.trim()

        pedidos = if (consultaLimpia.isEmpty()) {
            pedidosOriginales
        } else {
            pedidosOriginales.filter { pedido ->
                pedido.nombreComercial.contains(consultaLimpia, ignoreCase = true) ||
                        pedido.nombreSucursal.contains(consultaLimpia, ignoreCase = true) ||
                        pedido.fechaPedido.contains(consultaLimpia, ignoreCase = true) ||
                        pedido.metodoPago.contains(consultaLimpia, ignoreCase = true) ||
                        pedido.estado.contains(consultaLimpia, ignoreCase = true) ||
                        (pedido.nota?.contains(consultaLimpia, ignoreCase = true) ?: false) ||
                        pedido.detalles.any { detalle ->
                            detalle.nombreProducto.contains(consultaLimpia, ignoreCase = true) ||
                                    detalle.cantidad.toString().contains(consultaLimpia) ||
                                    detalle.subtotal.toString().contains(consultaLimpia)
                        }
            }
        }

        notifyDataSetChanged()
    }
}
