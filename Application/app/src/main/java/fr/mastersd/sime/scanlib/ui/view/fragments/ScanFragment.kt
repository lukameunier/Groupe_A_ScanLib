package fr.mastersd.sime.scanlib.ui.view.fragments

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.util.Size
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.ocr.BookSpineOCR
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentScanBinding
import fr.mastersd.sime.scanlib.domain.model.Book
import fr.mastersd.sime.scanlib.ml.BookSpineDetector
import fr.mastersd.sime.scanlib.ui.viewmodel.BookViewModel
import kotlinx.coroutines.launch
import java.io.FileOutputStream

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by viewModels()

    private lateinit var bookSpineDetector: BookSpineDetector
    private lateinit var bookSpineOCR: BookSpineOCR

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(requireContext())
    }

    private var syncStartTime: Long = 0L

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

        bookSpineDetector = BookSpineDetector(requireContext().assets)
        bookSpineOCR = BookSpineOCR()

        viewModel.syncResult.observe(viewLifecycleOwner) { result ->
            val duration = System.currentTimeMillis() - syncStartTime
            val timeString = "️${duration} ms"

            Log.d("ScanFragment", "syncResult reçu : ${result.foundBooks.size} livres en $duration ms")

            if (result.foundBooks.isNotEmpty()) {
                if (result.foundBooks.size == 1) {
                    showBookDetailsDialog(result.foundBooks[0], result.foundBooks, timeString)
                } else {
                    showBookListDialog(result.foundBooks, timeString)
                }
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

    private fun setupObservers() {
        viewModel.lastImagePath.observe(viewLifecycleOwner) { path ->
            val bitmap = getRotatedBitmap(path)
            val (boxes, modelSize) = bookSpineDetector.detect(bitmap)

            val boxedBitmap = drawBoxesOnBitmap(bitmap, boxes, modelSize)

            try {
                FileOutputStream(path).use { output ->
                    boxedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                }
                Log.d("ScanFragment", "Image avec boîtes enregistrée dans $path")
            } catch (e: Exception) {
                Log.e("ScanFragment", "Erreur lors de l’enregistrement de l’image annotée", e)
            }

            binding.previewThumbnail.setImageBitmap(boxedBitmap)

            // Ajout : lancer OCR après la détection
            lifecycleScope.launch {
                val texts = bookSpineOCR.extractTextsFromBoxes(bitmap, boxes)
                val nonEmptyTexts = texts.filter { it.isNotBlank() }

                if (nonEmptyTexts.isNotEmpty()) {
                    syncStartTime = System.currentTimeMillis()
                    viewModel.syncBooksFromValTexts(nonEmptyTexts)
                } else {
                    Toast.makeText(requireContext(), "Aucun texte détecté", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
                    val rotated = getRotatedBitmap(images[index].absolutePath)
                    previewThumbnail.setImageBitmap(rotated)
                }
                .setNegativeButton("Fermer", null)
                .show()
        }

        //Bouton temporaire pour test manuel Room
        tempButton.setOnClickListener {

         syncStartTime = System.currentTimeMillis()
//         viewModel.syncBooksFromAssets(requireContext(), "scan.txt")

            // Charger les livres en base locale
            viewModel.loadBooksFromDb(requireContext())

            // Observer et afficher les résultats
            viewModel.allBooks.observe(viewLifecycleOwner) { books ->
                val duration = System.currentTimeMillis() - syncStartTime
                if (books.isEmpty()) {
                    Toast.makeText(requireContext(), "📂 Aucun livre trouvé dans la base locale", Toast.LENGTH_SHORT).show()
                } else {
                    val bookTitles = books.mapIndexed { index, book ->
                        "📘 ${index + 1}. ${book.title}"
                    }.toTypedArray()

                    AlertDialog.Builder(requireContext())
                        .setTitle("Livres en base locale (${books.size}) – ${duration}ms")
                        .setItems(bookTitles) { _, index ->
                            val book = books[index]
                            val details = """
                        📚 Titre            : ${book.title}
                        👤 Auteur(s)        : ${book.authors.joinToString()}
                        🏢 Éditeur          : ${book.publisher}
                        📅 Date de pub.     : ${book.publishedDate}
                        📄 Pages            : ${book.pageCount}
                        🔗 Lien             : ${book.infoLink ?: "N/A"}
                    """.trimIndent()

                            AlertDialog.Builder(requireContext())
                                .setTitle("Détails du livre")
                                .setMessage(details)
                                .setPositiveButton("Fermer", null)
                                .show()
                        }
                        .setNegativeButton("Fermer", null)
                        .show()
                }
            }


        //-----------------------------
        //INSERTION D'UN LIVRE MANUEL
        //------------------------------
        /* A decommenter si envie de tester 
        viewModel.insertSampleBook(requireContext())
        viewModel.getAllBooks(requireContext())
        viewModel.fetchBooksFromDb(requireContext())
        viewModel.booksFromDb.observe(viewLifecycleOwner) { books ->
            books.forEach {
                Toast.makeText(requireContext(), "📘 ${it.title}", Toast.LENGTH_SHORT).show()
            }
        }
         */
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

    private fun getRotatedBitmap(path: String): Bitmap {
        val bmp = BitmapFactory.decodeFile(path)
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90   -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180  -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270  -> postRotate(270f)
            }
        }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    }

    private fun handleCameraDenied() {
        Toast.makeText(requireContext(), "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun drawBoxesOnBitmap(base: Bitmap, boxes: List<RectF>, inputSize: Size): Bitmap {
        val output = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        val scaleX = base.width.toFloat() / inputSize.width
        val scaleY = base.height.toFloat() / inputSize.height

        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 50f
            isAntiAlias = true
        }

        boxes.forEach { box ->
            val scaledBox = RectF(
                box.left * scaleX,
                box.top * scaleY,
                box.right * scaleX,
                box.bottom * scaleY
            )
            canvas.drawRect(scaledBox, paint)
        }

        return output
    }
}
