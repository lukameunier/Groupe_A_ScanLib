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
    private var yearListCache: List<String> = emptyList()
    private var scoreListCache: List<Double> = emptyList()

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

        //observer les années
        viewModel.years.observe(viewLifecycleOwner) { years ->
            yearListCache = years.sortedDescending()
        }

        //observer les scores
        viewModel.scores.observe(viewLifecycleOwner) {
            scoreListCache = it
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
        viewModel.genres.observe(viewLifecycleOwner) { genres ->
            genreListCache = genres
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
            val options = arrayOf("Filtrer par score", "Filtrer par année", "Réinitialiser les filtres")

            AlertDialog.Builder(requireContext())
                .setTitle("Choisir un filtre")
                .setItems(options) { _, index ->
                    when (index) {
                        0 -> showScoreFilterDialog()
                        1 -> showYearFilterDialog()
                        2 -> viewModel.loadBooks()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        // Charger les genres et les années et les socres
        viewModel.loadGenres()
        viewModel.loadYears()
        viewModel.loadScores()
    }

    private fun showScoreFilterDialog() {
        if (scoreListCache.isEmpty()) {
            Toast.makeText(requireContext(), "Aucun score trouvé", Toast.LENGTH_SHORT).show()
            return
        }

        // Construire la liste avec "Inconnu" en dernier
        val scores = scoreListCache.map { it.toString() } + "Score inconnu"

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrer par note")
            .setItems(scores.toTypedArray()) { _, index ->
                if (index == scores.lastIndex) {
                    // "Score inconnu" sélectionné
                    viewModel.filterByNoScore()
                } else {
                    val selectedScore = scores[index].toDouble()
                    viewModel.filterByScore(selectedScore)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }


    private fun showYearFilterDialog() {
        if (yearListCache.isEmpty()) {
            Toast.makeText(requireContext(), "Aucune année trouvée", Toast.LENGTH_SHORT).show()
            return
        }

        val years = yearListCache.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Filtrer par année")
            .setItems(years) { _, index ->
                viewModel.filterByYear(years[index])
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadBooks()
    }
}
