package fr.mastersd.sime.scanlib.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentEditBinding

@AndroidEntryPoint
class EditFragment: Fragment() {

    private lateinit var binding: FragmentEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.eraseButton.setOnClickListener {
            val action = EditFragmentDirections.actionEditFragmentToDetailsFragment()
            findNavController().navigate(action)
        }

        binding.validButton.setOnClickListener{
            val action = EditFragmentDirections.actionEditFragmentToDetailsFragment()
            findNavController().navigate(action)
        }
    }
}