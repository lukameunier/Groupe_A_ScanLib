package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import fr.mastersd.sime.scanlib.R
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.data.FilterState
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

        // Initialiser l'adapter avec le click listener
        adapter = BookAdapter { selectedBook ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(selectedBook)
            findNavController().navigate(action)
        }


        adapter.onSelectionModeChanged = { updateActionButtons() }

        binding.deleteSelectedButton.setOnClickListener {
            val toDelete = adapter.getSelectedBooks()
            if (toDelete.isEmpty()) {
                Toast.makeText(requireContext(), "Aucun livre sélectionné", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la sélection ?")
                .setMessage("Voulez-vous vraiment supprimer ces livres ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    viewModel.deleteBooks(toDelete)
                    adapter.exitSelectionMode()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        binding.rootLayout.setOnClickListener {
            if (adapter.selectionMode) {
                adapter.exitSelectionMode()
                updateActionButtons()
            }
        }

        binding.bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.bookRecyclerView.adapter = adapter

        observeViewModel()

        // Ajouter un livre
        binding.addBookButton.setOnClickListener {
            val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_add_book, null)
            val dialog = BottomSheetDialog(requireContext())
            dialog.setContentView(sheetView)
            dialog.show()

            sheetView.findViewById<MaterialButton>(R.id.option_scan).setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToScanFragment()
                findNavController().navigate(action)
                dialog.dismiss()
            }
            sheetView.findViewById<MaterialButton>(R.id.option_manual).setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToManualSearchFragment()
                findNavController().navigate(action)
                dialog.dismiss()
            }
            sheetView.findViewById<MaterialButton>(R.id.option_import).setOnClickListener {
                Toast.makeText(requireContext(), "Import depuis un fichier à implémenter", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }


        // Recherche dynamique
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

        // Bouton genre
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
                    updateFilterDisplay()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        // Bouton filtre combiné
        binding.filterButton.setOnClickListener {
            val options = arrayOf("Filtrer par score", "Filtrer par année", "Réinitialiser les filtres")

            AlertDialog.Builder(requireContext())
                .setTitle("Choisir un filtre")
                .setItems(options) { _, index ->
                    when (index) {
                        0 -> showScoreFilterDialog()
                        1 -> showYearFilterDialog()
                        2 -> viewModel.resetFilters()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        //reitialiser les filtres en cliquant sur le text
        binding.activeFiltersText.setOnClickListener {
            viewModel.resetFilters()
            Toast.makeText(requireContext(), "Filtres réinitialisés", Toast.LENGTH_SHORT).show()
        }

        // Bouton rénitialiser les filtres
        binding.resetFiltersButton.setOnClickListener {
            viewModel.resetFilters()
            Toast.makeText(requireContext(), "Filtres réinitialisés", Toast.LENGTH_SHORT).show()
        }


        // Charger les filtres
        viewModel.loadGenres()
        viewModel.loadYears()
        viewModel.loadScores()
    }

    private fun observeViewModel() {
        viewModel.books.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.genres.observe(viewLifecycleOwner) { genreListCache = it }
        viewModel.years.observe(viewLifecycleOwner) { yearListCache = it.sortedDescending() }
        viewModel.scores.observe(viewLifecycleOwner) { scoreListCache = it }
        viewModel.filters.observe(viewLifecycleOwner) { newFilter -> //observer les filtres
            currentFilter = newFilter
            viewModel.applyCombinedFilters(newFilter)
            updateFilterDisplay()
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
                currentFilter = if (index == scores.lastIndex) {
                    currentFilter.copy(minScore = null, scoreUnknown = true)
                } else {
                    currentFilter.copy(minScore = scores[index].toDouble(), scoreUnknown = false)
                }
                viewModel.updateFilter(currentFilter)
                updateFilterDisplay()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun updateActionButtons() {
        if (adapter.selectionMode) {
            binding.addBookButton.visibility = View.GONE
            binding.deleteSelectedButton.visibility = View.VISIBLE
        } else {
            binding.addBookButton.visibility = View.VISIBLE
            binding.deleteSelectedButton.visibility = View.GONE
        }
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
//                currentFilter = currentFilter.copy(year = years[index], yearUnknown = false)
                currentFilter = currentFilter.copy(year = years[index])
                viewModel.updateFilter(currentFilter)
                updateFilterDisplay()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun updateFilterDisplay() {
        val filters = mutableListOf<String>()

        currentFilter.category?.let {
            filters.add(getString(R.string.filter_genre, it))
        }

        currentFilter.year?.let {
            filters.add(getString(R.string.filter_year, it))
        }

        currentFilter.minScore?.let {
            filters.add(getString(R.string.filter_score_min, it))
        }

        if (currentFilter.scoreUnknown) {
            filters.add(getString(R.string.filter_score_unknown))
        }

        if (filters.isEmpty()) {
            // Aucun filtre actif → cacher les vues
            binding.activeFiltersText.visibility = View.GONE
            binding.resetFiltersButton.visibility = View.GONE
            binding.activeFiltersText.text = getString(R.string.no_filters)
        } else {
            // Un ou plusieurs filtres actifs → afficher les vues + texte
            val displayText = getString(R.string.filters_label) + "\n" + filters.joinToString("\n")
            binding.activeFiltersText.text = displayText
            binding.activeFiltersText.visibility = View.VISIBLE
            binding.resetFiltersButton.visibility = View.VISIBLE
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadBooks()
    }
}
