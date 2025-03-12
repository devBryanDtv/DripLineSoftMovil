package com.example.driplinesoftapp.ui.restaurante

import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.SucursalActivity
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
            tvNombreComercial.text = cliente.nombreComercial
            tvSector.text = "Sector: ${cliente.sector}"
            tvEstadoSuscripcion.text = "Suscripción: ${cliente.estadoSuscripcion}"

            Glide.with(ivLogo.context)
                .load(cliente.logo)
                .placeholder(R.drawable.ic_logo)
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
                    putExtra("LOGO", cliente.logo)
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
