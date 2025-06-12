package fr.mastersd.sime.scanlib.ui.view.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentProcessingBinding
import fr.mastersd.sime.scanlib.ui.viewmodel.ScanViewModel
import fr.mastersd.sime.scanlib.ui.viewmodel.BookViewModel

@AndroidEntryPoint
class ProcessingFragment : Fragment() {

    private var _binding: FragmentProcessingBinding? = null
    private val binding get() = _binding!!

    private val scanViewModel: ScanViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels()
    private var ocrTexts: List<String> = emptyList()
    private var imagePath: String? = null
    private var syncStartTime: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProcessingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Récupère le chemin de l'image via Safe Args
        val args = ProcessingFragmentArgs.fromBundle(requireArguments())
        val imagePath = args.imagePath

        // Affiche l'image brute dans l'UI
        val bmp = BitmapFactory.decodeFile(imagePath)
        if (bmp != null) {
            binding.imageOriginal.setImageBitmap(bmp)
            binding.progressBar.visibility = View.VISIBLE
            scanViewModel.processImage(imagePath) // Démarre le traitement (OCR + détection)
        } else {
            Toast.makeText(requireContext(), "Impossible de lire l'image.", Toast.LENGTH_SHORT)
                .show()
            findNavController().navigateUp()
            return
        }

        // Observe le résultat du traitement (image annotée)
        scanViewModel.processedImage.observe(viewLifecycleOwner) { annotated ->
            binding.imageWithBoxes.setImageBitmap(annotated)
        }

        // Observe le résultat OCR, affiche la progression et les résultats
        scanViewModel.ocrTexts.observe(viewLifecycleOwner) { texts ->
            val nonEmptyTexts = texts.filter { it.isNotBlank() }
            binding.progressBar.visibility = View.GONE

            binding.textBookCount.text = "Nombre de livres détectés : ${nonEmptyTexts.size}"
            binding.textOcrResults.text = nonEmptyTexts.joinToString("\n") { "• $it" }
            binding.btnContinue.visibility =
                if (nonEmptyTexts.isNotEmpty()) View.VISIBLE else View.GONE

            // Active le bouton continuer seulement s'il y a des textes
            binding.btnContinue.setOnClickListener {
                binding.btnContinue.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
                bookViewModel.syncBooksFromValTexts(nonEmptyTexts)
            }
        }

        // Observe le résultat de la synchronisation Google Books
        bookViewModel.syncResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            binding.btnContinue.isEnabled = true
            if (result.foundBooks.isNotEmpty()) {
                val action = ProcessingFragmentDirections
                    .actionProcessingFragmentToScanResultFragment(result.foundBooks.toTypedArray())
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Aucun livre trouvé !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
