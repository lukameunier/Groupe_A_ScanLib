package fr.mastersd.sime.scanlib.ui.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.mastersd.sime.scanlib.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity () {
    override fun onCreate(savedInstanceState: Bundle ?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge ()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}