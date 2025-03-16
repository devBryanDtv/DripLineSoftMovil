package com.example.driplinesoftapp.ui.restaurante

import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.driplinesoftapp.ProductoActivity
import com.example.driplinesoftapp.data.Menu
import com.example.driplinesoftapp.databinding.ItemMenuBinding

class MenuAdapter(
    private var menus: List<Menu>,
    private val idSucursal: Int,
    private val nombreSucursal: String?,
    private val nombreComercial: String?,
    private val logoCliente: String?
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private var menusOriginales: List<Menu> = menus.toList()

    inner class MenuViewHolder(val binding: ItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menus[position]

        with(holder.binding) {
            tvNombreMenu.text = Html.fromHtml("<b>${menu.nombreMenu}</b>")
            tvCategoria.text = Html.fromHtml("<b>Categoría:</b> ${menu.categoria}")

            // Efecto visual al presionar
            root.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
                false
            }

            root.setOnClickListener {
                val intent = Intent(root.context, ProductoActivity::class.java).apply {
                    putExtra("ID_MENU", menu.idMenu)
                    putExtra("NOMBRE_MENU", menu.nombreMenu)
                    putExtra("ID_SUCURSAL", idSucursal)
                    putExtra("NOMBRE_SUCURSAL", nombreSucursal)
                    putExtra("NOMBRE_COMERCIAL", nombreComercial)
                    putExtra("LOGO_CLIENTE", logoCliente)
                }
                root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = menus.size

    fun actualizarLista(nuevaLista: List<Menu>) {
        menusOriginales = nuevaLista.toList()
        menus = nuevaLista
        notifyDataSetChanged()
    }
}
