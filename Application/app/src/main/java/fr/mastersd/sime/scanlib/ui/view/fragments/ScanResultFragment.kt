package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import fr.mastersd.sime.scanlib.R
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.databinding.FragmentScanResultBinding
import fr.mastersd.sime.scanlib.ui.adapter.ScanResultAdapter
import fr.mastersd.sime.scanlib.ui.viewmodel.BookViewModel

@AndroidEntryPoint
class ScanResultFragment : Fragment() {

    private lateinit var binding: FragmentScanResultBinding
    private lateinit var adapter: ScanResultAdapter
    private val args: ScanResultFragmentArgs by navArgs()
    private val bookViewModel: BookViewModel by viewModels()

    private val selectedBooks = mutableSetOf<Book>()

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        )

        adapter = ScanResultAdapter { selectedBook ->
            val action = ScanResultFragmentDirections
                .actionScanResultFragmentToDetailsFragment(selectedBook)
            selectedBooks.clear()
            selectedBooks.addAll(adapter.getSelectedBooks())
            findNavController().navigate(action)
        }.apply {
            setSelectedBooks(selectedBooks)
            onSelectionChanged = { newSelection ->
                selectedBooks.clear()
                selectedBooks.addAll(newSelection)
            }
        }

        binding.bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.bookRecyclerView.adapter = adapter

        adapter.submitList(args.foundBooks.toList())
        adapter.setSelectedBooks(selectedBooks)

        binding.saveButton.setOnClickListener {
            val action = ScanResultFragmentDirections
                .actionScanResultFragmentToHomeFragment()
            val selected = adapter.getSelectedBooks()
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), "Aucun livre sélectionné", Toast.LENGTH_SHORT).show()
            } else {
                selected.forEach { book ->
                    bookViewModel.insertBook(book)
                }
                Toast.makeText(requireContext(), "${selected.size} livre(s) enregistré(s)", Toast.LENGTH_SHORT).show()
            }
            findNavController().navigate(action)
        }
    }
}
