package com.example.myapplication.ocr

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

class BookSpineOCR {

    suspend fun extractTextsFromBoxes(
        image: Bitmap,
        boxes: List<RectF>
    ): List<String> = withContext(Dispatchers.Default) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val results = mutableListOf<String>()

        val resizedImage = Bitmap.createScaledBitmap(image, 640, 640, true)

        for (box in boxes) {
            try {
                val cropped = cropBitmap(resizedImage, box)
                val inputImage = InputImage.fromBitmap(cropped, 0)
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

    private fun cropBitmap(bitmap: Bitmap, box: RectF): Bitmap {
        val left = box.left.coerceAtLeast(0f).toInt()
        val top = box.top.coerceAtLeast(0f).toInt()
        val right = box.right.coerceAtMost(bitmap.width.toFloat()).toInt()
        val bottom = box.bottom.coerceAtMost(bitmap.height.toFloat()).toInt()

        val rect = Rect(left, top, right, bottom)
        val cropped = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cropped)
        canvas.drawBitmap(bitmap, -rect.left.toFloat(), -rect.top.toFloat(), null)
        return cropped
    }

    private fun cleanTextSingleLine(rawText: String): String {
        return rawText
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.matches(Regex("^\\d+$")) } // supprime toutes les lignes numériques
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

}
