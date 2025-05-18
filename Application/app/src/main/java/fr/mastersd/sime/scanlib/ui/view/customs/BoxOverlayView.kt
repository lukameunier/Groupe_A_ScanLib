package fr.mastersd.sime.scanlib.ui.view.customs

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Vue personnalisée qui affiche une image et superpose des boîtes de détection (RectF).
 */
class BoxOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val transformMatrix = Matrix()
    private val tempRect = RectF()

    // L’image à afficher (ex: bitmap redimensionné à 640x640)
    private var bitmap: Bitmap? = null

    // Les boîtes de détection à dessiner (coordonnées en pixels)
    private var boxes: List<RectF> = emptyList()

    // Style de dessin pour les boîtes : rouge, trait fin
    private val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    /**
     * Permet de définir l’image et les boîtes, puis déclenche un redessin.
     */
    fun setBitmapAndBoxes(bitmap: Bitmap, boxes: List<RectF>) {
        this.bitmap = bitmap
        this.boxes = boxes
        invalidate() // Demande à Android de redessiner la vue
    }

    /**
     * Méthode appelée automatiquement pour dessiner la vue.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bitmap?.let { original ->
            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()

            val scale = maxOf(viewWidth / original.width, viewHeight / original.height)

            val scaledWidth = original.width * scale
            val scaledHeight = original.height * scale

            val dx = (viewWidth - scaledWidth) / 2f
            val dy = (viewHeight - scaledHeight) / 2f

            transformMatrix.reset()
            transformMatrix.postScale(scale, scale)
            transformMatrix.postTranslate(dx, dy)

            boxes.forEach { box ->
                tempRect.set(box)
                transformMatrix.mapRect(tempRect)
                canvas.drawRect(tempRect, boxPaint)
            }
        }
    }
}
