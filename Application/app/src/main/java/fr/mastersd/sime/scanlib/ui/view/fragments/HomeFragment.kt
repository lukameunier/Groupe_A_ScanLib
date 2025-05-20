package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
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

        val dummyBook1 = Book(
            id = "1",
            title = "Le Comte de Monte-Cristo",
            subtitle = "Édition intégrale",
            authors = listOf("Alexandre Dumas"),
            publisher = "Gallimard",
            publishedDate = "1844",
            description = "Un roman d'aventure et de vengeance, suivant Edmond Dantès, injustement emprisonné, qui s'évade et cherche à se venger de ceux qui l'ont trahi.",
            pageCount = 1312,
            thumbnailUrl = "https://www.gallimard.fr/system/files/styles/medium/private/migrations/ouvrages/couvertures/A64513.jpg.webp?itok=rh5djx2y",
            previewLink = null,
            infoLink = null,
            buyLink = null
        )

        val dummyBook2 = Book(
            id = "2",
            title = "Germinal",
            subtitle = "Édition intégrale",
            authors = listOf("Émile Zola"),
            publisher = "Gallimard",
            publishedDate = "1885",
            description = "Ce roman naturaliste dépeint la vie difficile des mineurs dans le nord de la France au XIXe siècle, mettant en lumière les luttes sociales et les conditions de travail éprouvantes.",
            pageCount = 592,
            thumbnailUrl = "https://www.gallimard.fr/system/files/styles/medium/private/migrations/ouvrages/couvertures/A66847.jpg.webp?itok=QKekMMvx",
            previewLink = null,
            infoLink = null,
            buyLink = null
        )

        val dummyBook3 = Book(
            id = "3",
            title = "L'Assommoir",
            subtitle = "Édition intégrale",
            authors = listOf("Émile Zola"),
            publisher = "Gallimard",
            publishedDate = "1877",
            description = "Ce roman explore la vie de Gervaise Macquart, une blanchisseuse à Paris, et sa descente progressive dans la pauvreté et l'alcoolisme, illustrant les effets dévastateurs de la misère urbaine.",
            pageCount = 480,
            thumbnailUrl = "https://images.epagine.fr/436/9782070411436_1_75.jpg",
            previewLink = null,
            infoLink = null,
            buyLink = null
        )

        val dummyBook4 = Book(
            id = "4",
            title = "Quatrevingt-treize",
            subtitle = "Édition intégrale",
            authors = listOf("Victor Hugo"),
            publisher = "Gallimard",
            publishedDate = "1874",
            description = "Le dernier roman de Victor Hugo, se déroulant pendant la Révolution française, explore les conflits entre révolutionnaires et royalistes, mettant en scène des personnages confrontés à des dilemmes moraux profonds.",
            pageCount = 528,
            thumbnailUrl = "https://www.gallimard.fr/system/files/styles/medium/private/migrations/ouvrages/couvertures/A50211.jpg.webp?itok=ZtkdyoV8",
            previewLink = null,
            infoLink = null,
            buyLink = null
        )

        val dummyBook5 = Book(
            id = "5",
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

        val bookList = listOf(dummyBook1, dummyBook2, dummyBook3, dummyBook4, dummyBook5)

        val adapter = BookAdapter(bookList) { selectedBook ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(selectedBook)
            findNavController().navigate(action)
        }

        binding.bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.bookRecyclerView.adapter = adapter
    }
}