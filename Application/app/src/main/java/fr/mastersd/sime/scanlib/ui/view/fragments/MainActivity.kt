package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.ActivityMainBinding

/**
 * Héberge les fragments de navigation (Scan, Home, Details) et applique un mode plein écran immersif
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //Runnable pour masquer la barre de navigation et la barre de statut
    private val hideSystemUIRunnable = Runnable {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    //Handler pour temporiser l’exécution du masquage
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Active le mode edge-to-edge et initialise le layout principal
     *
     * @param savedInstanceState État sauvegardé en cas de recréation
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemUI()
    }

    /**
     * Réactive le masquage du système quand la fenêtre reprend le focus, pour  éviter que la barre de navigation réapparaisse après une interaction
     *
     * @param hasFocus true si l’activité vient au premier plan
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            scheduleHideSystemUI()
        }
    }

    /**
     * Planifie le masquage de la barre système après un court délai
     */
    private fun scheduleHideSystemUI() {
        handler.removeCallbacks(hideSystemUIRunnable)
        handler.postDelayed(hideSystemUIRunnable, 1000)
    }

    /**
     * Masque immédiatement la barre de statut, la barre de navigation et entre en mode immersif
     */
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

//================================================================================
//================================================================================
// ?: extraire le comportement immersif dans une classe utilitaire --> pas necessaire
//================================================================================
//================================================================================

}
