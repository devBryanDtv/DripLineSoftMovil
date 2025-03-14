package com.example.driplinesoftapp.ui.sucursal_negocio

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Sucursal
import com.example.driplinesoftapp.data_negocio.SucursalToggleResponse
import com.example.driplinesoftapp.databinding.ItemSucursalBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SucursalAdapter(
    private var sucursales: List<Sucursal>,
    private val logoCliente: String,
    private val nombreComercial: String
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

            // Mostrar el Switch y establecer el estado actual de la sucursal
            switchSucursal.visibility = View.VISIBLE

            // 🔹 Remover temporalmente el listener antes de asignar el estado
            switchSucursal.setOnCheckedChangeListener(null)
            switchSucursal.isChecked = sucursal.activa == true
            Log.d("SucursalAdapter", "Sucursal ${sucursal.nombreSucursal} - Estado inicial: ${sucursal.activa}")

            // 🔹 Volver a agregar el listener solo después de la actualización
            switchSucursal.setOnCheckedChangeListener { _, isChecked ->
                Log.d("SucursalAdapter", "Toggle activado en sucursal ID: ${sucursal.idSucursal}, Activar: $isChecked")
                cambiarEstadoSucursal(sucursal.idSucursal, isChecked, holder)
            }

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
        }
    }

    override fun getItemCount(): Int = sucursales.size

    /** Método para activar/desactivar la sucursal en Retrofit */
    private fun cambiarEstadoSucursal(idSucursal: Int, activar: Boolean, holder: SucursalViewHolder) {
        RetrofitClient.instance.toggleSucursal(idSucursal).enqueue(object : Callback<SucursalToggleResponse> {
            override fun onResponse(call: Call<SucursalToggleResponse>, response: Response<SucursalToggleResponse>) {
                if (response.isSuccessful && response.body()?.exito == true) {
                    val estado = response.body()?.estado
                    val mensaje = response.body()?.mensaje ?: "Estado actualizado"

                    // 🔹 Actualizar solo el Switch correspondiente, sin `notifyDataSetChanged()`
                    holder.binding.switchSucursal.isChecked = estado == true
                    Log.d("SucursalAdapter", "✅ Sucursal ID: $idSucursal - Estado actualizado correctamente a: ${estado}")
                    Toast.makeText(holder.itemView.context, mensaje, Toast.LENGTH_SHORT).show()
                } else {
                    holder.binding.switchSucursal.isChecked = !activar // Revertir cambio en caso de error
                    Log.e("SucursalAdapter", "❌ Error en la actualización: ${response.errorBody()?.string()}")
                    Toast.makeText(holder.itemView.context, "Error en la actualización", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SucursalToggleResponse>, t: Throwable) {
                holder.binding.switchSucursal.isChecked = !activar // Revertir cambio en caso de error
                Log.e("SucursalAdapter", "❌ Error de conexión: ${t.message}")
                Toast.makeText(holder.itemView.context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Actualiza la lista de sucursales */
    fun actualizarLista(nuevaLista: List<Sucursal>) {
        sucursalesOriginales = nuevaLista.toList()
        sucursales = nuevaLista
        notifyDataSetChanged()
    }
}
