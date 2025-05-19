package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var detector: BookSpineDetection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Charger image depuis assets
        val originalBitmap = assets.open("biblio1.jpg").use {
            BitmapFactory.decodeStream(it)
        }

        // Redimensionner en 640x640 (entrée du modèle ET affichage)
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 640, 640, true)

        // Initialiser le modèle
        detector = BookSpineDetection(assets)

        // Détection sur l'image redimensionnée
        val boxes = detector.detect(resizedBitmap)

        // Log de test
        println("Détection : ${boxes.size} boîtes")
        boxes.take(5).forEach {
            println("Box: ${it.left}, ${it.top}, ${it.right}, ${it.bottom}")
        }

        // Affichage des résultats
        val overlayView = findViewById<OverlayView>(R.id.overlayView)
        overlayView.setBitmapAndBoxes(resizedBitmap, boxes)
    }
}

/**
 * Recadre l’image au centre en carré (sans redimensionner)
 */
fun centerCropSquare(src: Bitmap): Bitmap {
    val size = minOf(src.width, src.height)  // côté du carré
    val xOffset = (src.width - size) / 2
    val yOffset = (src.height - size) / 2

    return Bitmap.createBitmap(src, xOffset, yOffset, size, size)
}
