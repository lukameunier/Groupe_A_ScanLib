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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.data.FilterState
import fr.mastersd.sime.scanlib.databinding.FragmentHomeBinding
import fr.mastersd.sime.scanlib.ui.adapter.BookAdapter
import fr.mastersd.sime.scanlib.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: BookAdapter

    private var genreListCache: List<String> = emptyList()
    private var yearListCache: List<String> = emptyList()
    private var scoreListCache: List<Double> = emptyList()

    private var currentFilter = FilterState()

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

        adapter = BookAdapter { selectedBook ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(selectedBook)
            findNavController().navigate(action)
        }

        binding.bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.bookRecyclerView.adapter = adapter

        // 🔁 Observer les flux de données avec lifecycleScope
        collectStateFlows()

        // 📚 Ajouter un livre
        binding.addBookButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToScanFragment()
            findNavController().navigate(action)
        }

        // 🔍 Recherche dynamique
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString()
                if (keyword.length >= 2) {
                    viewModel.searchByKeyword(keyword)
                } else {
                    viewModel.loadBooks()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 🎯 Bouton genre
        binding.authorButton.setOnClickListener {
            if (genreListCache.isEmpty()) {
                Toast.makeText(requireContext(), "Aucun genre disponible", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val items = genreListCache.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle("Choisir un genre")
                .setItems(items) { _, index ->
                    currentFilter = currentFilter.copy(category = items[index])
                    viewModel.updateFilter(currentFilter)
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        // 🎚 Bouton filtre combiné
        binding.filterButton.setOnClickListener {
            val options = arrayOf("Filtrer par score", "Filtrer par année", "Réinitialiser les filtres")

            AlertDialog.Builder(requireContext())
                .setTitle("Choisir un filtre")
                .setItems(options) { _, index ->
                    when (index) {
                        0 -> showScoreFilterDialog()
                        1 -> showYearFilterDialog()
                        2 -> {
                            currentFilter = FilterState()
                            viewModel.updateFilter(currentFilter)
                        }
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    private fun collectStateFlows() {
        lifecycleScope.launch {
            viewModel.books.collectLatest { adapter.submitList(it) }
        }
        lifecycleScope.launch {
            viewModel.genres.collectLatest { genreListCache = it }
        }
        lifecycleScope.launch {
            viewModel.years.collectLatest { yearListCache = it.sortedDescending() }
        }
        lifecycleScope.launch {
            viewModel.scores.collectLatest { scoreListCache = it }
        }
    }

    private fun showScoreFilterDialog() {
        if (scoreListCache.isEmpty()) {
            Toast.makeText(requireContext(), "Aucun score trouvé", Toast.LENGTH_SHORT).show()
            return
        }

        val scores = scoreListCache.map { it.toString() } + "Score inconnu"

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrer par note")
            .setItems(scores.toTypedArray()) { _, index ->
                if (index == scores.lastIndex) {
                    currentFilter = currentFilter.copy(minScore = null, scoreUnknown = true)
                } else {
                    currentFilter = currentFilter.copy(minScore = scores[index].toDouble(), scoreUnknown = false)
                }
                viewModel.updateFilter(currentFilter)
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
                currentFilter = currentFilter.copy(year = years[index], yearUnknown = false)
                viewModel.updateFilter(currentFilter)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}
