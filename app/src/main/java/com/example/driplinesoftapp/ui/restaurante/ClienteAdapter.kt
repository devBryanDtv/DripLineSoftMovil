package com.example.driplinesoftapp.ui.restaurante

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.SucursalActivity
import com.example.driplinesoftapp.data.Cliente
import com.example.driplinesoftapp.databinding.ItemClienteBinding

class ClienteAdapter(private val clientes: List<Cliente>) :
    RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

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

            // Cargar imagen del logo con Glide
            Glide.with(ivLogo.context)
                .load(cliente.logo)
                .placeholder(R.drawable.ic_logo)
                .into(ivLogo)

            root.setOnClickListener {
                val intent = Intent(root.context, SucursalActivity::class.java)
                intent.putExtra("LOGO", cliente.logo)
                intent.putExtra("ID_CLIENTE", cliente.idCliente)
                root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = clientes.size
}
