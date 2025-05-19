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
import android.util.Size

class BookSpineDetector(assetManager: AssetManager) {

    private val interpreter: Interpreter
    private val inputSize = 640  // Taille d'entrée du modèle YOLOv8

    init {
        val model = loadModelFile(assetManager, "best-v1.tflite")
        interpreter = Interpreter(model)
    }

    private fun loadModelFile(assetManager: AssetManager, modelName: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Retourne les boîtes détectées et la taille du bitmap redimensionné
     */
    fun detect(bitmap: Bitmap): Pair<List<RectF>, Size> {
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

        val output = Array(1) { Array(5) { FloatArray(8400) } }
        interpreter.run(inputBuffer, output)

        val outputT = Array(8400) { FloatArray(5) }
        for (i in 0 until 8400) {
            for (j in 0 until 5) {
                outputT[i][j] = output[0][j][i]
            }
        }

        val threshold = 0.25f
        val nmsThreshold = 0.50f
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

        val nmsBoxes = nonMaxSuppression(boxesXYXY, scores, 0.5f, nmsThreshold, 50)

        Log.d("BookSpineDetector", "Boxes detected above threshold: ${boxesXYXY.size}")
        Log.d("BookSpineDetector", "Boxes kept after NMS: ${nmsBoxes.size}")

        nmsBoxes.take(5).forEachIndexed { i, box ->
            Log.d("BookSpineDetector", "Box[$i] = left=${box.left}, top=${box.top}, right=${box.right}, bottom=${box.bottom}")
        }

        return Pair(nmsBoxes, Size(inputSize, inputSize))
    }

    private fun computeIoU(a: RectF, b: RectF): Float {
        val interLeft = max(a.left, b.left)
        val interTop = max(a.top, b.top)
        val interRight = min(a.right, b.right)
        val interBottom = min(a.bottom, b.bottom)
        val interArea = max(0f, interRight - interLeft) * max(0f, interBottom - interTop)
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
        maxOutputSize: Int
    ): List<RectF> {
        val filtered = boxes.zip(scores).filter { it.second >= scoreThreshold }.sortedByDescending { it.second }
        val picked = mutableListOf<RectF>()
        for ((candidateBox, _) in filtered) {
            if (picked.all { computeIoU(candidateBox, it) <= iouThreshold }) {
                picked.add(candidateBox)
                if (picked.size >= maxOutputSize) break
            }
        }
        return picked
    }
}
