package com.example.driplinesoftapp.ui.restaurante

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Producto
import com.example.driplinesoftapp.data.ProductoCarrito
import com.example.driplinesoftapp.databinding.ItemCarritoBinding
import com.example.driplinesoftapp.models.CarritoDatabaseHelper

class CarritoAdapter(
    private val productos: MutableList<Producto>, // Cambio a MutableList para manejar cambios dinámicos
    private val dbHelper: CarritoDatabaseHelper,
    private val onUpdate: () -> Unit,
    private val onSubtotalChange: (Double) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    inner class CarritoViewHolder(val binding: ItemCarritoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val binding = ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val producto = productos[position]

        with(holder.binding) {
            tvNombreProducto.text = producto.nombreProducto
            tvPrecio.text = "Precio: $${producto.precio}"

            // Obtener cantidad actualizada directamente de SQLite
            val cantidadEnCarrito = dbHelper.obtenerProductoPorId(1, producto.idProducto)?.cantidad ?: 0
            tvCantidad.text = cantidadEnCarrito.toString()

            // Cargar imagen desde la URL concatenada
            if (!producto.imagenes.isNullOrEmpty()) {
                val imagenUrl = "${RetrofitClient.BASE_URL_IMAGENES}${producto.imagenes[0]}"
                Glide.with(ivImagenProducto.context)
                    .load(imagenUrl)
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_error)
                    .into(ivImagenProducto)
            } else {
                ivImagenProducto.setImageResource(R.drawable.ic_logo)
            }

            // ➤ SUMAR PRODUCTO
            btnSumar.setOnClickListener {
                // Obtener cantidad actualizada directamente desde SQLite
                val cantidadActual = dbHelper.obtenerProductoPorId(1, producto.idProducto)?.cantidad ?: 0
                val nuevaCantidad = cantidadActual + 1

                dbHelper.actualizarCantidad(1, ProductoCarrito(producto.idProducto, nuevaCantidad))
                tvCantidad.text = nuevaCantidad.toString()

                productos[position] = producto.copy(cantidad = nuevaCantidad)
                onSubtotalChange(calcularNuevoSubtotal())
                onUpdate()
            }

            // ➤ RESTAR PRODUCTO
            btnRestar.setOnClickListener {
                // Obtener cantidad actualizada directamente desde SQLite
                val cantidadActual = dbHelper.obtenerProductoPorId(1, producto.idProducto)?.cantidad ?: 0

                if (cantidadActual > 1) {
                    val nuevaCantidad = cantidadActual - 1
                    dbHelper.actualizarCantidad(1, ProductoCarrito(producto.idProducto, nuevaCantidad))
                    tvCantidad.text = nuevaCantidad.toString()

                    productos[position] = producto.copy(cantidad = nuevaCantidad)
                    onSubtotalChange(calcularNuevoSubtotal())
                    onUpdate()
                } else {
                    dbHelper.eliminarProducto(1, producto.idProducto)
                    Toast.makeText(root.context, "Producto eliminado", Toast.LENGTH_SHORT).show()

                    productos.removeAt(position)
                    notifyItemRemoved(position)
                    onSubtotalChange(calcularNuevoSubtotal())
                    onUpdate()
                }
            }

            // ➤ ELIMINAR PRODUCTO
            btnEliminar.setOnClickListener {
                dbHelper.eliminarProducto(1, producto.idProducto)
                Toast.makeText(root.context, "Producto eliminado", Toast.LENGTH_SHORT).show()

                productos.removeAt(position)
                notifyItemRemoved(position)
                onSubtotalChange(calcularNuevoSubtotal())
                onUpdate()
            }
        }
    }

    override fun getItemCount(): Int = productos.size

    // ➤ Método para calcular el subtotal dinámicamente
    private fun calcularNuevoSubtotal(): Double {
        return productos.sumOf { it.precio * (dbHelper.obtenerProductoPorId(1, it.idProducto)?.cantidad ?: 0) }
    }
}
