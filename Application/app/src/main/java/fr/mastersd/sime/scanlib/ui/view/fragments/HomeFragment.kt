package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.R
import fr.mastersd.sime.scanlib.data.FavoriteGroup
import fr.mastersd.sime.scanlib.data.FilterState
import fr.mastersd.sime.scanlib.databinding.FragmentHomeBinding
import fr.mastersd.sime.scanlib.ui.adapter.BookAdapter
import fr.mastersd.sime.scanlib.ui.adapter.GroupManageAdapter
import fr.mastersd.sime.scanlib.ui.viewmodel.HomeViewModel

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var adapter: BookAdapter
    private var genreListCache: List<String> = emptyList()
    private var yearListCache: List<String> = emptyList()
    private var scoreListCache: List<Double> = emptyList()
    private var currentFilter = FilterState()
    private var currentGroups: List<FavoriteGroup> = emptyList()
    private var groupManageAdapter: GroupManageAdapter? = null
    private var groupDialog: BottomSheetDialog? = null


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
        setupAdapter()
        setupObservers()
        setupListeners()
        homeViewModel.loadGenres()
        homeViewModel.loadYears()
        homeViewModel.loadScores()
        homeViewModel.loadGroups()
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
        homeViewModel.currentGroupName.observe(viewLifecycleOwner) { groupName ->
            binding.groupButton.text = groupName
        }
        homeViewModel.groups.observe(viewLifecycleOwner) { groups ->
            currentGroups = groups
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

        // Bouton groupes
        groupButton.setOnClickListener { showManageGroupsDialog() }
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
            yearListCache.forEachIndexed { index, year ->
                menu.add(0, index, index, year)
            }
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

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    // ------------------- GROUPES ---------------------

    private fun showManageGroupsDialog() {
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_manage_groups, null)
        val recyclerView = sheetView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.groupsRecyclerView)
        val createGroupBtn = sheetView.findViewById<MaterialButton>(R.id.createGroupButton)
        groupDialog = BottomSheetDialog(requireContext())
        groupDialog?.setContentView(sheetView)

        // Adapter initialisé avec une liste vide
        groupManageAdapter = GroupManageAdapter(
            groups = emptyList(),
            onSelect = { group ->
                if (group.id == -1L) homeViewModel.loadBooks()
                else homeViewModel.filterByGroup(group.id)
                groupDialog?.dismiss()
            },
            onEdit = { group -> if (group.id != -1L) showRenameGroupDialog(group) },
            onDelete = { group ->
                if (group.id != -1L) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Supprimer le groupe")
                        .setMessage("Confirmer la suppression du groupe '${group.name}' ?")
                        .setPositiveButton("Supprimer") { _, _ -> homeViewModel.deleteGroup(group.id) }
                        .setNegativeButton("Annuler", null)
                        .show()
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = groupManageAdapter

        createGroupBtn.setOnClickListener {
            showCreateGroupDialog { group ->
                homeViewModel.addGroupLocally(group)

                // Mets à jour l'adapter immédiatement
                val allGroups = listOf(FavoriteGroup(-1, "Tous")) + (homeViewModel.groups.value ?: emptyList())
                groupManageAdapter?.updateGroups(allGroups)

                // Recharge depuis la base après coup pour l'intégrité (Room)
                homeViewModel.loadGroups()
            }
        }

        // Observe les groupes ici, pour mettre à jour la liste dynamiquement
        homeViewModel.groups.observe(viewLifecycleOwner) { groups ->
            val allGroups = listOf(FavoriteGroup(-1, "Tous")) + groups
            groupManageAdapter?.updateGroups(allGroups)
        }

        groupDialog?.show()
        homeViewModel.loadGroups()
    }

    private fun showCreateGroupDialog(onGroupCreated: (FavoriteGroup) -> Unit = {}) {
        val editText = EditText(requireContext()).apply { hint = "Nom du groupe" }
        AlertDialog.Builder(requireContext())
            .setTitle("Créer un groupe")
            .setView(editText)
            .setPositiveButton("Créer") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    homeViewModel.checkOrCreateGroup(name) { group ->
                        onGroupCreated(group)
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showRenameGroupDialog(group: FavoriteGroup) {
        val editText = EditText(requireContext()).apply {
            setText(group.name)
            setSelection(group.name.length)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Renommer le groupe")
            .setView(editText)
            .setPositiveButton("Valider") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty() && newName != group.name)
                    homeViewModel.renameGroup(group.id, newName)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}
