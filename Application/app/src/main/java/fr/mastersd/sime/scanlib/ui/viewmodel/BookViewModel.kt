package fr.mastersd.sime.scanlib.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor() : ViewModel() {

    private var imageCapture: ImageCapture? = null
    private var appContext: Context? = null

    private val _lastImagePath = MutableLiveData<String>()
    val lastImagePath: LiveData<String> get() = _lastImagePath

    fun setContext(context: Context) {
        appContext = context.applicationContext
    }

    fun setImageCapture(capture: ImageCapture) {
        imageCapture = capture
    }

    fun captureImage() {
        val context = appContext ?: return

        val captureDir = File(context.cacheDir, "captures").apply { mkdirs() }

        val photoFile = File(
            captureDir,
            "${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    _lastImagePath.postValue(photoFile.absolutePath)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("BookViewModel", "Erreur de capture : ${exception.message}", exception)
                }
            }
        )
    }

    fun getAllCapturedImages(): List<File> {
        val context = appContext ?: return emptyList()
        val dir = File(context.cacheDir, "captures")
        return dir.listFiles()?.toList() ?: emptyList()
    }
}
