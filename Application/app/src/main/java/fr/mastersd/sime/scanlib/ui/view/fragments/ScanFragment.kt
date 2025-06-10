package fr.mastersd.sime.scanlib.ui.view.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.R
import fr.mastersd.sime.scanlib.databinding.FragmentScanBinding
import fr.mastersd.sime.scanlib.ui.viewmodel.BookViewModel
import fr.mastersd.sime.scanlib.ui.viewmodel.ScanViewModel

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by viewModels()
    private val scanViewModel: ScanViewModel by viewModels()

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(requireContext())
    }

    private var camera: Camera? = null
    private var syncStartTime: Long = 0L

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCamera() else handleCameraDenied()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cleanCaptureCache()

        viewModel.setContext(requireContext())

        // Observe les résultats du traitement OCR + appel API
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
        setupTouchToFocus()
        checkCameraPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Nettoie le dossier cache/captures au démarrage du fragment
     */
    private fun cleanCaptureCache() {
        val context = requireContext()
        val captureDir = java.io.File(context.cacheDir, "captures")
        if (captureDir.exists()) {
            captureDir.listFiles()?.forEach { file ->
                // Supprime seulement les fichiers, pas les dossiers
                if (file.isFile) file.delete()
            }
        }
    }

    /**
     * Observe les données des ViewModels :
     * - image capturée
     * - image traitée (bitmap)
     * - textes extraits par OCR
     */
    private fun setupObservers() {
        viewModel.lastImagePath.observe(viewLifecycleOwner) { path ->
            scanViewModel.processImage(path)
        }

        scanViewModel.ocrTexts.observe(viewLifecycleOwner) { texts ->
            val nonEmptyTexts = texts.filter { it.isNotBlank() }
            if (nonEmptyTexts.isNotEmpty()) {
                syncStartTime = System.currentTimeMillis()
                viewModel.syncBooksFromValTexts(nonEmptyTexts)
            } else {
                Toast.makeText(requireContext(), "Aucun texte détecté", Toast.LENGTH_SHORT).show()
            }
            // Suppression de l'image après traitement OCR
            viewModel.lastImagePath.value?.let { path ->
                try {
                    val file = java.io.File(path)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {
                    Log.e("ScanFragment", "Erreur suppression image $path", e)
                }
            }
        }
    }

    /**
     * Configure les interactions utilisateur :
     * - capture avec le bouton central
     */
    private fun setupListeners() = with(binding) {
        captureButton.setOnClickListener { viewModel.captureImage() }
    }

    /**
     * Ajoute le focus manuel au toucher dans CameraX
     */
    private fun setupTouchToFocus() {
        binding.previewView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP && camera != null) {
                val factory = binding.previewView.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)

                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .disableAutoCancel()
                    .build()

                camera?.cameraControl?.startFocusAndMetering(action)

                showFocusIndicator(event.x, event.y)
                v.performClick()
            }
            true
        }
    }

    /**
     * Affiche l’animation circulaire de mise au point
     */
    private fun showFocusIndicator(x: Float, y: Float) {
        val indicator = binding.focusIndicator

        indicator.translationX = x - indicator.width / 2
        indicator.translationY = y - indicator.height / 2
        indicator.visibility = View.VISIBLE
        indicator.alpha = 1f
        indicator.scaleX = 1f
        indicator.scaleY = 1f

        indicator.animate()
            .scaleX(1.8f)
            .scaleY(1.8f)
            .alpha(0f)
            .setDuration(600)
            .withEndAction { indicator.visibility = View.GONE }
            .start()
    }

    /**
     * Vérifie la permission caméra et lance la caméra si accordée
     */
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

    /**
     * Initialise CameraX et bind le flux sur la preview
     */
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
                camera = provider.bindToLifecycle(
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

    /**
     * Retourne à l'écran précédent si permission refusée
     */
    private fun handleCameraDenied() {
        Toast.makeText(requireContext(), "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}
