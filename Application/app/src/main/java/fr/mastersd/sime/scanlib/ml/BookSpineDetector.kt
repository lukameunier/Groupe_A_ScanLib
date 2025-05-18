package fr.mastersd.sime.scanlib.ml

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.get
import androidx.core.graphics.scale

class BookSpineDetector(assetManager: AssetManager) {

    private val interpreter: Interpreter
    private val inputSize = 640  // Taille d'entrée du modèle YOLOv8

    init {
        val model = loadModelFile(assetManager, "best-v1.tflite")
        interpreter = Interpreter(model)
    }

    /**
     * Charge le modèle .tflite depuis le dossier assets/
     */
    private fun loadModelFile(assetManager: AssetManager, modelName: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Exécute la détection sur une image Bitmap et retourne les boîtes filtrées (RectF)
     */
    fun detect(bitmap: Bitmap): List<RectF> {
        //  Étape 1 : Redimensionner et normaliser l’image (RGB entre 0 et 1.0)
        val resized = bitmap.scale(inputSize, inputSize)
        val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
            .order(ByteOrder.nativeOrder())

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = resized[x, y]
                val r = ((pixel shr 16) and 0xFF) / 255f
                val g = ((pixel shr 8) and 0xFF) / 255f
                val b = (pixel and 0xFF) / 255f
                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }

        //  Étape 2 : Initialiser la sortie [1, 5, 8400] (cx, cy, w, h, conf)
        val output = Array(1) { Array(5) { FloatArray(8400) } }

        //  Étape 3 : Exécuter l’inférence avec le modèle TFLite
        interpreter.run(inputBuffer, output)

        //  Étape 4 : Transposer la sortie [1][5][8400] → [8400][5]
        val outputT = Array(8400) { FloatArray(5) }
        for (i in 0 until 8400) {
            for (j in 0 until 5) {
                outputT[i][j] = output[0][j][i]
            }
        }

        //  Étape 5 : Filtrer les prédictions avec seuil de confiance
        val threshold = 0.25f
        val boxesXYXY = mutableListOf<RectF>()
        val scores = mutableListOf<Float>()

        for (i in 0 until 8400) {
            val cx = outputT[i][0]
            val cy = outputT[i][1]
            val w = outputT[i][2]
            val h = outputT[i][3]
            val conf = outputT[i][4]

            if (conf > threshold) {
                val x1 = cx - w / 2
                val y1 = cy - h / 2
                val x2 = cx + w / 2
                val y2 = cy + h / 2
                boxesXYXY.add(RectF(x1, y1, x2, y2))
                scores.add(conf)
            }
        }

        Log.d("DEBUG", "Score max : ${scores.maxOrNull()}")
        Log.d("DEBUG", "Boxes before NMS: ${boxesXYXY.size}")

        //  Étape 6 : Appliquer la suppression non maximale (NMS)
        val nmsBoxes = nonMaxSuppression(
            boxes = boxesXYXY,
            scores = scores,
            iouThreshold = 0.5f,
            scoreThreshold = threshold,
            maxOutputSize = 50
        )


        Log.d("DEBUG", "Boxes after NMS: ${nmsBoxes.size}")
        return nmsBoxes
    }

    private fun computeIoU(a: RectF, b: RectF): Float {
        // Calcul de l’intersection
        val interLeft = max(a.left, b.left)
        val interTop = max(a.top, b.top)
        val interRight = min(a.right, b.right)
        val interBottom = min(a.bottom, b.bottom)

        val interWidth = max(0f, interRight - interLeft)
        val interHeight = max(0f, interBottom - interTop)
        val interArea = interWidth * interHeight

        // Calcul des aires des boîtes
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)

        val union = areaA + areaB - interArea
        return if (union > 0f) interArea / union else 0f
    }

    private fun nonMaxSuppression(
        boxes: List<RectF>,
        scores: List<Float>,
        iouThreshold: Float,
        scoreThreshold: Float,
        maxOutputSize: Int = 50
    ): List<RectF> {
        //  Filtrer et trier les boîtes par score décroissant
        val filtered = boxes.zip(scores)
            .filter { it.second >= scoreThreshold }
            .sortedByDescending { it.second }

        val picked = mutableListOf<RectF>()

        for ((candidateBox, _) in filtered) {
            var shouldSelect = true

            //  Comparer avec les boîtes déjà sélectionnées
            for (selectedBox in picked) {
                val iou = computeIoU(candidateBox, selectedBox)
                if (iou > iouThreshold) {
                    shouldSelect = false
                    break
                }
            }

            //  Ajouter si non redondant
            if (shouldSelect) {
                picked.add(candidateBox)
                if (picked.size >= maxOutputSize) break
            }
        }

        return picked
    }
}



