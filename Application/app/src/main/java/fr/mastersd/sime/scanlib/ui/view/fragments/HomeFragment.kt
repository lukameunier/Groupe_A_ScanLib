package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentHomeBinding
import fr.mastersd.sime.scanlib.ui.adapter.BookAdapter
import fr.mastersd.sime.scanlib.ui.viewmodel.HomeViewModel

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: BookAdapter
    private var genreListCache: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser l'adapter avec le click listener
        adapter = BookAdapter { selectedBook ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(selectedBook)
            findNavController().navigate(action)
        }

        binding.bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.bookRecyclerView.adapter = adapter

        // Observer les livres
        viewModel.books.observe(viewLifecycleOwner) { bookList ->
            adapter.submitList(bookList)
        }

        // Ajouter un livre
        binding.addBookButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToScanFragment()
            findNavController().navigate(action)
        }

        // Recherche dynamique
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString()
                if (keyword.length >= 2) {
                    viewModel.searchByKeyword(keyword) // ✅ nom correct
                } else {
                    viewModel.loadBooks()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Observer les genres
        viewModel.genres.observe(viewLifecycleOwner) { list ->
            genreListCache = list
        }

        // Bouton genre (menu dynamique avec cache)
        binding.authorButton.setOnClickListener {
            if (genreListCache.isEmpty()) {
                Toast.makeText(requireContext(), "Aucun genre disponible", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val items = genreListCache.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle("Choisir un genre")
                .setItems(items) { _, index ->
                    viewModel.filterByCategory(items[index])
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        // Bouton score minimal
        binding.filterButton.setOnClickListener {
            val scores = arrayOf("1.0", "2.0", "3.0", "4.0", "5.0")
            AlertDialog.Builder(requireContext())
                .setTitle("Filtrer par note minimale")
                .setItems(scores) { _, index ->
                    val selected = scores[index].toDouble()
                    viewModel.filterByScore(selected)
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

//        binding.filterButton.setOnClickListener {
//            val options = arrayOf("Filtrer par score", "Filtrer par année", "Réinitialiser les filtres")
//
//            AlertDialog.Builder(requireContext())
//                .setTitle("Choisir un filtre")
//                .setItems(options) { _, index ->
//                    when (index) {
//                        0 -> showScoreFilterDialog()
//                        1 -> showYearFilterDialog()
//                        2 -> viewModel.loadBooks() // reset
//                    }
//                }
//                .setNegativeButton("Annuler", null)
//                .show()
//        }

        // Charger les genres
        viewModel.loadGenres()
    }

    private fun showScoreFilterDialog() {
        val scores = arrayOf("1.0", "2.0", "3.0", "4.0", "5.0")
        AlertDialog.Builder(requireContext())
            .setTitle("Filtrer par note minimale")
            .setItems(scores) { _, index ->
                val selectedScore = scores[index].toDouble()
                viewModel.filterByScore(selectedScore)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}
