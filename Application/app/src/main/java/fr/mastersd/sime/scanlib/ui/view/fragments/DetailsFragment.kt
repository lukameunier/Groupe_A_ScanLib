package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentDetailsBinding

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

        binding.editButton.setOnClickListener{
            val action = DetailsFragmentDirections.actionDetailsFragmentToEditFragment(book)
            findNavController().navigate(action)
        }

        binding.returnButton.setOnClickListener{
            val action = DetailsFragmentDirections.actionDetailsFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        binding.bookTitle.text = book.title
        binding.authorName.text = book.authors.toString()
        binding.bookGenre.text
        binding.datePublisher.text
        binding.editor.text
        binding.pagesNumber.text
        binding.isbn.text
        binding.synopsisContent.text = book.description
    }
}