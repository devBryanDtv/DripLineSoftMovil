package com.example.driplinesoftapp.ui.restaurante

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.data.Cliente
import com.example.driplinesoftapp.databinding.FragmentRestaurantesBinding
import com.google.android.material.tabs.TabLayout

class RestauranteFragment : Fragment() {

    private var _binding: FragmentRestaurantesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RestauranteViewModel by viewModels()
    private lateinit var clienteAdapter: ClienteAdapter
    private var listaClientesOriginal: List<Cliente> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRestaurantesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewClientes.layoutManager = LinearLayoutManager(requireContext())

        clienteAdapter = ClienteAdapter(emptyList())
        binding.recyclerViewClientes.adapter = clienteAdapter

        viewModel.cargarClientes()

        viewModel.clientes.observe(viewLifecycleOwner) { clientes ->
            listaClientesOriginal = clientes
            clienteAdapter.actualizarLista(clientes)

            // 🔹 Mostrar u ocultar el mensaje informativo
            if (clientes.isEmpty()) {
                binding.tvNoResultados.visibility = View.VISIBLE
                binding.recyclerViewClientes.visibility = View.GONE
            } else {
                binding.tvNoResultados.visibility = View.GONE
                binding.recyclerViewClientes.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        configurarSearchView()
        configurarTabLayout()
    }

    private fun configurarSearchView() {
        binding.searchViewClientes.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarClientes(query?.trim() ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarClientes(newText?.trim() ?: "")
                return true
            }
        })
    }

    private fun configurarTabLayout() {
        binding.tabLayoutSectores.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filtrarClientes(binding.searchViewClientes.query.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filtrarClientes(query: String) {
        val sectorSeleccionado = binding.tabLayoutSectores.selectedTabPosition
        val sectorFiltro = when (sectorSeleccionado) {
            1 -> "cafetería"
            2 -> "restaurante"
            3 -> "otro"
            else -> null
        }

        val clientesFiltrados = listaClientesOriginal.filter { cliente ->
            val coincideSector = sectorFiltro == null || cliente.sector.equals(sectorFiltro, ignoreCase = true)
            val coincideTexto = query.isEmpty() || cliente.nombreComercial.contains(query, ignoreCase = true)
            coincideSector && coincideTexto
        }

        clienteAdapter.actualizarLista(clientesFiltrados)

        // 🔹 Mostrar u ocultar el mensaje informativo en el filtro
        if (clientesFiltrados.isEmpty()) {
            binding.tvNoResultados.visibility = View.VISIBLE
            binding.recyclerViewClientes.visibility = View.GONE
        } else {
            binding.tvNoResultados.visibility = View.GONE
            binding.recyclerViewClientes.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
