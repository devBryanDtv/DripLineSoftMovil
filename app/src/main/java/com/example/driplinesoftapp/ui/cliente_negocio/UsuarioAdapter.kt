package com.example.driplinesoftapp.ui.cliente_negocio

import java.text.SimpleDateFormat
import java.util.*
import android.text.Html
import android.util.Log
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
        val fechaClienteDesde = formatearFecha(usuario.fechaCreacion)
        holder.tvFechaClienteDesde.text = Html.fromHtml("<b>Cliente desde:</b> $fechaClienteDesde")
    }

    override fun getItemCount(): Int = usuarios.size

    fun actualizarLista(nuevaLista: List<Usuario>) {
        usuarios = nuevaLista
        notifyDataSetChanged()
    }

    // ➤ Nuevo formato de fecha: Ejemplo → 12 de marzo, 2025 a las 10:45 AM
    private fun formatearFecha(fecha: String?): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            formatoEntrada.timeZone = TimeZone.getTimeZone("UTC")

            val formatoSalida = SimpleDateFormat("d 'de' MMMM, yyyy 'a las' h:mm a", Locale("es", "ES"))

            val fechaParseada = formatoEntrada.parse(fecha)
            formatoSalida.format(fechaParseada ?: Date())
        } catch (e: Exception) {
            Log.e("UsuarioAdapter", "❌ Error formateando fecha: ${e.message}")
            "Fecha no disponible"
        }
    }
}
