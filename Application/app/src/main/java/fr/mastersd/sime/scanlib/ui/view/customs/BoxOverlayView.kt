package fr.mastersd.sime.scanlib.ui.view.customs

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Size
import android.view.View

class BoxOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val tempRect = RectF()

    private var bitmap: Bitmap? = null
    private var boxes: List<RectF> = emptyList()
    private var modelInputSize: Size? = null

    private val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    fun setBitmapAndBoxes(bitmap: Bitmap, boxes: List<RectF>, originalSize: Size) {
        this.bitmap = bitmap
        this.boxes = boxes
        this.modelInputSize = originalSize
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bitmap?.let { bmp ->
            modelInputSize?.let { input ->
                val scaleX = width.toFloat() / input.width.toFloat()
                val scaleY = height.toFloat() / input.height.toFloat()

                val avgSize = (bmp.width + bmp.height) / 2f
                boxPaint.strokeWidth = avgSize / 200f

                boxes.forEach { box ->
                    tempRect.set(
                        box.left * scaleX,
                        box.top * scaleY,
                        box.right * scaleX,
                        box.bottom * scaleY
                    )
                    canvas.drawRect(tempRect, boxPaint)
                }
            }
        }
    }

    fun clear() {
        this.bitmap = null
        this.boxes = emptyList()
        this.modelInputSize = null
        invalidate()
    }
}
