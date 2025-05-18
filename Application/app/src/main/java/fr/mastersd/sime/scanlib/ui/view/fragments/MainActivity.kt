package fr.mastersd.sime.scanlib.ui.view.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity () {

    private val hideSystemUIRunnable = Runnable {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle ?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge ()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemUI()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            scheduleHideSystemUI()
        }
    }

    private fun scheduleHideSystemUI() {
        handler.removeCallbacks(hideSystemUIRunnable)
        handler.postDelayed(hideSystemUIRunnable, 1000)
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
}