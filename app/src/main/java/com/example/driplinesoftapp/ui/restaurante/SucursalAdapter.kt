package com.example.driplinesoftapp.ui.sucursal

import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.data.Sucursal
import com.example.driplinesoftapp.databinding.ItemSucursalBinding
import com.example.driplinesoftapp.MenuActivity

class SucursalAdapter(
    private var sucursales: List<Sucursal>,
    private val logoCliente: String,
    private val nombreComercial: String // Nombre comercial del cliente
) : RecyclerView.Adapter<SucursalAdapter.SucursalViewHolder>() {

    private var sucursalesOriginales: List<Sucursal> = sucursales.toList()

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

            // Efecto de animación al tocar la card
            root.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
                false
            }

            // Click para abrir el activity de menús
            root.setOnClickListener {
                val intent = Intent(root.context, MenuActivity::class.java).apply {
                    putExtra("ID_SUCURSAL", sucursal.idSucursal)
                    putExtra("LOGO_CLIENTE", logoCliente)
                    putExtra("NOMBRE_SUCURSAL", sucursal.nombreSucursal)
                    putExtra("NOMBRE_COMERCIAL", nombreComercial)
                }
                root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = sucursales.size

    /** Actualiza la lista completa de sucursales */
    fun actualizarLista(nuevaLista: List<Sucursal>) {
        sucursalesOriginales = nuevaLista.toList()
        sucursales = nuevaLista
        notifyDataSetChanged()
    }

}
