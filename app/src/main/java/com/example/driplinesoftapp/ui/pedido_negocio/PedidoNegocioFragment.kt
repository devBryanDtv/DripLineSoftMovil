package com.example.driplinesoftapp.ui.pedido_negocio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.data_negocio.PedidoNegocio
import com.example.driplinesoftapp.databinding.FragmentPedidoNegocioBinding
import com.example.driplinesoftapp.utils.SessionManager
import com.google.android.material.tabs.TabLayout

class PedidoNegocioFragment : Fragment() {

    companion object {
        fun newInstance() = PedidoNegocioFragment()
    }

    private val viewModel: PedidoNegocioViewModel by viewModels()
    private lateinit var pedidoAdapter: PedidoNegocioAdapter
    private var listaPedidosOriginal: List<PedidoNegocio> = emptyList()

    private var _binding: FragmentPedidoNegocioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidoNegocioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewPedidos.layoutManager = LinearLayoutManager(requireContext())

        // Inicializamos el adaptador con un lambda que maneja el click
        pedidoAdapter = PedidoNegocioAdapter(emptyList()) { pedido ->

        }

        binding.recyclerViewPedidos.adapter = pedidoAdapter

        val sessionManager = SessionManager(requireContext())
        val usuario = sessionManager.getUser()
        val idUsuario = usuario?.idUsuario ?: return

        viewModel.obtenerHistorialPedidos(idUsuario)

        viewModel.pedidos.observe(viewLifecycleOwner) { pedidos ->
            listaPedidosOriginal = pedidos
            pedidoAdapter.actualizarLista(pedidos)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            binding.tvNoPedidos.visibility = if (errorMessage.isNotEmpty()) View.VISIBLE else View.GONE
        }

        configurarSearchView()

        // Configuración de filtros (tabs)
        binding.tabLayoutEstados.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                filtrarPedidos(binding.searchViewPedidos.query.toString())
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun configurarSearchView() {
        binding.searchViewPedidos.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarPedidos(query?.trim())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarPedidos(newText?.trim())
                return true
            }
        })
    }

    private fun filtrarPedidos(query: String? = null) {
        val estadoSeleccionado = binding.tabLayoutEstados.selectedTabPosition
        val estadoFiltro = when (estadoSeleccionado) {
            1 -> "pendiente"
            2 -> "en preparación"
            3 -> "listo"
            4 -> "cancelado"
            5 -> "entregado"
            else -> null
        }

        // Filtrar los pedidos en función del estado y la búsqueda
        val listaFiltrada = listaPedidosOriginal.filter { pedido ->
            val coincideEstado = estadoFiltro == null || pedido.estado.equals(estadoFiltro, true)
            val coincideTexto = query.isNullOrEmpty() ||
                    pedido.nombreComercial.contains(query, ignoreCase = true) ||
                    pedido.nombreSucursal.contains(query, ignoreCase = true) ||
                    pedido.fechaPedido.contains(query, ignoreCase = true) ||
                    pedido.metodoPago.contains(query, ignoreCase = true) ||
                    pedido.estado.contains(query, ignoreCase = true)

            coincideEstado && coincideTexto
        }

        // Actualizar la lista de pedidos filtrados
        pedidoAdapter.actualizarLista(listaFiltrada)
        binding.tvNoPedidos.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
