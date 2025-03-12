package com.example.driplinesoftapp.ui.restaurante

import android.content.Intent
import android.view.LayoutInflater
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
            tvNombreMenu.text = menu.nombreMenu
            tvCategoria.text = "Categoría: ${menu.categoria}"

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
