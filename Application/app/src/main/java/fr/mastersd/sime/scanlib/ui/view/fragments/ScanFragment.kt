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
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentScanBinding
import fr.mastersd.sime.scanlib.ml.BookSpineDetector
import fr.mastersd.sime.scanlib.ui.view.customs.BoxOverlayView
import fr.mastersd.sime.scanlib.ui.viewmodel.BookViewModel
import java.io.FileOutputStream

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by viewModels()

    private lateinit var bookSpineDetector: BookSpineDetector
    private lateinit var overlayView: BoxOverlayView

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(requireContext())
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else handleCameraDenied()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setContext(requireContext())

        bookSpineDetector = BookSpineDetector(requireContext().assets)
        overlayView = binding.overlayView

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
            overlayView.setBitmapAndBoxes(bitmap, boxes, modelSize)
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
                    // Utilisation de getRotatedBitmap ici aussi
                    val rotated = getRotatedBitmap(images[index].absolutePath)
                    previewThumbnail.setImageBitmap(rotated)
                    overlayView.clear()
                }
                .setNegativeButton("Fermer", null)
                .show()
        }

        returnButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> startCamera()

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

            // Preview suiveuse de rotation
            val preview = Preview.Builder()
                .setTargetRotation(binding.previewView.display.rotation)
                .build()
                .also { it.surfaceProvider = binding.previewView.surfaceProvider }

            // ImageCapture avec même rotation
            val imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.previewView.display.rotation)
                //.setTargetResolution(Size(640, 640))
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

    /** Charge et corrige l’orientation d’une JPEG grâce à ses métadonnées EXIF */
    private fun getRotatedBitmap(path: String): Bitmap {
        val bmp = BitmapFactory.decodeFile(path)
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
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
            strokeWidth = 10f
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

