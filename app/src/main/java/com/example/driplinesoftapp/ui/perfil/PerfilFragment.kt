package com.example.driplinesoftapp.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.driplinesoftapp.Login
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.SplashActivity
import com.example.driplinesoftapp.databinding.FragmentPerfilBinding
import com.example.driplinesoftapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar

class PerfilFragment : Fragment(), View.OnTouchListener {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var startX = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        configurarBotones()

        // Habilitar gestos de deslizamiento para volver atrás
        binding.root.setOnTouchListener(this)

        return binding.root
    }

    private fun configurarBotones() {
        agregarAnimacion(binding.cardVerPerfil) {
            findNavController().navigate(R.id.action_perfilFragment_to_verPerfilFragment)
        }

        agregarAnimacion(binding.cardCambiarContrasena) {
            findNavController().navigate(R.id.action_perfilFragment_to_cambiarContrasenaFragment)
        }

        agregarAnimacion(binding.cardCerrarSesion) {
            cerrarSesion()
        }

        agregarAnimacion(binding.cardAcercaDe) {
            findNavController().navigate(R.id.navigation_acerca_de)
        }
    }

    private fun cerrarSesion() {
        sessionManager.logout()
        mostrarMensaje("Sesión cerrada correctamente")

        // Mostrar el Splash Screen con el mensaje "Cerrando sesión..."
        val intent = Intent(requireContext(), SplashActivity::class.java)
        intent.putExtra("MENSAJE_SPLASH", "Cerrando sesión...") // Agrega el mensaje personalizado
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun mostrarMensaje(mensaje: String) {
        val rootView = requireActivity().findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, mensaje, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ➤ Implementación del gesto para regresar deslizando hacia la derecha
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> startX = event.x
            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - startX
                if (deltaX > 200) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        return true
    }

    // ➤ Agregar animación de escala al presionar una CardView
    private fun agregarAnimacion(view: View, onClickAction: () -> Unit) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val scaleDown = ScaleAnimation(
                        1f, 0.95f,
                        1f, 0.95f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f
                    )
                    scaleDown.duration = 100
                    scaleDown.fillAfter = true
                    v.startAnimation(scaleDown)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val scaleUp = ScaleAnimation(
                        0.95f, 1f,
                        0.95f, 1f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f
                    )
                    scaleUp.duration = 100
                    scaleUp.fillAfter = true
                    v.startAnimation(scaleUp)
                    onClickAction()
                }
            }
            false
        }
    }
}
