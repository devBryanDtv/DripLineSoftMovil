package com.example.driplinesoftapp.ui.sucursal_negocio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.data.Sucursal
import com.example.driplinesoftapp.databinding.FragmentSucursalesNegocioBinding
import com.example.driplinesoftapp.utils.SessionManager

class SucursalFragment : Fragment() {

    private var _binding: FragmentSucursalesNegocioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SucursalViewModel by viewModels()
    private lateinit var sucursalAdapter: SucursalAdapter
    private var listaSucursalesOriginal: List<Sucursal> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSucursalesNegocioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar RecyclerView
        binding.recyclerViewSucursales.layoutManager = LinearLayoutManager(requireContext())
        sucursalAdapter = SucursalAdapter(emptyList(), "", "") // Logo y nombre comercial aún no disponibles
        binding.recyclerViewSucursales.adapter = sucursalAdapter

        // Obtener ID del cliente desde SessionManager
        val sessionManager = SessionManager(requireContext())
        val usuario = sessionManager.getUser()
        val idCliente = usuario?.idUsuario ?: return

        viewModel.cargarSucursales(idCliente)

        viewModel.sucursales.observe(viewLifecycleOwner) { sucursales ->
            listaSucursalesOriginal = sucursales
            if (sucursales.isEmpty()) {
                binding.tvNoResultados.visibility = View.VISIBLE
            } else {
                binding.tvNoResultados.visibility = View.GONE
                sucursalAdapter.actualizarLista(sucursales)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        configurarSearchView()
    }

    private fun configurarSearchView() {
        binding.searchViewSucursales.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarSucursales(query?.trim() ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarSucursales(newText?.trim() ?: "")
                return true
            }
        })
    }

    private fun filtrarSucursales(query: String) {
        val sucursalesFiltradas = listaSucursalesOriginal.filter { sucursal ->
            sucursal.nombreSucursal.contains(query, ignoreCase = true) ||
                    sucursal.direccion?.contains(query, ignoreCase = true) == true ||
                    sucursal.telefono?.contains(query, ignoreCase = true) == true
        }

        if (sucursalesFiltradas.isEmpty()) {
            binding.tvNoResultados.visibility = View.VISIBLE
        } else {
            binding.tvNoResultados.visibility = View.GONE
        }

        sucursalAdapter.actualizarLista(sucursalesFiltradas)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
