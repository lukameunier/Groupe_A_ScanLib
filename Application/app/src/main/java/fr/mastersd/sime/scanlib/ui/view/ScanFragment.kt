package fr.mastersd.sime.scanlib.ui.view

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.FragmentScanBinding

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                //startCamera()
            } else {
                Toast.makeText(requireContext(), "Permission caméra refusée", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkCameraPermissionAndStart()

        binding.returnButton.setOnClickListener {
            val action = ScanFragmentDirections.actionScanFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    private fun checkCameraPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                //startCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Permission requise")
                    .setMessage("L'application a besoin d'accéder à la caméra pour scanner les livres.")
                    .setPositiveButton("Autoriser") { _, _ ->
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Refuser") { _, _ ->
                        findNavController().navigateUp()
                    }
                    .show()
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
