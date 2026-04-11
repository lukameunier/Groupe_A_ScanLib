package fr.mastersd.sime.scanlib.ui.view.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import android.widget.Toast
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import fr.mastersd.sime.scanlib.R
import android.widget.EditText
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentDetailsBinding
import androidx.core.view.isEmpty
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import fr.mastersd.sime.scanlib.ui.viewmodel.DetailsViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.FavoriteGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import fr.mastersd.sime.scanlib.ui.adapter.BookGroupSelectAdapter
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private lateinit var binding: FragmentDetailsBinding
    private val detailsViewModel: DetailsViewModel by viewModels()
    private lateinit var originalBook: Book
    private lateinit var editTexts: List<EditText>
    private var isEditMode = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = DetailsFragmentArgs.fromBundle(requireArguments())
        val book = args.book
        originalBook = book

        binding.bookTitle.text = book.title
        binding.authorName.setText(book.authors.joinToString())
        binding.bookGenreEditText.setText(book.categories?.joinToString() ?: "")
        binding.datePublisherEditText.setText(book.publishedDate ?: "")
        binding.editorEditText.setText(book.publisher ?: "")
        binding.pagesNumberEditText.setText(book.pageCount.toString())
        binding.isbnEditText.setText(book.industryIdentifiers?.joinToString() ?: "")
        binding.synopsisContent.text = book.description ?: ""

        // Liste des champs éditables surveillés
        editTexts = listOf(
            binding.authorName,
            binding.bookGenreEditText,
            binding.datePublisherEditText,
            binding.editorEditText,
            binding.pagesNumberEditText,
            binding.isbnEditText,
        )

        // Ajoute un TextWatcher pour détecter les modifications
        editTexts.forEach { editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    checkIfModified()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        // Bouton enregistrer : enregistre les modifs et réinitialise le bouton
        binding.saveButton.setOnClickListener {
            val newBook = originalBook.copy(
                authors = binding.authorName.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                categories = binding.bookGenreEditText.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                publishedDate = binding.datePublisherEditText.text.toString().ifBlank { null },
                publisher = binding.editorEditText.text.toString().ifBlank { null },
                pageCount = binding.pagesNumberEditText.text.toString().toIntOrNull() ?: 0,
                industryIdentifiers = binding.isbnEditText.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                description = binding.synopsisContent.text.toString().ifBlank { null }
            )
            detailsViewModel.updateBook(newBook)
            originalBook = newBook // Réinitialise la référence pour la comparaison
            binding.saveButton.visibility = View.GONE
            binding.addToGroupsButton.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Modifications enregistrées", Toast.LENGTH_SHORT).show()
        }

        // Affiche la couverture du livre
        val imageView = android.widget.ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        }
        imageView.load(book.thumbnailUrl)
        if (binding.cardPreviewContainer.isEmpty()) {
            binding.cardPreviewContainer.addView(imageView)
        }

        binding.root.setOnTouchListener { v, _ ->
            if (isEditMode) {
                setEditMode(false)
                editTexts.forEach { it.clearFocus() }
            }
            v.performClick()
            false
        }

        // Activation du mode édition (bordure et padding) au focus
        editTexts.forEach { editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setEditMode(true)
                }
            }
            // Gère la validation du clavier (IME action)
            editText.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setEditMode(false)
                    editText.clearFocus()
                    // Ferme le clavier
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(editText.windowToken, 0)
                    true
                } else {
                    false
                }
            }
        }

        // Masque le bouton au départ, il n'apparaît que si modification
        binding.saveButton.visibility = View.GONE

