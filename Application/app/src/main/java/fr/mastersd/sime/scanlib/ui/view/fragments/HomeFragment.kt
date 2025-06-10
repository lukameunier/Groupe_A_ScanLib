package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import fr.mastersd.sime.scanlib.ui.viewmodel.ScanViewModel
import kotlin.getValue

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private val scanViewModel: ScanViewModel by viewModels()
    private lateinit var adapter: BookAdapter

    private var genreListCache = emptyList<String>()
    private var yearListCache = emptyList<String>()
    private var scoreListCache = emptyList<Double>()
    private var currentFilter = FilterState()

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val tempFile = kotlin.io.path.createTempFile(suffix = ".jpg").toFile()
            requireContext().contentResolver.openInputStream(it)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            scanViewModel.processImageAndFetchBooks(tempFile.absolutePath)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupObservers()
        setupListeners()
        homeViewModel.loadGenres()
        homeViewModel.loadYears()
        homeViewModel.loadScores()
    }

    private fun setupAdapter() = with(binding) {
        adapter = BookAdapter { selectedBook ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(selectedBook)
            findNavController().navigate(action)
        }
        adapter.onSelectionModeChanged = { updateActionButtons() }
        bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        bookRecyclerView.adapter = adapter
    }

    private fun setupObservers() {
        homeViewModel.books.observe(viewLifecycleOwner) { adapter.submitList(it) }
        homeViewModel.genres.observe(viewLifecycleOwner) { genreListCache = it }
        homeViewModel.years.observe(viewLifecycleOwner) { yearListCache = it.sortedDescending() }
        homeViewModel.scores.observe(viewLifecycleOwner) { scoreListCache = it }
        homeViewModel.filters.observe(viewLifecycleOwner) { newFilter ->
            currentFilter = newFilter
            homeViewModel.applyCombinedFilters(newFilter)
            updateFilterDisplay()
        }
        scanViewModel.foundBooks.observe(viewLifecycleOwner) { books ->
            if (books.isNotEmpty()) {
                val action = HomeFragmentDirections.actionHomeFragmentToScanResultFragment(books.toTypedArray())
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Aucun livre trouvé pour cette image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() = with(binding) {
        // Suppression sélection
        deleteSelectedButton.setOnClickListener {
            val toDelete = adapter.getSelectedBooks()
            if (toDelete.isEmpty()) {
                toast("Aucun livre sélectionné")
            } else {
                showDeleteDialog(toDelete)
            }
        }

        // Exit mode sélection par clic sur le fond
        rootLayout.setOnClickListener {
            if (adapter.selectionMode) {
                adapter.exitSelectionMode()
                updateActionButtons()
            }
        }

        // Ajout livre (BottomSheet)
        addBookButton.setOnClickListener { showAddBookSheet() }

        // Recherche dynamique
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString()
                if (keyword.length >= 2) homeViewModel.searchByKeyword(keyword) else homeViewModel.loadBooks()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Bouton genre
        authorButton.setOnClickListener { showGenrePopup(it) }

        // Bouton filtre combiné
        filterButton.setOnClickListener { showFilterPopup(it) }

        // Réinitialiser les filtres
        activeFiltersText.setOnClickListener {
            homeViewModel.resetFilters()
            toast("Filtres réinitialisés")
        }
    }

    private fun showAddBookSheet() {
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_add_book, null)
        val dialog = BottomSheetDialog(requireContext()).apply { setContentView(sheetView) }
        dialog.show()

        sheetView.findViewById<MaterialButton>(R.id.option_scan)?.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToScanFragment())
            dialog.dismiss()
        }
        sheetView.findViewById<MaterialButton>(R.id.option_manual)?.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToManualSearchFragment())
            dialog.dismiss()
        }
        sheetView.findViewById<MaterialButton>(R.id.option_import)?.setOnClickListener {
            dialog.dismiss()
            toast("À implémenter...")
            /*
            galleryLauncher.launch("image/*")
            */
             */
        }
    }

    private fun showDeleteDialog(toDelete: List<fr.mastersd.sime.scanlib.data.Book>) {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer la sélection ?")
            .setMessage("Voulez-vous vraiment supprimer ces livres ?")
            .setPositiveButton("Supprimer") { _, _ ->
                homeViewModel.deleteBooks(toDelete)
                adapter.exitSelectionMode()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showGenrePopup(anchor: View) {
        if (genreListCache.isEmpty()) {
            toast("Aucun genre disponible")
            return
        }
        PopupMenu(requireContext(), anchor).apply {
            genreListCache.forEachIndexed { index, genre -> menu.add(0, index, index, genre) }
            setOnMenuItemClickListener { item ->
                currentFilter = currentFilter.copy(category = genreListCache[item.itemId])
                homeViewModel.updateFilter(currentFilter)
                updateFilterDisplay()
                true
            }
            show()
        }
    }

    private fun showFilterPopup(anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.menu_filter_popup, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.filter_score -> { showScorePopup(anchor); true }
                    R.id.filter_year -> { showYearFilterPopup(anchor); true }
                    R.id.filter_recent -> {
                        currentFilter = currentFilter.copy(sortByDateAjout = true)
                        homeViewModel.updateFilter(currentFilter)
                        updateFilterDisplay()
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showScorePopup(anchor: View) {
        if (scoreListCache.isEmpty()) {
            toast("Aucun score trouvé")
            return
        }
        PopupMenu(requireContext(), anchor).apply {
            scoreListCache.forEachIndexed { index, score ->
                menu.add(0, index, index, "≥ %.1f".format(score))
            }
            val unknownId = scoreListCache.size
            menu.add(0, unknownId, unknownId, "Score inconnu")
            setOnMenuItemClickListener { item ->
                currentFilter = if (item.itemId == unknownId)
                    currentFilter.copy(minScore = null, scoreUnknown = true)
                else
                    currentFilter.copy(minScore = scoreListCache[item.itemId], scoreUnknown = false)
                homeViewModel.updateFilter(currentFilter)
                updateFilterDisplay()
                true
            }
            show()
        }
    }

    private fun showYearFilterPopup(anchor: View) {
        if (yearListCache.isEmpty()) {
            toast("Aucune année trouvée")
            return
        }
        PopupMenu(requireContext(), anchor).apply {
            yearListCache.forEachIndexed { index, year -> menu.add(0, index, index, year) }
            setOnMenuItemClickListener { item ->
                currentFilter = currentFilter.copy(year = yearListCache[item.itemId])
                homeViewModel.updateFilter(currentFilter)
                updateFilterDisplay()
                true
            }
            show()
        }
    }

    private fun updateActionButtons() = with(binding) {
        addBookButton.visibility = if (adapter.selectionMode) View.GONE else View.VISIBLE
        deleteSelectedButton.visibility = if (adapter.selectionMode) View.VISIBLE else View.GONE
    }

    private fun updateFilterDisplay() = with(binding) {
        val filters = mutableListOf<String>()
        currentFilter.category?.let { filters.add(getString(R.string.filter_genre, it)) }
        currentFilter.year?.let { filters.add(getString(R.string.filter_year, it)) }
        currentFilter.minScore?.let { filters.add(getString(R.string.filter_score_min, it)) }
        if (currentFilter.scoreUnknown) filters.add(getString(R.string.filter_score_unknown))

        if (filters.isEmpty()) {
            activeFiltersText.visibility = View.GONE
            resetFiltersButton.visibility = View.GONE
            activeFiltersText.text = getString(R.string.no_filters)
        } else {
            activeFiltersText.text = getString(R.string.filters_label) + "\n" + filters.joinToString("\n")
            activeFiltersText.visibility = View.VISIBLE
            resetFiltersButton.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.loadBooks()
    }

    // Petite extension locale pour afficher rapidement un toast
    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}

