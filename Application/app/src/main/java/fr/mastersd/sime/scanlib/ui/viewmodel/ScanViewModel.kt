package fr.mastersd.sime.scanlib.ui.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.*
import fr.mastersd.sime.scanlib.ml.BookSpineDetector
import fr.mastersd.sime.scanlib.ml.BookSpineOCR
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val detector: BookSpineDetector,
    private val ocr: BookSpineOCR
) : ViewModel() {

    private val _processedImage = MutableLiveData<Bitmap>()
    val processedImage: LiveData<Bitmap> = _processedImage

    private val _ocrTexts = MutableLiveData<List<String>>()
    val ocrTexts: LiveData<List<String>> = _ocrTexts

    /**
     * Lance détection + OCR depuis le chemin d’une image.
     */
    fun processImage(path: String) {
        viewModelScope.launch {
            val bitmap = getRotatedBitmap(path)
            val (boxes, modelSize) = detector.detect(bitmap)
            val annotated = drawBoxesOnBitmap(bitmap, boxes, modelSize)

            // Écrase l’image originale (ou pas, à toi de choisir)
            FileOutputStream(path).use {
                annotated.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }

            _processedImage.postValue(annotated)

            val texts = ocr.extractTextsFromBoxes(bitmap, boxes).filter { it.isNotBlank() }
            _ocrTexts.postValue(texts)
        }
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
