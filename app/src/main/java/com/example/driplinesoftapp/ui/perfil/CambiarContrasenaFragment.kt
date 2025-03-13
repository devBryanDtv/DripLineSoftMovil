package com.example.driplinesoftapp.ui.perfil

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.CambiarContrasenaRequest
import com.example.driplinesoftapp.data.PassResponse
import com.example.driplinesoftapp.databinding.FragmentCambiarContrasenaBinding
import com.example.driplinesoftapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CambiarContrasenaFragment : Fragment(), View.OnTouchListener {

    private var _binding: FragmentCambiarContrasenaBinding? = null
    private val binding get() = _binding!!
    private var startX = 0f
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCambiarContrasenaBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())

        // Configurar ActionBar con flecha de regreso
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        setHasOptionsMenu(true) // Habilitar el menú

        // Configurar el botón de cambio de contraseña
        binding.btnCambiarContrasena.setOnClickListener {
            validarYEnviar()
        }

        // Habilitar gestos de deslizamiento para volver atrás
        binding.root.setOnTouchListener(this)

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Log.d("CambiarContrasena", "Regresando al fragmento anterior con la flecha")
                findNavController().popBackStack() // Regresar al fragmento anterior
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun validarYEnviar() {
        val contrasenaActual = binding.etContrasenaActual.text.toString().trim()
        val nuevaContrasena = binding.etNuevaContrasena.text.toString().trim()
        val confirmarContrasena = binding.etConfirmarContrasena.text.toString().trim()

        Log.d("CambiarContrasena", "Validando contraseñas...")

        if (contrasenaActual.isEmpty() || nuevaContrasena.isEmpty() || confirmarContrasena.isEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios")
            Log.e("CambiarContrasena", "Error: Hay campos vacíos")
            return
        }

        if (!esContrasenaSegura(nuevaContrasena)) {
            mostrarMensaje("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial")
            Log.e("CambiarContrasena", "Error: La nueva contraseña no cumple los requisitos")
            return
        }

        if (nuevaContrasena != confirmarContrasena) {
            mostrarMensaje("Las contraseñas no coinciden")
            Log.e("CambiarContrasena", "Error: La confirmación no coincide")
            return
        }

        Log.d("CambiarContrasena", "Validación exitosa. Enviando solicitud...")
        cambiarContrasena(contrasenaActual, nuevaContrasena)
    }

    private fun esContrasenaSegura(password: String): Boolean {
        val regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$"
        val esValida = password.matches(regex.toRegex())

        Log.d("VALIDACION_PASSWORD", "Contraseña ingresada: $password")
        Log.d("VALIDACION_PASSWORD", "¿Cumple requisitos?: $esValida")

        return esValida
    }

    private fun cambiarContrasena(contrasenaActual: String, nuevaContrasena: String) {
        val usuario = sessionManager.getUser() ?: return
        val idUsuario = usuario.idUsuario

        Log.d("CambiarContrasena", "ID Usuario: $idUsuario")
        Log.d("CambiarContrasena", "Enviando solicitud al servidor...")

        val request = CambiarContrasenaRequest(idUsuario, contrasenaActual, nuevaContrasena)

        RetrofitClient.instance.cambiarContrasena(request)
            .enqueue(object : Callback<PassResponse> {
                override fun onResponse(call: Call<PassResponse>, response: Response<PassResponse>) {
                    Log.d("CambiarContrasena", "Respuesta recibida del servidor: ${response.code()}")

                    if (response.isSuccessful && response.body()?.success == true) {
                        mostrarMensaje("Contraseña actualizada correctamente")
                        Log.d("CambiarContrasena", "Contraseña cambiada con éxito")
                        findNavController().popBackStack()
                    } else {
                        val errorMessage = response.body()?.message ?: "Error al actualizar contraseña"
                        mostrarMensaje(errorMessage)
                        Log.e("CambiarContrasena", "Error: $errorMessage")

                        response.body()?.errors?.forEach { (campo, errores) ->
                            errores.forEach { error ->
                                Log.e("CambiarContrasena", "Error en campo $campo: $error")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<PassResponse>, t: Throwable) {
                    mostrarMensaje("Error de conexión: ${t.message}")
                    Log.e("CambiarContrasena", "Error de conexión: ${t.message}")
                }
            })
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> startX = event.x
            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - startX
                if (deltaX > 200) {
                    Log.d("CambiarContrasena", "Regresando al fragmento anterior por gesto")
                    findNavController().popBackStack()
                }
            }
        }
        return true
    }
}
