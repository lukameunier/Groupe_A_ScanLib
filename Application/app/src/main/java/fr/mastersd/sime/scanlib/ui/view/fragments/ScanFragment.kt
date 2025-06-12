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
import fr.mastersd.sime.scanlib.databinding.FragmentScanBinding
import fr.mastersd.sime.scanlib.ui.viewmodel.BookViewModel
import fr.mastersd.sime.scanlib.ui.viewmodel.ScanViewModel

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by viewModels()

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(requireContext())
    }

    private var camera: Camera? = null

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCamera() else handleCameraDenied()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkCameraPermission()

        viewModel.lastImagePath.observe(viewLifecycleOwner) { path ->
            if (!path.isNullOrBlank()) {
                val action = ScanFragmentDirections.actionScanFragmentToProcessingFragment(path)
                findNavController().navigate(action)
                viewModel.clearLastImagePath()
            }
        }

        binding.captureButton.setOnClickListener {
            viewModel.captureImage(requireContext())
        }

        setupTouchToFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
     * Retourne à l'écran précédent si permission refusée
     */
    private fun handleCameraDenied() {
        Toast.makeText(requireContext(), "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}
