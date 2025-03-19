package com.example.driplinesoftapp.ui.sucursal_negocio

import android.util.Log
import android.text.Html
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Sucursal
import com.example.driplinesoftapp.data_negocio.SucursalToggleResponse
import com.example.driplinesoftapp.databinding.ItemSucursalBinding
import com.google.android.material.snackbar.Snackbar
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
            tvNombreSucursal.text = Html.fromHtml("<b>${sucursal.nombreSucursal}</b>")
            tvDireccion.text = Html.fromHtml("<b>Dirección:</b> ${sucursal.direccion ?: "No disponible"}")
            tvTelefono.text = Html.fromHtml("<b>Teléfono:</b> ${sucursal.telefono ?: "No disponible"}")

            // Formatear el horario usando Html.fromHtml
            val horarioFormateado = formatearHorario(sucursal.horarioAtencion)
            tvHorario.text = Html.fromHtml(horarioFormateado)

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

            // Reemplazar Glide por la asignación directa del ícono
            ivLogoSucursal.setImageResource(R.drawable.ic_restaurante_sucursal)

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

                    holder.binding.switchSucursal.isChecked = estado == true
                    Log.d("SucursalAdapter", "✅ Sucursal ID: $idSucursal - Estado actualizado correctamente a: ${estado}")
                    mostrarSnackbar(holder, mensaje)
                } else {
                    holder.binding.switchSucursal.isChecked = !activar
                    Log.e("SucursalAdapter", "❌ Error en la actualización: ${response.errorBody()?.string()}")
                    mostrarSnackbar(holder, "Error en la actualización")
                }
            }

            override fun onFailure(call: Call<SucursalToggleResponse>, t: Throwable) {
                holder.binding.switchSucursal.isChecked = !activar
                Log.e("SucursalAdapter", "❌ Error de conexión: ${t.message}")
                mostrarSnackbar(holder, "Error de conexión")
            }
        })
    }

    /** Método para mostrar un Snackbar */
    private fun mostrarSnackbar(holder: SucursalViewHolder, mensaje: String) {
        Snackbar.make(holder.binding.root, mensaje, Snackbar.LENGTH_SHORT).show()
    }

    /** Actualiza la lista de sucursales */
    fun actualizarLista(nuevaLista: List<Sucursal>) {
        sucursalesOriginales = nuevaLista.toList()
        sucursales = nuevaLista
        notifyDataSetChanged()
    }

    /** 🔹 Formatea el horario al formato solicitado */
    private fun formatearHorario(horario: String?): String {
        return if (!horario.isNullOrEmpty()) {
            val regex = Regex("""(\d{2}):(\d{2}) - (\d{2}):(\d{2})""")
            val match = regex.find(horario)

            if (match != null) {
                val inicio = match.groupValues[1].toInt()
                val fin = match.groupValues[3].toInt()

                val horaInicio = if (inicio < 12) "$inicio:00 am" else "${inicio - 12}:00 pm"
                val horaFin = if (fin < 12) "$fin:00 am" else "${fin - 12}:00 pm"

                "<b>Días de la semana:</b> De lunes a viernes<br>" +
                        "<b>Descanso:</b> Sábado y domingo<br>" +
                        "<b>Horario:</b> De $horaInicio a $horaFin"
            } else {
                "<b>Horario:</b> No disponible"
            }
        } else {
            "<b>Horario:</b> No disponible"
        }
    }
}
