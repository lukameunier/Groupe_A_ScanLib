package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentDetailsBinding
import androidx.core.view.isEmpty

@AndroidEntryPoint
class DetailsFragment: Fragment() {

    private lateinit var binding: FragmentDetailsBinding

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
    }
}
