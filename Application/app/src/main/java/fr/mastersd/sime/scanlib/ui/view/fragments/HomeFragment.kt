package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentHomeBinding
import fr.mastersd.sime.scanlib.ui.adapter.BookAdapter
import fr.mastersd.sime.scanlib.ui.viewmodel.HomeViewModel

@AndroidEntryPoint
class HomeFragment: Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addBookButton.setOnClickListener{
            val action = HomeFragmentDirections.actionHomeFragmentToScanFragment()
            findNavController().navigate(action)
        }

        val adapter = BookAdapter { selectedBook ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(selectedBook)
            findNavController().navigate(action)
        }
        binding.bookRecyclerView.adapter = adapter

        viewModel.books.observe(viewLifecycleOwner) { bookList ->
            adapter.submitList(bookList)
        }

        binding.bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.bookRecyclerView.adapter = adapter
    }
}