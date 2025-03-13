package com.example.driplinesoftapp.ui.perfil

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.driplinesoftapp.databinding.FragmentVerPerfilBinding
import com.example.driplinesoftapp.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class VerPerfilFragment : Fragment(), View.OnTouchListener {

    private var _binding: FragmentVerPerfilBinding? = null
    private val binding get() = _binding!!
    private var startX = 0f
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerPerfilBinding.inflate(inflater, container, false)

        // Configurar la flecha de regreso en la ActionBar
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        setHasOptionsMenu(true) // Habilita opciones en el menú

        // Obtener datos del usuario y mostrarlos
        sessionManager = SessionManager(requireContext())
        cargarDatosUsuario()

        // Habilitar gestos de deslizamiento para volver atrás
        binding.root.setOnTouchListener(this)

        return binding.root
    }

    private fun cargarDatosUsuario() {
        val usuario = sessionManager.getUser()

        if (usuario == null) {
            Log.e("VerPerfilFragment", "❌ No se encontró ningún usuario autenticado")
            return
        }

        // Formatear la fecha
        val fechaFormateada = formatearFecha(usuario.fechaCreacion)

        // Aplicar negrita a los subtítulos usando HTML
        binding.tvEmailUsuario.text = Html.fromHtml("<b>Correo electrónico:</b> ${usuario.email}")
        binding.tvFechaCreacion.text = Html.fromHtml("<b>Te uniste desde:</b> $fechaFormateada")
        binding.tvNombreUsuario.text = usuario.nombre

        // 📌 Logs para ver si todo se asigna correctamente
        Log.d("VerPerfilFragment", "✅ Nombre: ${binding.tvNombreUsuario.text}")
        Log.d("VerPerfilFragment", "✅ Correo electrónico: ${binding.tvEmailUsuario.text}")
        Log.d("VerPerfilFragment", "✅ Fecha Creación: ${binding.tvFechaCreacion.text}")
    }


    private fun formatearFecha(fecha: String): String {
        return try {
            // 📌 Formato de la fecha con zona horaria y microsegundos
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            formatoEntrada.timeZone = TimeZone.getTimeZone("UTC") // Convertir desde UTC

            // 📌 Formato de salida en español
            val formatoSalida = SimpleDateFormat("d 'de' MMMM, yyyy 'a las' h:mm a", Locale("es", "ES"))

            val fechaParseada = formatoEntrada.parse(fecha)

            // 📌 Log para verificar el formateo
            Log.d("VerPerfilFragment", "📌 Fecha parseada correctamente: ${fechaParseada.toString()}")

            formatoSalida.format(fechaParseada ?: Date())
        } catch (e: Exception) {
            Log.e("VerPerfilFragment", "❌ Error formateando fecha: ${e.message}")
            "Fecha no disponible"
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
