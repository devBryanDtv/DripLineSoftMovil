package com.example.driplinesoftapp.ui.perfil

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.driplinesoftapp.databinding.FragmentVerPerfilBinding
import com.example.driplinesoftapp.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class VerPerfilFragment : Fragment(), View.OnTouchListener {

    private var _binding: FragmentVerPerfilBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EstadisticasViewModel by viewModels()

    private var startX = 0f
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerPerfilBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        setHasOptionsMenu(true) // Habilita opciones en el menú

        sessionManager = SessionManager(requireContext())
        cargarDatosUsuario()

        binding.root.setOnTouchListener(this)

        return binding.root
    }

    private fun cargarDatosUsuario() {
        val usuario = sessionManager.getUser()

        if (usuario == null) {
            Log.e("VerPerfilFragment", "❌ No se encontró ningún usuario autenticado")
            return
        }

        val fechaFormateada = formatearFecha(usuario.fechaCreacion)

        // Aplicar formato HTML para negritas
        binding.tvEmailUsuario.text = Html.fromHtml("<b>Correo electrónico:</b> ${usuario.email}")
        binding.tvFechaCreacion.text = Html.fromHtml("<b>Te uniste desde:</b> $fechaFormateada")
        binding.tvNombreUsuario.text = usuario.nombre

        when (usuario.rol) {
            "cliente_final" -> {
                binding.cardPedidos.visibility = VISIBLE
                binding.cardInformacionNegocio.visibility = GONE

                viewModel.obtenerCantidadPedidosUsuario(usuario.idUsuario)

                viewModel.cantidadPedidos.observe(viewLifecycleOwner) { cantidadPedidos ->
                    binding.tvCantidadPedidos.text = Html.fromHtml("<b>Pedidos realizados:</b> $cantidadPedidos")
                }
            }

            "admin_cliente" -> {
                binding.cardPedidos.visibility = GONE
                binding.cardInformacionNegocio.visibility = VISIBLE

                viewModel.obtenerEstadisticasCliente(usuario.idUsuario)

                viewModel.cantidadSucursales.observe(viewLifecycleOwner) { cantidadSucursales ->
                    binding.tvCantidadSucursales.text = Html.fromHtml("<b>Sucursales:</b> $cantidadSucursales")
                }

                viewModel.cantidadMenus.observe(viewLifecycleOwner) { cantidadMenus ->
                    binding.tvCantidadMenus.text = Html.fromHtml("<b>Menús:</b> $cantidadMenus")
                }

                viewModel.cantidadProductos.observe(viewLifecycleOwner) { cantidadProductos ->
                    binding.tvCantidadProductos.text = Html.fromHtml("<b>Productos:</b> $cantidadProductos")
                }
            }

            else -> {
                binding.cardPedidos.visibility = GONE
                binding.cardInformacionNegocio.visibility = GONE
                Log.e("VerPerfilFragment", "❌ Rol no identificado: ${usuario.rol}")
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.tvCantidadPedidos.text = Html.fromHtml("<b>Error:</b> $error")
            binding.tvCantidadSucursales.text = Html.fromHtml("<b>Error:</b> $error")
            binding.tvCantidadMenus.text = Html.fromHtml("<b>Error:</b> $error")
            binding.tvCantidadProductos.text = Html.fromHtml("<b>Error:</b> $error")
        }

        Log.d("VerPerfilFragment", "✅ Nombre: ${binding.tvNombreUsuario.text}")
        Log.d("VerPerfilFragment", "✅ Correo electrónico: ${binding.tvEmailUsuario.text}")
        Log.d("VerPerfilFragment", "✅ Fecha Creación: ${binding.tvFechaCreacion.text}")
    }

    private fun formatearFecha(fecha: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            formatoEntrada.timeZone = TimeZone.getTimeZone("UTC")

            val formatoSalida = SimpleDateFormat("d 'de' MMMM, yyyy 'a las' h:mm a", Locale("es", "ES"))

            val fechaParseada = formatoEntrada.parse(fecha)
            formatoSalida.format(fechaParseada ?: Date())
        } catch (e: Exception) {
            Log.e("VerPerfilFragment", "❌ Error formateando fecha: ${e.message}")
            "Fecha no disponible"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
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
