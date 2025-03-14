package com.example.driplinesoftapp.ui.cliente_negocio

import java.text.SimpleDateFormat
import java.util.*
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.models.Usuario

class UsuarioAdapter(private var usuarios: List<Usuario>) :
    RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvFechaClienteDesde: TextView = itemView.findViewById(R.id.tvFechaClienteDesde)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_asociado, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]

        // Formatear datos en HTML con negritas
        holder.tvNombre.text = Html.fromHtml("<b>Nombre:</b> ${usuario.nombre}")
        holder.tvEmail.text = Html.fromHtml("<b>Correo:</b> ${usuario.email}")

        // Obtener la fecha de creación desde "pivot" y formatearla en español
        val fechaClienteDesde = formatearFecha(usuario.pivot?.createdAt)
        holder.tvFechaClienteDesde.text = Html.fromHtml("<b>Cliente desde:</b> $fechaClienteDesde")

    }

    override fun getItemCount(): Int = usuarios.size

    fun actualizarLista(nuevaLista: List<Usuario>) {
        usuarios = nuevaLista
        notifyDataSetChanged()
    }

    fun formatearFecha(fechaISO: String?): String {
        if (fechaISO.isNullOrEmpty()) return "Fecha desconocida"

        try {
            // Convertir de formato ISO 8601 a Date
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
            formatoEntrada.timeZone = TimeZone.getTimeZone("UTC") // Definir la zona horaria correcta
            val fecha = formatoEntrada.parse(fechaISO)

            // Formatear la fecha en español: martes 12 de marzo, 2025
            val formatoSalida = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", Locale("es", "ES"))
            return formatoSalida.format(fecha!!)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Fecha inválida"
        }
    }

}
