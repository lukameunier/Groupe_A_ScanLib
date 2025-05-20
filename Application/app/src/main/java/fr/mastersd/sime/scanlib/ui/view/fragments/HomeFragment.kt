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
            description = "Un roman d'aventure et de vengeance...",
            pageCount = 1312,
            industryIdentifiers = listOf("ISBN-13: 9782070411245"),
            readingModesText = true,
            readingModesImage = true,
            printType = "BOOK",
            categories = listOf("Roman historique", "Aventure"),
            averageRating = 4.8,
            ratingsCount = 1250,
            maturityRating = "NOT_MATURE",
            allowAnonLogging = true,
            contentVersion = "1.0.0.0.preview.0",
            language = "fr",
            thumbnailUrl = "https://www.gallimard.fr/system/files/styles/medium/private/migrations/ouvrages/couvertures/A64513.jpg.webp?itok=rh5djx2y",
            smallThumbnailUrl = null,
            previewLink = null,
            infoLink = null,
            canonicalVolumeLink = null,
            country = "FR",
            saleability = "FOR_SALE",
            isEbook = true,
            listPrice = 14.99,
            retailPrice = 11.99,
            currencyCode = "EUR",
            buyLink = null,
            viewability = "PARTIAL",
            embeddable = true,
            publicDomain = false,
            textToSpeechPermission = "ALLOWED",
            epubAvailable = true,
            pdfAvailable = false,
            webReaderLink = null,
            accessViewStatus = "SAMPLE",
            quoteSharingAllowed = true,
            textSnippet = "Un extrait captivant du Comte de Monte-Cristo."
        )

        val dummyBook2 = dummyBook1.copy(
            id = "2",
            title = "Germinal",
            authors = listOf("Émile Zola"),
            publishedDate = "1885",
            description = "Ce roman naturaliste dépeint la vie difficile des mineurs...",
            pageCount = 592,
            thumbnailUrl = "https://www.gallimard.fr/system/files/styles/medium/private/migrations/ouvrages/couvertures/A66847.jpg.webp?itok=QKekMMvx",
            industryIdentifiers = listOf("ISBN-13: 9782070411238"),
            categories = listOf("Roman social", "Drame"),
            averageRating = 4.6,
            ratingsCount = 980,
            textSnippet = "Un passage saisissant du combat ouvrier."
        )

        val dummyBook3 = dummyBook1.copy(
            id = "3",
            title = "L'Assommoir",
            authors = listOf("Émile Zola"),
            publishedDate = "1877",
            description = "Ce roman explore la vie de Gervaise Macquart...",
            pageCount = 480,
            thumbnailUrl = "https://images.epagine.fr/436/9782070411436_1_75.jpg",
            industryIdentifiers = listOf("ISBN-13: 9782070411436"),
            categories = listOf("Roman naturaliste"),
            averageRating = 4.5,
            ratingsCount = 870,
            textSnippet = "Un aperçu poignant de la misère parisienne."
        )

        val dummyBook4 = dummyBook1.copy(
            id = "4",
            title = "Quatrevingt-treize",
            authors = listOf("Victor Hugo"),
            publishedDate = "1874",
            description = "Le dernier roman de Victor Hugo, pendant la Révolution française...",
            pageCount = 528,
            thumbnailUrl = "https://www.gallimard.fr/system/files/styles/medium/private/migrations/ouvrages/couvertures/A50211.jpg.webp?itok=ZtkdyoV8",
            industryIdentifiers = listOf("ISBN-13: 9782070411450"),
            categories = listOf("Historique", "Politique"),
            averageRating = 4.4,
            ratingsCount = 740,
            textSnippet = "Un dilemme moral au cœur de la Révolution."
        )

        val dummyBook5 = dummyBook1.copy(
            id = "5",
            title = "Les Misérables",
            authors = listOf("Victor Hugo"),
            publishedDate = "1862",
            description = "Un roman historique et social majeur du XIXe siècle.",
            pageCount = 1463,
            thumbnailUrl = "https://www.parismuseescollections.paris.fr/sites/default/files/styles/pm_diaporama/public/atoms/images/MVH/aze_mvhperec3069.05_001.jpg?itok=uKikxBqb",
            industryIdentifiers = listOf("ISBN-13: 9782070411498"),
            categories = listOf("Roman historique", "Social"),
            averageRating = 4.9,
            ratingsCount = 1600,
            textSnippet = "Un destin bouleversant entre ombre et lumière."
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