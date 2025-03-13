package com.example.driplinesoftapp.ui.perfil

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.driplinesoftapp.databinding.FragmentAcercaDeBinding

class AcercaDeFragment : Fragment(), View.OnTouchListener {

    private var _binding: FragmentAcercaDeBinding? = null
    private val binding get() = _binding!!
    private var startX = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAcercaDeBinding.inflate(inflater, container, false)

        // Configurar la flecha de regreso en la ActionBar
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        setHasOptionsMenu(true)

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
