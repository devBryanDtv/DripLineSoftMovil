package com.example.driplinesoftapp.ui.restaurante

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driplinesoftapp.databinding.FragmentRestaurantesBinding

class RestauranteFragment : Fragment() {

    private var _binding: FragmentRestaurantesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RestauranteViewModel by viewModels()

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

        viewModel.cargarClientes()

        viewModel.clientes.observe(viewLifecycleOwner) { clientes ->
            binding.recyclerViewClientes.adapter = ClienteAdapter(clientes)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
