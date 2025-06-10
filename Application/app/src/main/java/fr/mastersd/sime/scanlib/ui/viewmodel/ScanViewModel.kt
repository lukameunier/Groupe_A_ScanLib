package fr.mastersd.sime.scanlib.ui.viewmodel

import android.graphics.*
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookRepository
import fr.mastersd.sime.scanlib.ml.BookSpineDetector
import fr.mastersd.sime.scanlib.ml.BookSpineOCR
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val detector: BookSpineDetector,  // Détecteur d'emplacement des tranches de livres (bounding boxes)
    private val ocr: BookSpineOCR,              // OCR pour extraire du texte à partir des zones détectées
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _processedImage = MutableLiveData<Bitmap>() // Image annotée avec les boîtes dessinées
    val processedImage: LiveData<Bitmap> = _processedImage

    private val _foundBooks = MutableLiveData<List<Book>>()
    val foundBooks: LiveData<List<Book>> = _foundBooks

    private val _ocrTexts = MutableLiveData<List<String>>() // Textes extraits (titre + auteur estimé)
    val ocrTexts: LiveData<List<String>> = _ocrTexts

    /**
     * Fonction principale : détecte les tranches puis applique l'OCR.
     * - Charge et fait pivoter l’image si besoin
     * - Applique la détection
     * - Dessine les boîtes de détection sur une copie de l’image
     * - Extrait le texte de chaque boîte avec l’OCR
     */
    fun processImage(path: String) {
        viewModelScope.launch {
            val bitmap = getRotatedBitmap(path)
            val (boxes, modelSize) = detector.detect(bitmap)
            val annotated = drawBoxesOnBitmap(bitmap, boxes, modelSize)

            FileOutputStream(path).use {
                annotated.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }

            _processedImage.postValue(annotated)

            val texts = ocr.extractTextsFromBoxes(bitmap, boxes)
                .filter { it.isNotBlank() }
            _ocrTexts.postValue(texts)

            try {
                val file = java.io.File(path)
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                android.util.Log.e("ScanViewModel", "Erreur suppression image $path", e)
            }
        }
    }

    /**
     * Lit une image à partir du disque, applique une rotation correcte selon les métadonnées EXIF.
     */
    private fun getRotatedBitmap(path: String): Bitmap {
        val bmp = BitmapFactory.decodeFile(path)
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90   -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180  -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270  -> postRotate(270f)
                // Aucun cas pour NORMAL → pas de rotation
            }
        }

        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    }

    /**
     * Dessine les boîtes (RectF) détectées sur une copie de l’image.
     * On ajuste les coordonnées selon la résolution réelle de l’image.
     */
    private fun drawBoxesOnBitmap(base: Bitmap, boxes: List<RectF>, inputSize: Size): Bitmap {
        val output = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        // Rapport de redimensionnement entre l'image d'entrée du modèle et l'image réelle
        val scaleX = base.width.toFloat() / inputSize.width
        val scaleY = base.height.toFloat() / inputSize.height

        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 50f // Largeur des bordures de boîte
            isAntiAlias = true
        }

        // Redimensionne et dessine chaque boîte
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

    fun processImageAndFetchBooks(path: String) {
        viewModelScope.launch {
            val bitmap = getRotatedBitmap(path)
            val (boxes, modelSize) = detector.detect(bitmap)
            val texts = ocr.extractTextsFromBoxes(bitmap, boxes).filter { it.isNotBlank() }

            if (texts.isEmpty()) {
                _foundBooks.postValue(emptyList())
            } else {
                val result = bookRepository.syncBooksFromValTexts(texts)
                _foundBooks.postValue(result.foundBooks)
            }
        }
    }
}
