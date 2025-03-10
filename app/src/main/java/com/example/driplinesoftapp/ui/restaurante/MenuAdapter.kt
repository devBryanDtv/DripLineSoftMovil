package com.example.driplinesoftapp.ui.restaurante

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.driplinesoftapp.ProductoActivity
import com.example.driplinesoftapp.data.Menu
import com.example.driplinesoftapp.databinding.ItemMenuBinding

class MenuAdapter(
    private val menus: List<Menu>,
    private val onItemClick: ((Menu) -> Unit)? = null  // ✅ Se agrega el evento de clic opcional
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

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
                }
                root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = menus.size
}
