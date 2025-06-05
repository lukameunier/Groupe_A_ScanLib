package fr.mastersd.sime.scanlib.ui.view.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.R
import fr.mastersd.sime.scanlib.databinding.FragmentScanBinding
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.ui.viewmodel.BookViewModel
import fr.mastersd.sime.scanlib.ui.viewmodel.ScanViewModel

/**
 * Fragment responsable de la capture d'image, de la détection de tranches de livres, de l'extraction OCR, interrogation de l'API Google Books via viewmodel
 *
 * @see BookViewModel pour la logique de synchronisation
 * @see ScanViewModel pour la logique de traitement d'image
 */
@AndroidEntryPoint
class ScanFragment : Fragment() {
    /* ----- initialisation ----- */
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by viewModels()
    private val scanViewModel: ScanViewModel by viewModels() // MODIF MVVM : nouveau ViewModel pour traitement image

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(requireContext())
    }

    private var syncStartTime: Long = 0L

    /**
     * Lance la demande de permission pour la caméra
     */
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else handleCameraDenied()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setContext(requireContext())

        // Observe les résultats après le traitement OCR + appel API,
        // puis navigue vers ScanResultFragment en supprimant ScanFragment de la pile
        viewModel.syncResult.observe(viewLifecycleOwner) { result ->
            val duration = System.currentTimeMillis() - syncStartTime
            Log.d("ScanFragment", "syncResult reçu : ${result.foundBooks.size} livres en $duration ms")

            if (result.foundBooks.isNotEmpty()) {
                val action = ScanFragmentDirections
                    .actionScanFragmentToScanResultFragment(result.foundBooks.toTypedArray())

                val navOptions = androidx.navigation.navOptions {
                    popUpTo(R.id.scanFragment) {
                        inclusive = true
                    }
                }

                findNavController().navigate(action, navOptions)
            } else {
                Toast.makeText(requireContext(), "Aucun livre trouvé pour cette image", Toast.LENGTH_SHORT).show()
            }
        }



        setupObservers()
        setupListeners()
        checkCameraPermission()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Observe les chemins d'image et résultats du traitement (MVVM)
     */
    private fun setupObservers() {
        viewModel.lastImagePath.observe(viewLifecycleOwner) { path ->
            scanViewModel.processImage(path)
        }

        scanViewModel.processedImage.observe(viewLifecycleOwner) { bitmap ->
            binding.previewThumbnail.setImageBitmap(bitmap)
        }

        scanViewModel.ocrTexts.observe(viewLifecycleOwner) { texts ->
            val nonEmptyTexts = texts.filter { it.isNotBlank() }
            if (nonEmptyTexts.isNotEmpty()) {
                syncStartTime = System.currentTimeMillis()
                viewModel.syncBooksFromValTexts(nonEmptyTexts)
            } else {
                Toast.makeText(requireContext(), "Aucun texte détecté", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Configure les boutons de capture et aperçu miniature (actions user)
     */
    private fun setupListeners() = with(binding) {
        captureButton.setOnClickListener { viewModel.captureImage() }

        previewThumbnail.setOnClickListener {
            val images = viewModel.getAllCapturedImages()
            if (images.isEmpty()) {
                Toast.makeText(requireContext(), "Aucune image capturée", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Images capturées")
                .setItems(images.map { it.name }.toTypedArray()) { _, index ->
                    scanViewModel.processImage(images[index].absolutePath)
                }
                .setNegativeButton("Fermer", null)
                .show()
        }
    }

    private fun showBookDetailsDialog(book: Book, allBooks: List<Book>, duration: String) {
        val message = """
            $duration

            📚 Titre            : ${book.title}
            👤 Auteur(s)        : ${book.authors.joinToString()}
            🏢 Éditeur          : ${book.publisher}
            📅 Date de pub.     : ${book.publishedDate}
            📝 Description      : ${book.description}
            📄 Pages            : ${book.pageCount}
            🔗 Lien             : ${book.infoLink ?: "N/A"}
            🖼️ Couverture       : ${book.thumbnailUrl ?: "Pas d'image disponible"}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Détails du livre")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNegativeButton("Voir autres résultats") { _, _ ->
                showBookListDialog(allBooks, duration)
            }
            .show()
    }

    private fun showBookListDialog(books: List<Book>, duration: String) {
        val titledBooks = books.mapIndexed { index, book ->
            "Livre ${index + 1} : ${book.title}"
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Livres détectés (${books.size}) – $duration")
            .setItems(titledBooks) { _, index ->
                showBookDetailsDialog(books[index], books, duration)
            }
            .setNegativeButton("Fermer", null)
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> startCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Permission requise")
                    .setMessage("L'application a besoin d'accéder à la caméra pour scanner les livres.")
                    .setPositiveButton("Autoriser") { _, _ ->
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Refuser") { _, _ -> handleCameraDenied() }
                    .show()
            }
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetRotation(binding.previewView.display.rotation)
                .build()
                .also { it.surfaceProvider = binding.previewView.surfaceProvider }

            val imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.previewView.display.rotation)
                .build()
                .also(viewModel::setImageCapture)

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("ScanFragment", "Échec bind camera", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun handleCameraDenied() {
        Toast.makeText(requireContext(), "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

//================================================================================
//================================================================================
// !: eliminer les doublants avec vm => déplacer le logique de lecture ocr et detection dans vm pour isoler l'affichage et la capture ---> temps de traitement ?
// !: injection via Hilt pour les appels à la bd
// ?: séparer la logique => new [ImageProcessingHelper]: drawBoxesOnBitmap, getRotatedBitmap
//================================================================================
//================================================================================
}
