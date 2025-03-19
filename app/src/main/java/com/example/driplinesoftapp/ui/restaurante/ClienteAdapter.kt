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
import com.example.driplinesoftapp.SucursalActivity
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Cliente
import com.example.driplinesoftapp.databinding.ItemClienteBinding

class ClienteAdapter(private var clientes: List<Cliente>) :
    RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    private var clientesOriginales: List<Cliente> = clientes.toList()

    inner class ClienteViewHolder(val binding: ItemClienteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClienteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        with(holder.binding) {
            tvNombreComercial.text = Html.fromHtml("<b>${cliente.nombreComercial}</b>")
            tvSector.text = Html.fromHtml("<b>Sector:</b> ${cliente.sector}")
            tvEstadoSuscripcion.text = Html.fromHtml("<b>Suscripción:</b> ${cliente.estadoSuscripcion}")

            tvEstadoSuscripcion.visibility = View.GONE

            // Construir la URL completa de la imagen
            val logoUrl = RetrofitClient.BASE_URL_IMAGENES + cliente.logo

            Glide.with(ivLogo.context)
                .load(logoUrl)
                .placeholder(R.drawable.ic_sucursales)
                .into(ivLogo)

            // Efecto visual al presionar
            root.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
                false
            }

            root.setOnClickListener {
                val intent = Intent(root.context, SucursalActivity::class.java).apply {
                    putExtra("LOGO", logoUrl)
                    putExtra("ID_CLIENTE", cliente.idCliente)
                    putExtra("NOMBRE", cliente.nombreComercial)
                }
                root.context.startActivity(intent)
            }
        }
    }


    override fun getItemCount(): Int = clientes.size

    fun actualizarLista(nuevaLista: List<Cliente>) {
        clientesOriginales = nuevaLista.toList()
        clientes = nuevaLista
        notifyDataSetChanged()
    }

}
