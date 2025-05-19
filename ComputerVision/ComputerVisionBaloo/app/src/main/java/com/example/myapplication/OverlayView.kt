package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Vue personnalisée qui affiche une image et superpose des boîtes de détection (RectF).
 */
class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

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
            // Dimensions de la vue (écran)
            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()

            // Calcul du facteur d’échelle pour faire tenir l’image dans la vue
            val scale = minOf(viewWidth / original.width, viewHeight / original.height)

            // Calcul du décalage pour centrer l’image dans la vue
            val dx = (viewWidth - original.width * scale) / 2
            val dy = (viewHeight - original.height * scale) / 2

            // Matrice de transformation : échelle + translation
            val matrix = Matrix().apply {
                postScale(scale, scale)       // redimensionnement
                postTranslate(dx, dy)         // recentrage dans la vue
            }

            // Dessine l’image redimensionnée
            canvas.drawBitmap(original, matrix, null)

            // Pour chaque boîte détectée :
            boxes.forEach { box ->
                val transformed = RectF(box) // copie
                matrix.mapRect(transformed)  // applique l’échelle + translation
                canvas.drawRect(transformed, boxPaint) // dessine la boîte rouge
            }
        }
    }
}
