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
import com.example.driplinesoftapp.utils.SessionManager

class ProductoAdapter(
    private var productos: MutableList<Producto>,
    private val dbHelper: CarritoDatabaseHelper,
    private val sessionManager: SessionManager,  // 🔹 Ahora pasamos SessionManager al constructor
    private val onAddToCart: (Producto) -> Unit,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    private val productosOriginales: MutableList<Producto> = productos.toMutableList()

    inner class ProductoViewHolder(val binding: ItemProductoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        // 🔹 Obtener el ID del usuario autenticado
        val idUsuario = sessionManager.getUser()?.idUsuario ?: -1
        val productoCarrito = dbHelper.obtenerProductoPorId(idUsuario, producto.idProducto)

        with(holder.binding) {
            tvNombreProducto.text = producto.nombreProducto
            tvDescripcion.text = producto.descripcion ?: "Sin descripción"
            tvPrecio.text = "Precio: $${producto.precio}"

            // 🔹 Cargar imagen correctamente
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

            // 🔹 Mostrar controles de cantidad si el producto está en el carrito
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
                notifyItemChanged(position) // 🔹 Actualizar la vista inmediatamente
                Toast.makeText(root.context, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
                onUpdate()
            }

            // ➤ SUMAR PRODUCTO
            btnSumar.setOnClickListener {
                val cantidadActual = dbHelper.obtenerProductoPorId(idUsuario, producto.idProducto)?.cantidad ?: 0
                val nuevaCantidad = cantidadActual + 1
                val productoCarritoActualizado = ProductoCarrito(idProducto = producto.idProducto, cantidad = nuevaCantidad)

                dbHelper.actualizarCantidad(idUsuario, productoCarritoActualizado)
                tvCantidad.text = nuevaCantidad.toString()
                onUpdate()
            }

            // ➤ RESTAR PRODUCTO
            btnRestar.setOnClickListener {
                val cantidadActual = dbHelper.obtenerProductoPorId(idUsuario, producto.idProducto)?.cantidad ?: 0
                if (cantidadActual > 1) {
                    val nuevaCantidad = cantidadActual - 1
                    val productoCarritoActualizado = ProductoCarrito(idProducto = producto.idProducto, cantidad = nuevaCantidad)

                    dbHelper.actualizarCantidad(idUsuario, productoCarritoActualizado)
                    tvCantidad.text = nuevaCantidad.toString()
                    onUpdate()
                } else if (cantidadActual == 1) {
                    dbHelper.eliminarProducto(idUsuario, producto.idProducto)
                    Toast.makeText(root.context, "Producto eliminado", Toast.LENGTH_SHORT).show()

                    layoutControlesCantidad.visibility = View.GONE
                    btnAgregarCarrito.visibility = View.VISIBLE
                    onUpdate()
                }
            }
        }
    }

    override fun getItemCount(): Int = productos.size

    // ➤ Método para actualizar un producto específico en el RecyclerView
    fun actualizarProducto(producto: Producto) {
        val index = productos.indexOfFirst { it.idProducto == producto.idProducto }
        if (index != -1) {
            productos[index] = producto
            notifyItemChanged(index)
        }
    }

    fun filtrarProductos(query: String) {
        productos = if (query.isEmpty()) {
            productosOriginales.toMutableList()
        } else {
            productosOriginales.filter {
                it.nombreProducto.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

}
