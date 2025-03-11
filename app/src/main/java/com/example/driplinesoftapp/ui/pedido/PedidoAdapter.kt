package com.example.driplinesoftapp.ui.pedido

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.driplinesoftapp.data.Pedido
import com.example.driplinesoftapp.databinding.ItemPedidoBinding

class PedidoAdapter(private var pedidos: List<Pedido>) :
    RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

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

            // Mostrar detalles de los productos en el pedido
            val detalles = pedido.detalles.joinToString("\n") {
                "${it.nombreProducto} x${it.cantidad} - $${it.subtotal}"
            }
            tvDetallesPedido.text = detalles
        }
    }

    override fun getItemCount(): Int = pedidos.size

    fun actualizarLista(nuevaLista: List<Pedido>) {
        pedidos = nuevaLista
        notifyDataSetChanged()
    }
}