//================================================================================
        // === GESTION DES GROUPES ===

        binding.addToGroupsButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                detailsViewModel.loadGroups()
                detailsViewModel.loadBookGroupIds(originalBook.id)
                val loadedGroups = detailsViewModel.groups.value
                val groupIds = detailsViewModel.bookGroupIds.value
                when {
                    loadedGroups.isEmpty() -> {
                        showCreateGroupDialog { group ->
                            detailsViewModel.addBookToGroup(originalBook.id, group.id)
                            detailsViewModel.loadGroups()
                            detailsViewModel.loadBookGroupIds(originalBook.id)
                            showGroupSelectionSheet(
                                detailsViewModel.groups.value,
                                detailsViewModel.bookGroupIds.value
                            )
                        }
                    }
                    else -> showGroupSelectionSheet(loadedGroups, groupIds)
                }
            }
        }
    }

    private fun showCreateGroupDialog(onGroupCreated: (FavoriteGroup) -> Unit) {
        val editText = EditText(requireContext()).apply { hint = "Nom du groupe" }
        AlertDialog.Builder(requireContext())
            .setTitle("Créer un groupe")
            .setView(editText)
            .setPositiveButton("Créer") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    detailsViewModel.checkOrCreateGroup(name) { group ->
                        onGroupCreated(group)
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showGroupSelectionSheet(groups: List<FavoriteGroup>, bookGroupIds: List<Long>) {
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_manage_groups, null)
        val recyclerView = sheetView.findViewById<RecyclerView>(R.id.groupsRecyclerView)
        val createGroupBtn = sheetView.findViewById<MaterialButton>(R.id.createGroupButton)
        val titleView = sheetView.findViewById<TextView>(R.id.groupSheetTitle)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(sheetView)

        titleView?.text = getString(R.string.ajouter_un_groupe)

        val adapter = BookGroupSelectAdapter(
            groups,
            bookGroupIds.toMutableSet(),
            onGroupCheckedChanged = { group, isChecked ->
                if (isChecked) detailsViewModel.addBookToGroup(originalBook.id, group.id)
                else detailsViewModel.removeBookFromGroup(originalBook.id, group.id)
                detailsViewModel.loadBookGroupIds(originalBook.id)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe les ids de groupes pour mettre à jour la sélection
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detailsViewModel.bookGroupIds.collect { newIds ->
                    adapter.updateGroups(groups, newIds.toSet())
                }
            }
        }

        createGroupBtn.setOnClickListener {
            showCreateGroupDialog { group ->
                val displayedGroups = adapter.groupsList.toMutableList()
                if (displayedGroups.none { it.id == group.id }) {
                    displayedGroups.add(group)
                    adapter.updateGroups(displayedGroups, adapter.selectedGroupIds + group.id)
                }
                detailsViewModel.addBookToGroup(originalBook.id, group.id)
                detailsViewModel.loadGroups()
                detailsViewModel.loadBookGroupIds(originalBook.id)
            }
        }

        dialog.show()
    }

//=======================================================================

    private fun setEditMode(enabled: Boolean) {
        if (isEditMode == enabled) return
        isEditMode = enabled
        val background = if (enabled)
            ContextCompat.getDrawable(requireContext(), R.drawable.edittext_border)
        else
            ContextCompat.getDrawable(requireContext(), android.R.color.transparent)

        val targetPadding = if (enabled) 10 else 0
        editTexts.forEach {
            it.background = background
            animateEditTextPadding(it, targetPadding)
        }
    }

    private fun animateEditTextPadding(editText: EditText, toPadding: Int, duration: Long = 250) {
        val fromPadding = editText.paddingLeft
        val animator = ValueAnimator.ofInt(fromPadding, toPadding)
        animator.duration = duration
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            editText.setPadding(value, value, value, value)
        }
        animator.start()
    }

    // Vérifie si un champ a été modifié, et affiche le bouton le cas échéant
    private fun checkIfModified() {
        val currentBook = originalBook.copy(
            authors = binding.authorName.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
            categories = binding.bookGenreEditText.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
            publishedDate = binding.datePublisherEditText.text.toString().ifBlank { null },
            publisher = binding.editorEditText.text.toString().ifBlank { null },
            pageCount = binding.pagesNumberEditText.text.toString().toIntOrNull() ?: 0,
            industryIdentifiers = binding.isbnEditText.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
            description = binding.synopsisContent.text.toString().ifBlank { null }
        )

        val modified = currentBook != originalBook
        binding.saveButton.visibility = if (modified) View.VISIBLE else View.GONE
        binding.addToGroupsButton.visibility = if (modified) View.GONE else View.VISIBLE
    }

}
