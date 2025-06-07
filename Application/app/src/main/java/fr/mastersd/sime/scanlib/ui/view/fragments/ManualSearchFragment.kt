package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import coil.load
import fr.mastersd.sime.scanlib.data.BookRepository
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.data.GoogleBooksService
import fr.mastersd.sime.scanlib.databinding.FragmentManualSearchBinding
import javax.inject.Inject

@AndroidEntryPoint
class ManualSearchFragment : Fragment() {

    private var _binding: FragmentManualSearchBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var googleBooksService: GoogleBooksService
    @Inject lateinit var bookRepository: BookRepository
    private var lastFoundBook: fr.mastersd.sime.scanlib.data.Book? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonSearch.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val author = binding.editAuthor.text.toString().trim()
            val publisher = binding.editPublisher.text.toString().trim()

            // Petite validation
            if (title.isBlank() && author.isBlank() && publisher.isBlank()) {
                Toast.makeText(requireContext(), "Remplis au moins un champ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lancer la recherche
            searchBook(title, author, publisher)
            binding.validButton.setOnClickListener {
                val bookToAdd = lastFoundBook
                if (bookToAdd == null) {
                    Toast.makeText(requireContext(), "Aucun livre à ajouter", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    bookRepository.insertBook(bookToAdd)
                    val action = ManualSearchFragmentDirections.actionManualSearchFragmentToHomeFragment()
                    findNavController().navigate(action)
                }
            }

        }
    }

    private fun searchBook(title: String, author: String, publisher: String) {
        // Construire la requête "intelligente"
        val query = buildQuery(title, author, publisher)
        lifecycleScope.launch {
            val book = googleBooksService.searchBook(query)
            if (book == null) {
                Toast.makeText(requireContext(), "Aucun livre trouvé", Toast.LENGTH_SHORT).show()
            } else {
                // 1. Remplis tous les champs avec les valeurs de 'book'
                binding.bookTitle.text = book.title

                // Si plusieurs auteurs, on les affiche séparés par une virgule
                binding.authorName.setText(book.authors.joinToString(", "))

                binding.bookGenreEditText.setText(book.categories?.joinToString(", ") ?: "")
                binding.datePublisherEditText.setText(book.publishedDate ?: "")
                binding.editorEditText.setText(book.publisher ?: "")
                binding.pagesNumberEditText.setText(book.pageCount.toString())
                binding.isbnEditText.setText(book.industryIdentifiers?.joinToString(", ") ?: "")
                binding.synopsisContent.text = book.description ?: ""

                // Pour l'image (avec Coil ou Glide, exemple avec Coil) :
                // Assure-toi d'avoir importé coil dans le module UI
                binding.bookCoverImage.load(book.thumbnailUrl)

                // 2. Puis lance l'animation
                lastFoundBook = book
                animateSearchToResult()
            }
        }
    }

    private fun buildQuery(title: String, author: String, publisher: String): String {
        val params = mutableListOf<String>()
        if (title.isNotBlank()) params.add("intitle:$title")
        if (author.isNotBlank()) params.add("inauthor:$author")
        if (publisher.isNotBlank()) params.add("inpublisher:$publisher")
        return params.joinToString("+")
    }

    private fun animateSearchToResult() {
        val searchViews = listOf(
            binding.editTitle,
            binding.editAuthor,
            binding.editPublisher,
            binding.buttonSearch
        )

        val resultViews = listOf(
            binding.bookTitle,
            binding.cardPreviewContainer,
            binding.authorName,
            binding.bookGenre,
            binding.bookGenreEditText,
            binding.datePublisher,
            binding.datePublisherEditText,
            binding.editor,
            binding.editorEditText,
            binding.pagesNumber,
            binding.pagesNumberEditText,
            binding.isbn,
            binding.isbnEditText,
            binding.separator,
            binding.synopsisTitle,
            binding.synopsisContent,
            binding.validButton
        )

        // Animation du formulaire vers le haut
        searchViews.forEachIndexed { i, v ->
            v.animate()
                .translationY(-v.height.toFloat() - 100)
                .alpha(0f)
                .setDuration(350)
                .setStartDelay(i * 30L)
                .withEndAction { v.visibility = View.GONE }
                .start()
        }

        // Apparition des résultats après un délai
        resultViews.forEachIndexed { i, v ->
            v.visibility = View.VISIBLE
            v.translationY = 80f
            v.alpha = 0f
            v.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(420)
                .setStartDelay(250 + i * 25L)
                .start()
        }
    }
}
