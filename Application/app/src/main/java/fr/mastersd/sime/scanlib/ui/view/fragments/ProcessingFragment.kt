package fr.mastersd.sime.scanlib.ui.view.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentProcessingBinding
import fr.mastersd.sime.scanlib.ui.viewmodel.ScanViewModel
import fr.mastersd.sime.scanlib.ui.viewmodel.BookViewModel
import kotlinx.coroutines.launch

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

        val bmp = if (imagePath?.startsWith("content://") == true) {
            try {
                val uri = android.net.Uri.parse(imagePath)
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                null
            }
        } else {
            BitmapFactory.decodeFile(imagePath)
        }

        if (bmp != null) {
            binding.imageOriginal.setImageBitmap(bmp)

            // Convertir l’image en fichier temporaire si elle vient d’un URI content://
            val realPath = if (imagePath?.startsWith("content://") == true) {
                val tempFile = java.io.File.createTempFile("temp_imported", ".jpg", requireContext().cacheDir)
                requireContext().contentResolver.openInputStream(android.net.Uri.parse(imagePath))?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                tempFile.absolutePath
            } else imagePath

            scanViewModel.processImage(realPath ?: return)
        } else {
            Toast.makeText(requireContext(), "Impossible de lire l'image.", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe le résultat du traitement (image annotée)
                launch {
                    scanViewModel.processedImage.collect { annotated ->
                        if (annotated != null) binding.imageWithBoxes.setImageBitmap(annotated)
                    }
                }

                // Observe le résultat OCR
                launch {
                    scanViewModel.ocrTexts.collect { texts ->
                        val nonEmptyTexts = texts.filter { it.isNotBlank() }
                        binding.textBookCount.text = "Nombre de livres détectés : ${nonEmptyTexts.size}"
                        binding.textOcrResults.text = nonEmptyTexts.joinToString("\n") { "• $it" }
                        binding.btnContinue.visibility =
                            if (nonEmptyTexts.isNotEmpty()) View.VISIBLE else View.GONE
                        binding.btnContinue.setOnClickListener {
                            binding.btnContinue.isEnabled = false
                            bookViewModel.syncBooksFromValTexts(nonEmptyTexts)
                        }
                    }
                }

                // Observe le résultat de la synchronisation Google Books
                launch {
                    bookViewModel.syncResult.collect { result ->
                        if (result == null) return@collect
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
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
