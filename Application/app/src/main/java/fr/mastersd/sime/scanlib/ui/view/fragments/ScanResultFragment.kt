package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentScanResultBinding
import fr.mastersd.sime.scanlib.ui.adapter.ScanResultAdapter

@AndroidEntryPoint
class ScanResultFragment : Fragment() {

    private lateinit var binding: FragmentScanResultBinding
    private lateinit var adapter: ScanResultAdapter
    private val args: ScanResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ScanResultAdapter { selectedBook ->
            val action = ScanResultFragmentDirections
                .actionScanResultFragmentToDetailsFragment(selectedBook)
            findNavController().navigate(action)
        }

        binding.bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.bookRecyclerView.adapter = adapter

        adapter.submitList(args.foundBooks.toList())
    }
}
