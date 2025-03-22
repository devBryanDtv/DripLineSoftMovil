package com.example.driplinesoftapp.ui.perfil

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.driplinesoftapp.R
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.CambiarContrasenaRequest
import com.example.driplinesoftapp.data.PassResponse
import com.example.driplinesoftapp.databinding.FragmentCambiarContrasenaBinding
import com.example.driplinesoftapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
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
        setHasOptionsMenu(true)

        // Configurar el botón de cambio de contraseña
        binding.btnCambiarContrasena.setOnClickListener {
            validarYEnviar()
        }

        // Validación en tiempo real para cada campo
        configurarValidacionTiempoReal()

        // Habilitar gestos de deslizamiento para volver atrás
        binding.root.setOnTouchListener(this)

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack() // Regresar al fragmento anterior
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** ➤ Configura la validación en tiempo real para cada campo */
    private fun configurarValidacionTiempoReal() {
        binding.etContrasenaActual.addTextChangedListener(textWatcher)
        binding.etNuevaContrasena.addTextChangedListener(textWatcher)
        binding.etConfirmarContrasena.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            validarCamposEnTiempoReal()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    /** ➤ Validar campos en tiempo real */
    private fun validarCamposEnTiempoReal() {
        val nuevaContrasena = binding.etNuevaContrasena.text.toString().trim()
        val confirmarContrasena = binding.etConfirmarContrasena.text.toString().trim()

        if (!esContrasenaSegura(nuevaContrasena)) {
            binding.tilNuevaContrasena.error =
                "Debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
        } else {
            binding.tilNuevaContrasena.error = null
        }

        if (nuevaContrasena != confirmarContrasena) {
            binding.tilConfirmarContrasena.error = "Las contraseñas no coinciden"
        } else {
            binding.tilConfirmarContrasena.error = null
        }
    }

    private fun validarYEnviar() {
        val contrasenaActual = binding.etContrasenaActual.text.toString().trim()
        val nuevaContrasena = binding.etNuevaContrasena.text.toString().trim()
        val confirmarContrasena = binding.etConfirmarContrasena.text.toString().trim()

        var esValido = true

        if (contrasenaActual.isEmpty()) {
            binding.tilContrasenaActual.error = "Campo obligatorio"
            esValido = false
        } else {
            binding.tilContrasenaActual.error = null
        }

        if (!esContrasenaSegura(nuevaContrasena)) {
            binding.tilNuevaContrasena.error =
                "Debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
            esValido = false
        } else {
            binding.tilNuevaContrasena.error = null
        }

        if (nuevaContrasena != confirmarContrasena) {
            binding.tilConfirmarContrasena.error = "Las contraseñas no coinciden"
            esValido = false
        } else {
            binding.tilConfirmarContrasena.error = null
        }

        if (!esValido) {
            mostrarSnackbar("Corrige los errores antes de continuar", false)
            return
        }

        mostrarProgreso(true)
        cambiarContrasena(contrasenaActual, nuevaContrasena)
    }

    private fun esContrasenaSegura(password: String): Boolean {
        val regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$"
        return password.matches(regex.toRegex())
    }

    private fun cambiarContrasena(contrasenaActual: String, nuevaContrasena: String) {
        val usuario = sessionManager.getUser() ?: return
        val idUsuario = usuario.idUsuario

        val request = CambiarContrasenaRequest(idUsuario, contrasenaActual, nuevaContrasena)

        RetrofitClient.instance.cambiarContrasena(request)
            .enqueue(object : Callback<PassResponse> {
                override fun onResponse(call: Call<PassResponse>, response: Response<PassResponse>) {
                    mostrarProgreso(false)

                    when (response.code()) {
                        200 -> {
                            mostrarSnackbar("Contraseña actualizada correctamente", true)
                            findNavController().popBackStack()
                        }
                        400 -> {
                            val errorDetails = response.body()?.errors
                            val errorMessage = errorDetails?.let { obtenerErroresDetallados(it) }
                                ?: "Error de validación en los campos enviados"
                            mostrarSnackbar(errorMessage, false)
                        }
                        401 -> {
                            mostrarSnackbar("La contraseña actual no es correcta", false)
                        }
                        404 -> {
                            mostrarSnackbar("Usuario no encontrado", false)
                        }
                        500 -> {
                            mostrarSnackbar("Error interno del servidor. Inténtalo más tarde", false)
                        }
                        else -> {
                            val errorMessage = response.body()?.message ?: "Error desconocido"
                            mostrarSnackbar(errorMessage, false)
                        }
                    }
                }

                override fun onFailure(call: Call<PassResponse>, t: Throwable) {
                    mostrarProgreso(false)
                    mostrarSnackbar("Error de conexión: ${t.message}", false)
                }
            })
    }

    /** ➤ Generar mensaje detallado de errores */
    private fun obtenerErroresDetallados(errors: Map<String, List<String>>): String {
        val errores = StringBuilder("Corrige los siguientes errores:\n")
        errors.forEach { (campo, listaErrores) ->
            errores.append("$campo: ${listaErrores.joinToString(", ")}\n")
        }
        return errores.toString().trim()
    }

    /** ➤ Mostrar Snackbar con colores diferenciados */
    private fun mostrarSnackbar(mensaje: String, esExito: Boolean) {
        val colorFondo = if (esExito) android.R.color.background_dark else android.R.color.holo_red_dark

        Snackbar.make(requireView(), mensaje, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), colorFondo))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .setAction("OK") {}
            .show()
    }

    /** ➤ Mostrar u Ocultar Progreso */
    private fun mostrarProgreso(mostrar: Boolean) {
        binding.btnCambiarContrasena.isEnabled = !mostrar
        binding.btnCambiarContrasena.text = if (mostrar) "" else "Actualizar Contraseña"
        binding.progressBar.visibility = if (mostrar) VISIBLE else GONE
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> startX = event.x
            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - startX
                if (deltaX > 200) {
                    findNavController().popBackStack()
                }
            }
        }
        return true
    }
}
