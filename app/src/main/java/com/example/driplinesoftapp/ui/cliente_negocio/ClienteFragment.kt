package com.example.driplinesoftapp.ui.cliente_negocio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.databinding.FragmentClientesNegocioBinding
import com.example.driplinesoftapp.models.Usuario
import com.example.driplinesoftapp.utils.SessionManager

class ClienteFragment : Fragment() {

    private var _binding: FragmentClientesNegocioBinding? = null
    private val binding get() = _binding!!

    private lateinit var clienteViewModel: ClienteViewModel
    private lateinit var usuarioAdapter: UsuarioAdapter
    private var listaUsuariosOriginal: List<Usuario> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        clienteViewModel = ViewModelProvider(this)[ClienteViewModel::class.java]
        _binding = FragmentClientesNegocioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar RecyclerView
        usuarioAdapter = UsuarioAdapter(emptyList())
        binding.recyclerViewUsuarios.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewUsuarios.adapter = usuarioAdapter

        // Obtener ID del cliente desde SessionManager
        val sessionManager = SessionManager(requireContext())
        val usuario = sessionManager.getUser()

        if (usuario == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val idCliente = usuario.idUsuario

        // Mostrar ProgressBar
        binding.progressBar.visibility = View.VISIBLE

        clienteViewModel.obtenerUsuariosPorCliente(idCliente)

        clienteViewModel.usuarios.observe(viewLifecycleOwner) { usuarios ->
            binding.progressBar.visibility = View.GONE
            listaUsuariosOriginal = usuarios
            if (usuarios.isEmpty()) {
                Toast.makeText(requireContext(), "No hay usuarios asociados", Toast.LENGTH_SHORT).show()
            } else {
                usuarioAdapter.actualizarLista(usuarios)
            }
        }

        clienteViewModel.errorMessage.observe(viewLifecycleOwner) { mensajeError ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), mensajeError, Toast.LENGTH_SHORT).show()
        }

        configurarSearchView()
    }

    private fun configurarSearchView() {
        binding.searchViewUsuarios.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarUsuarios(query?.trim() ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarUsuarios(newText?.trim() ?: "")
                return true
            }
        })
    }

    private fun filtrarUsuarios(query: String) {
        val usuariosFiltrados = listaUsuariosOriginal.filter { usuario ->
            usuario.nombre.contains(query, ignoreCase = true) ||
                    usuario.email.contains(query, ignoreCase = true)
        }

        usuarioAdapter.actualizarLista(usuariosFiltrados)

        // Mostrar mensaje si no hay resultados
        if (usuariosFiltrados.isEmpty()) {
            binding.tvNoResultados.visibility = View.VISIBLE
        } else {
            binding.tvNoResultados.visibility = View.GONE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
