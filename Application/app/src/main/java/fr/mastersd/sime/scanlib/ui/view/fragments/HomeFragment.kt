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
                Toast.makeText(requireContext(), "Ajout manuel à implémenter", Toast.LENGTH_SHORT).show()
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
