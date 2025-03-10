package com.example.driplinesoftapp.ui.sucursal_negocio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.driplinesoftapp.databinding.FragmentSucursalesNegocioBinding

class SucursalFragment : Fragment() {

    private var _binding: FragmentSucursalesNegocioBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(SucursalViewModel::class.java)

        _binding = FragmentSucursalesNegocioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSucursal
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}