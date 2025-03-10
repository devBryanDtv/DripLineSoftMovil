package com.example.driplinesoftapp.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.driplinesoftapp.Login
import com.example.driplinesoftapp.databinding.FragmentPerfilBinding
import com.example.driplinesoftapp.utils.SessionManager

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())

        configurarBotones()

        return binding.root
    }

    private fun configurarBotones() {
        binding.cardVerPerfil.setOnClickListener {
            mostrarMensaje("Ver Perfil - En construcción")
        }

        binding.cardCambiarContrasena.setOnClickListener {
            mostrarMensaje("Cambiar Contraseña - En construcción")
        }

        binding.cardCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        sessionManager.logout()
        mostrarMensaje("Sesión cerrada correctamente")
        val intent = Intent(requireContext(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
