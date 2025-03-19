package com.example.driplinesoftapp.ui.restaurante

import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
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

        // Verificar que la sucursal esté activa antes de mostrarla
        if (sucursal.activa == true) {
            with(holder.binding) {
                tvNombreSucursal.text = Html.fromHtml("<b>${sucursal.nombreSucursal}</b>")
                tvDireccion.text = Html.fromHtml("<b>Dirección:</b> ${sucursal.direccion ?: "No disponible"}")
                tvTelefono.text = Html.fromHtml("<b>Teléfono:</b> ${sucursal.telefono ?: "No disponible"}")

                // Formatear el horario usando Html.fromHtml
                val horarioFormateado = formatearHorario(sucursal.horarioAtencion)
                tvHorario.text = Html.fromHtml(horarioFormateado)

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
        } else {
            // Si la sucursal no está activa, ocúltala del listado
            holder.binding.root.visibility = View.GONE
            holder.binding.root.layoutParams = RecyclerView.LayoutParams(0, 0)
        }
    }

    override fun getItemCount(): Int = sucursales.size

    /** Actualiza la lista completa de sucursales */
    fun actualizarLista(nuevaLista: List<Sucursal>) {
        sucursalesOriginales = nuevaLista.toList()

        // Filtrar solo las sucursales activas
        sucursales = nuevaLista.filter { it.activa == true }

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
