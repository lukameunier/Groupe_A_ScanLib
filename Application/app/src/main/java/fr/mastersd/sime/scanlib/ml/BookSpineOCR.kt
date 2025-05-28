package fr.mastersd.sime.scanlib.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.core.graphics.scale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap

/**
 * Service d'OCR appliqué aux tranches de livres détectées
 *
 * Utilise Google MLKit pour détecter, nettoyer les lignes de texte dans les régions détectées
 *
 * Transforme les régions détectées en chaînes de texte pour des requêtes API
 */
class BookSpineOCR {

    /**
     * Extrait et nettoie les textes OCR à chaque boîte (liste de régions), pour récupérer une ligne lisible de type "titre auteur" par tranche
     *
     * @param image L’image source (bitmap) des tranches
     * @param boxes Les régions (RectF, extrait de YOLO) à traiter par OCR
     * @return Liste des chaînes de texte nettoyées de chaque boîte
     */
    suspend fun extractTextsFromBoxes(image: Bitmap, boxes: List<RectF>): List<String> = withContext(Dispatchers.Default) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val results = mutableListOf<String>()
        val resizedImage = image.scale(640, 640)

        for (box in boxes) {
            try {
                val cropped = cropBitmap(resizedImage, box)
                val zoomedImage = cropped.scale(cropped.width * 3, cropped.height * 3)
                val inputImage = InputImage.fromBitmap(zoomedImage, 0)
                val resultText = recognizer.process(inputImage).await().text
                val line = cleanTextSingleLine(resultText)
                results.add(line)
            } catch (e: Exception) {
                Log.e("BookSpineOCR", "Erreur OCR: ${e.message}")
                results.add("")
            }
        }

        return@withContext results
    }

    /**
     * Découpe une sous-image en fonction de la boîte donnée, pour zoomer et isoler la zone à analyser
     *
     * @param bitmap Image source
     * @param box Région à extraire
     * @return Image bitmap correspondant à la sous-région
     */
    private fun cropBitmap(bitmap: Bitmap, box: RectF): Bitmap {
        val left = box.left.coerceAtLeast(0f).toInt()
        val top = box.top.coerceAtLeast(0f).toInt()
        val right = box.right.coerceAtMost(bitmap.width.toFloat()).toInt()
        val bottom = box.bottom.coerceAtMost(bitmap.height.toFloat()).toInt()

        val rect = Rect(left, top, right, bottom)
        val cropped = createBitmap(rect.width(), rect.height())
        val canvas = Canvas(cropped)
        canvas.drawBitmap(bitmap, -rect.left.toFloat(), -rect.top.toFloat(), null)
        return cropped
    }

    /**
     * Nettoie le texte OCR pour en faire une ligne lisible, pour la requête API
     *
     * Supprime les lignes numériques, normalise les espaces et concatène tout en une ligne
     *
     * @param rawText Texte brut de l'OCR
     * @return Chaîne nettoyée et formatée
     */
    private fun cleanTextSingleLine(rawText: String): String {
        return rawText
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.matches(Regex("^\\d+$")) }
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
