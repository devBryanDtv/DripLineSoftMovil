package com.example.driplinesoftapp.ui.restaurante

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.models.CarritoDatabaseHelper
import com.example.driplinesoftapp.data.Producto
import com.example.driplinesoftapp.data.ProductoCarrito
import com.example.driplinesoftapp.databinding.ItemProductoBinding

class ProductoAdapter(
    private val productos: MutableList<Producto>, // Cambiado a MutableList para manejar actualizaciones
    private val dbHelper: CarritoDatabaseHelper,
    private val onAddToCart: (Producto) -> Unit,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        val productoCarrito = dbHelper.obtenerProductoPorId(1, producto.idProducto)

        with(holder.binding) {
            tvNombreProducto.text = producto.nombreProducto
            tvDescripcion.text = producto.descripcion ?: "Sin descripción"
            tvPrecio.text = "Precio: $${producto.precio}"

            if (!producto.imagenes.isNullOrEmpty()) {
                val imagenUrl = RetrofitClient.BASE_URL_IMAGENES + producto.imagenes[0].rutaImagen
                Glide.with(ivImagenProducto.context)
                    .load(imagenUrl)
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_error)
                    .into(ivImagenProducto)
            } else {
                ivImagenProducto.setImageResource(R.drawable.ic_logo)
            }

            // Mostrar controles según la cantidad del producto
            if (productoCarrito == null) {
                btnAgregarCarrito.visibility = View.VISIBLE
                layoutControlesCantidad.visibility = View.GONE
            } else {
                btnAgregarCarrito.visibility = View.GONE
                layoutControlesCantidad.visibility = View.VISIBLE
                tvCantidad.text = productoCarrito.cantidad.toString()
            }

            // ➤ AGREGAR PRODUCTO
            btnAgregarCarrito.setOnClickListener {
                onAddToCart(producto)
                productos[position] = producto.copy(cantidad = 1)
                notifyItemChanged(position) // Actualizar inmediatamente la vista
                Toast.makeText(root.context, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
                onUpdate()
            }

            // ➤ SUMAR PRODUCTO
            btnSumar.setOnClickListener {
                val cantidadActual = dbHelper.obtenerProductoPorId(1, producto.idProducto)?.cantidad ?: 0
                val nuevaCantidad = cantidadActual + 1
                val productoCarritoActualizado = ProductoCarrito(idProducto = producto.idProducto, cantidad = nuevaCantidad)

                dbHelper.actualizarCantidad(1, productoCarritoActualizado)
                tvCantidad.text = nuevaCantidad.toString()
                onUpdate()
            }

            // ➤ RESTAR PRODUCTO
            btnRestar.setOnClickListener {
                val cantidadActual = dbHelper.obtenerProductoPorId(1, producto.idProducto)?.cantidad ?: 0
                if (cantidadActual > 1) {
                    val nuevaCantidad = cantidadActual - 1
                    val productoCarritoActualizado = ProductoCarrito(idProducto = producto.idProducto, cantidad = nuevaCantidad)

                    dbHelper.actualizarCantidad(1, productoCarritoActualizado)
                    tvCantidad.text = nuevaCantidad.toString()
                    onUpdate()
                } else if (cantidadActual == 1) {
                    dbHelper.eliminarProducto(1, producto.idProducto)
                    Toast.makeText(root.context, "Producto eliminado", Toast.LENGTH_SHORT).show()

                    layoutControlesCantidad.visibility = View.GONE
                    btnAgregarCarrito.visibility = View.VISIBLE
                    onUpdate()
                }
            }
        }
    }

    override fun getItemCount(): Int = productos.size

    // ➤ Método para actualizar un producto específico
    fun actualizarProducto(producto: Producto) {
        val index = productos.indexOfFirst { it.idProducto == producto.idProducto }
        if (index != -1) {
            productos[index] = producto
            notifyItemChanged(index)
        }
    }
}
