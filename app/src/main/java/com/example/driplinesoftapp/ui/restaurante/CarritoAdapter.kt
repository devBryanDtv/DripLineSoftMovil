package com.example.driplinesoftapp.ui.restaurante

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.driplinesoftapp.data.Producto
import com.example.driplinesoftapp.databinding.ItemNegocioBinding
import com.example.driplinesoftapp.databinding.ItemCarritoBinding
import com.example.driplinesoftapp.databinding.ItemUsuarioBinding
import com.example.driplinesoftapp.models.CarritoDatabaseHelper
import com.example.driplinesoftapp.utils.SessionManager

class CarritoAdapter(
    private val productos: MutableList<Producto>,
    private val dbHelper: CarritoDatabaseHelper,
    private val sessionManager: SessionManager,
    private val nombreMenu: String?,
    private val nombreSucursal: String?,
    private val nombreComercial: String?,
    private val logoCliente: String?,
    private val onSubtotalChange: (Double) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TIPO_NEGOCIO = 1
    private val TIPO_USUARIO = 2
    private val TIPO_PEDIDO = 3

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TIPO_NEGOCIO
            1 -> TIPO_USUARIO
            else -> TIPO_PEDIDO
        }
    }

    inner class NegocioViewHolder(val binding: ItemNegocioBinding) : RecyclerView.ViewHolder(binding.root)
    inner class UsuarioViewHolder(val binding: ItemUsuarioBinding) : RecyclerView.ViewHolder(binding.root)
    inner class PedidoViewHolder(val binding: ItemCarritoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TIPO_NEGOCIO -> NegocioViewHolder(ItemNegocioBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            TIPO_USUARIO -> UsuarioViewHolder(ItemUsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> PedidoViewHolder(ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NegocioViewHolder -> {
                with(holder.binding) {
                    tvNombreComercial.text = Html.fromHtml("<b>Negocio:</b> ${nombreComercial ?: "Sin nombre comercial"}")
                    tvNombreSucursal.text = Html.fromHtml("<b>Sucursal:</b> ${nombreSucursal ?: "No especificada"}")
                    tvNombreMenu.text = Html.fromHtml("<b>Menú:</b> ${nombreMenu ?: "No especificado"}")
                }
            }

            is UsuarioViewHolder -> {
                val usuario = sessionManager.getUser()
                with(holder.binding) {
                    tvNombreUsuario.text = Html.fromHtml("<b>Cliente:</b> ${usuario?.nombre ?: "No especificado"}")
                    tvCorreoUsuario.text = Html.fromHtml("<b>Correo:</b> ${usuario?.email ?: "No especificado"}")
                }
            }

            is PedidoViewHolder -> {
                with(holder.binding) {
                    layoutProductos.removeAllViews()
                    productos.forEach { producto ->
                        val cantidadEnCarrito = dbHelper.obtenerProductoPorId(
                            sessionManager.getUser()?.idUsuario ?: -1,
                            producto.idProducto
                        )?.cantidad ?: 0

                        val textoProducto = Html.fromHtml(
                            "<b>• ${cantidadEnCarrito}x ${producto.nombreProducto}</b> - $${producto.precio * cantidadEnCarrito}"
                        )

                        val textViewProducto = TextView(layoutProductos.context).apply {
                            text = textoProducto
                            textSize = 14f
                            setPadding(0, 4, 0, 4)
                        }

                        layoutProductos.addView(textViewProducto)
                    }

                    // Subtotal
                    val subtotal = productos.sumOf {
                        it.precio * (dbHelper.obtenerProductoPorId(
                            sessionManager.getUser()?.idUsuario ?: -1,
                            it.idProducto
                        )?.cantidad ?: 0)
                    }
                    tvSubtotal.text = Html.fromHtml("<b>Subtotal:</b> $${"%.2f".format(subtotal)}")

                    // Notificar el cambio del subtotal
                    onSubtotalChange(subtotal)
                }
            }
        }
    }

    override fun getItemCount(): Int = 3 // Tres elementos: uno para negocio, uno para usuario y otro para pedido
}
