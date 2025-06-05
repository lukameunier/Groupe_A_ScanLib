package fr.mastersd.sime.scanlib.ui.view.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import android.content.Context
import fr.mastersd.sime.scanlib.R
import android.widget.EditText
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentDetailsBinding
import androidx.core.view.isEmpty

/**
 * Fragment d’affichage détaillé d’un livre, après la détection et la synchronisation réussie
 *
 * Récupère l’objet [Book] et remplit l’interface avec ses champs
 *
 * @see model.Book pour le modèle affiché
 * @see ScanFragment pour le fragment qui déclenche cette vue de détails
 */
@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private lateinit var binding: FragmentDetailsBinding
    private lateinit var editTexts: List<EditText>
    private var isEditMode = false

    /**
     * Gonfle le layout XML avec ViewBinding
     *
     * @param inflater Inflater standard de fragments
     * @param container Vue parente
     * @param savedInstanceState État sauvegardé si recréation
     * @return Vue racine de l’interface
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsBinding.inflate(inflater)
        return binding.root
    }

    /**
     * Récupère les données du livre via Safe Args et remplit tous les champs de l’interface
     *
     * @param view Vue initialisée
     * @param savedInstanceState État restauré si recréation
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = DetailsFragmentArgs.fromBundle(requireArguments())
        val book = args.book

        binding.bookTitle.text = book.title
        binding.authorName.setText(book.authors.joinToString())
        binding.bookGenreEditText.setText(book.categories?.joinToString() ?: "À préciser...")
        binding.datePublisherEditText.setText(book.publishedDate ?: "À préciser...")
        binding.editorEditText.setText(book.publisher ?: "À préciser...")
        binding.pagesNumberEditText.setText(book.pageCount.toString())
        binding.isbnEditText.setText(book.industryIdentifiers?.joinToString() ?: "À préciser...")
        binding.synopsisContent.text = book.description ?: "À préciser..."

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

        // Récupère tous les EditText à gérer
        editTexts = listOf(
            binding.authorName,
            binding.bookGenreEditText,
            binding.datePublisherEditText,
            binding.editorEditText,
            binding.pagesNumberEditText,
            binding.isbnEditText
        )

        // Ajoute un listener de focus à chacun
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
    }

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

    fun animateEditTextPadding(editText: EditText, toPadding: Int, duration: Long = 250) {
        val fromPadding = editText.paddingLeft
        val animator = ValueAnimator.ofInt(fromPadding, toPadding)
        animator.duration = duration
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            editText.setPadding(value, value, value, value)
        }
        animator.start()
    }
}