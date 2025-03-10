package com.example.driplinesoftapp.ui.sucursal

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.data.Sucursal
import com.example.driplinesoftapp.databinding.ItemSucursalBinding
import com.example.driplinesoftapp.MenuActivity

class SucursalAdapter(
    private val sucursales: List<Sucursal>,
    private val logoCliente: String // Se pasa el logo del cliente desde la actividad
) : RecyclerView.Adapter<SucursalAdapter.SucursalViewHolder>() {

    inner class SucursalViewHolder(val binding: ItemSucursalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SucursalViewHolder {
        val binding = ItemSucursalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SucursalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SucursalViewHolder, position: Int) {
        val sucursal = sucursales[position]
        with(holder.binding) {
            tvNombreSucursal.text = sucursal.nombreSucursal
            tvDireccion.text = "Dirección: ${sucursal.direccion ?: "No disponible"}"
            tvTelefono.text = "Teléfono: ${sucursal.telefono ?: "No disponible"}"
            tvHorario.text = "Horario: ${sucursal.horarioAtencion ?: "No disponible"}"

            // Cargar el logo del cliente en cada card de sucursal
            Glide.with(ivLogoSucursal.context)
                .load(logoCliente)
                .placeholder(R.drawable.ic_logo)
                .into(ivLogoSucursal)

            // Click para abrir el activity de menús
            root.setOnClickListener {
                val intent = Intent(root.context, MenuActivity::class.java).apply {
                    putExtra("ID_SUCURSAL", sucursal.idSucursal)
                }
                root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = sucursales.size
}
