package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentHomeBinding
import fr.mastersd.sime.scanlib.domain.model.Book
import fr.mastersd.sime.scanlib.ui.adapter.BookAdapter

@AndroidEntryPoint
class HomeFragment: Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addBookButton.setOnClickListener{
            val action = HomeFragmentDirections.actionHomeFragmentToScanFragment()
            findNavController().navigate(action)
        }

        val dummyBook = Book(
            id = "1",
            title = "Les Misérables",
            subtitle = "Édition intégrale",
            authors = listOf("Victor Hugo"),
            publisher = "Gallimard",
            publishedDate = "1862",
            description = "Un roman historique et social majeur du XIXe siècle.",
            pageCount = 1463,
            thumbnailUrl = "https://www.parismuseescollections.paris.fr/sites/default/files/styles/pm_diaporama/public/atoms/images/MVH/aze_mvhperec3069.05_001.jpg?itok=uKikxBqb",
            previewLink = null,
            infoLink = null,
            buyLink = null
        )

        val dummyBook1 = Book(
            id = "1",
            title = "Les Misérables",
            subtitle = "Édition intégrale",
            authors = listOf("Victor Hugo"),
            publisher = "Gallimard",
            publishedDate = "1862",
            description = "Un roman historique et social majeur du XIXe siècle.",
            pageCount = 1463,
            thumbnailUrl = "https://www.parismuseescollections.paris.fr/sites/default/files/styles/pm_diaporama/public/atoms/images/MVH/aze_mvhperec3069.05_001.jpg?itok=uKikxBqb",
            previewLink = null,
            infoLink = null,
            buyLink = null
        )

        val dummyBook2 = Book(
            id = "1",
            title = "Les Misérables",
            subtitle = "Édition intégrale",
            authors = listOf("Victor Hugo"),
            publisher = "Gallimard",
            publishedDate = "1862",
            description = "Un roman historique et social majeur du XIXe siècle.",
            pageCount = 1463,
            thumbnailUrl = "https://www.parismuseescollections.paris.fr/sites/default/files/styles/pm_diaporama/public/atoms/images/MVH/aze_mvhperec3069.05_001.jpg?itok=uKikxBqb",
            previewLink = null,
            infoLink = null,
            buyLink = null
        )

        val dummyBook3 = Book(
            id = "1",
            title = "Les Misérables",
            subtitle = "Édition intégrale",
            authors = listOf("Victor Hugo"),
            publisher = "Gallimard",
            publishedDate = "1862",
            description = "Un roman historique et social majeur du XIXe siècle.",
            pageCount = 1463,
            thumbnailUrl = "https://www.parismuseescollections.paris.fr/sites/default/files/styles/pm_diaporama/public/atoms/images/MVH/aze_mvhperec3069.05_001.jpg?itok=uKikxBqb",
            previewLink = null,
            infoLink = null,
            buyLink = null
        )

        val dummyBook4 = Book(
            id = "1",
            title = "Les Misérables",
            subtitle = "Édition intégrale",
            authors = listOf("Victor Hugo"),
            publisher = "Gallimard",
            publishedDate = "1862",
            description = "Un roman historique et social majeur du XIXe siècle.",
            pageCount = 1463,
            thumbnailUrl = "https://www.parismuseescollections.paris.fr/sites/default/files/styles/pm_diaporama/public/atoms/images/MVH/aze_mvhperec3069.05_001.jpg?itok=uKikxBqb",
            previewLink = null,
            infoLink = null,
            buyLink = null
        )

        val dummyBook5 = Book(
            id = "1",
            title = "Les Misérables",
            subtitle = "Édition intégrale",
            authors = listOf("Victor Hugo"),
            publisher = "Gallimard",
            publishedDate = "1862",
            description = "Un roman historique et social majeur du XIXe siècle.",
            pageCount = 1463,
            thumbnailUrl = "https://www.parismuseescollections.paris.fr/sites/default/files/styles/pm_diaporama/public/atoms/images/MVH/aze_mvhperec3069.05_001.jpg?itok=uKikxBqb",
            previewLink = null,
            infoLink = null,
            buyLink = null
        )

        val bookList = listOf(dummyBook, dummyBook1, dummyBook2, dummyBook3, dummyBook4, dummyBook5)

        val adapter = BookAdapter(bookList) { selectedBook ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment()
            findNavController().navigate(action)
        }

        binding.bookRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.bookRecyclerView.adapter = adapter
    }
}