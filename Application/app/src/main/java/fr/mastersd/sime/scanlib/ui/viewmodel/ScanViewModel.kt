package fr.mastersd.sime.scanlib.ui.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookRepository
import fr.mastersd.sime.scanlib.ml.BookSpineDetector
import fr.mastersd.sime.scanlib.ml.BookSpineOCR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val detector: BookSpineDetector,
    private val ocr: BookSpineOCR,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _processedImage = MutableStateFlow<Bitmap?>(null)
    val processedImage: StateFlow<Bitmap?> = _processedImage.asStateFlow()

    private val _foundBooks = MutableStateFlow<List<Book>>(emptyList())
    val foundBooks: StateFlow<List<Book>> = _foundBooks.asStateFlow()

    private val _ocrTexts = MutableStateFlow<List<String>>(emptyList())
    val ocrTexts: StateFlow<List<String>> = _ocrTexts.asStateFlow()

    fun processImage(path: String) {
        viewModelScope.launch {
            val bitmap = getRotatedBitmap(path)
            val (boxes, modelSize) = detector.detect(bitmap)
            val annotated = drawBoxesOnBitmap(bitmap, boxes, modelSize)

            val annotatedPath = path.replace(".jpg", "_boxes.jpg")
            FileOutputStream(annotatedPath).use {
                annotated.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }

            _processedImage.value = annotated

            val texts = ocr.extractTextsFromBoxes(bitmap, boxes).filter { it.isNotBlank() }
            _ocrTexts.value = texts
        }
    }

    private fun getRotatedBitmap(path: String): Bitmap {
        val file = java.io.File(path)
        if (!file.exists()) throw java.io.FileNotFoundException("Le fichier image $path n'existe pas !")
        val bmp = BitmapFactory.decodeFile(path)
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90  -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
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
            canvas.drawRect(
                RectF(box.left * scaleX, box.top * scaleY, box.right * scaleX, box.bottom * scaleY),
                paint
            )
        }
        return output
    }

    fun processImageAndFetchBooks(path: String) {
        viewModelScope.launch {
            val bitmap = getRotatedBitmap(path)
            val (boxes, _) = detector.detect(bitmap)
            val texts = ocr.extractTextsFromBoxes(bitmap, boxes).filter { it.isNotBlank() }

            _foundBooks.value = if (texts.isEmpty()) emptyList()
            else bookRepository.syncBooksFromValTexts(texts).foundBooks
        }
    }
}
