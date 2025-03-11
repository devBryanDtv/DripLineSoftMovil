package com.example.driplinesoftapp.ui.pedido

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.databinding.FragmentPedidoBinding
import com.example.driplinesoftapp.utils.SessionManager

class PedidoFragment : Fragment() {

    private var _binding: FragmentPedidoBinding? = null
    private val binding get() = _binding!!
    private lateinit var pedidoViewModel: PedidoViewModel
    private lateinit var pedidoAdapter: PedidoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        pedidoViewModel = ViewModelProvider(this).get(PedidoViewModel::class.java)

        pedidoAdapter = PedidoAdapter(emptyList())
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
            pedidoAdapter.actualizarLista(pedidos)
            binding.tvNoPedidos.visibility = if (pedidos.isEmpty()) View.VISIBLE else View.GONE
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
