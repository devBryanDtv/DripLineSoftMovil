package com.example.driplinesoftapp.ui.pedido

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.data.Pedido
import com.example.driplinesoftapp.databinding.FragmentPedidoBinding
import com.example.driplinesoftapp.utils.SessionManager

class PedidoFragment : Fragment() {

    private var _binding: FragmentPedidoBinding? = null
    private val binding get() = _binding!!
    private lateinit var pedidoViewModel: PedidoViewModel
    private lateinit var pedidoAdapter: PedidoAdapter
    private var listaPedidosOriginal: List<Pedido> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        pedidoViewModel = ViewModelProvider(this).get(PedidoViewModel::class.java)

        pedidoAdapter = PedidoAdapter(emptyList()) { activarSearchView() }
        binding.recyclerViewPedidos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pedidoAdapter
        }

        val sessionManager = SessionManager(requireContext())
        val usuario = sessionManager.getUser()
        val idUsuario = usuario?.idUsuario ?: return root

        Log.d("PedidoFragment", "ID Usuario autenticado: $idUsuario")

        pedidoViewModel.cargarHistorialPedidos(idUsuario)

        pedidoViewModel.pedidos.observe(viewLifecycleOwner) { pedidos ->
            listaPedidosOriginal = pedidos
            filtrarPedidos()
        }

        configurarSearchView()

        // Filtrado por estado usando TabLayout
        binding.tabLayoutEstados.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                filtrarPedidos(binding.searchViewPedidos.query.toString())
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        return root
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

        // Activar automáticamente el SearchView
        binding.searchViewPedidos.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.searchViewPedidos.isIconified = false
        }

        // Cerrar SearchView al presionar fuera
        binding.root.setOnClickListener {
            binding.searchViewPedidos.clearFocus()
            ocultarTeclado()
        }
    }

    private fun ocultarTeclado() {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.searchViewPedidos.windowToken, 0)
    }

    private fun activarSearchView() {
        binding.searchViewPedidos.isIconified = false
        binding.searchViewPedidos.requestFocus()
    }

    private fun filtrarPedidos(query: String? = null) {
        val estadoSeleccionado = binding.tabLayoutEstados.selectedTabPosition
        val estadoFiltro = when (estadoSeleccionado) {
            1 -> "pendiente"
            2 -> "en preparación"
            3 -> "listo"
            4 -> "cancelado"
            else -> null
        }

        val listaFiltrada = listaPedidosOriginal.filter { pedido ->
            val coincideEstado = estadoFiltro == null || pedido.estado.equals(estadoFiltro, true)
            val coincideTexto = query.isNullOrEmpty() ||
                    pedido.nombreComercial.contains(query, ignoreCase = true) ||
                    pedido.nombreSucursal.contains(query, ignoreCase = true) ||
                    pedido.fechaPedido.contains(query, ignoreCase = true) ||
                    pedido.metodoPago.contains(query, ignoreCase = true) ||
                    pedido.estado.contains(query, ignoreCase = true) ||
                    (pedido.nota?.contains(query, ignoreCase = true) ?: false) ||
                    pedido.detalles.any { detalle ->
                        detalle.nombreProducto.contains(query, ignoreCase = true) ||
                                detalle.cantidad.toString().contains(query.trim()) ||
                                detalle.subtotal.toString().contains(query.trim())
                    }

            coincideEstado && coincideTexto
        }

        pedidoAdapter.actualizarLista(listaFiltrada)
        binding.tvNoPedidos.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
