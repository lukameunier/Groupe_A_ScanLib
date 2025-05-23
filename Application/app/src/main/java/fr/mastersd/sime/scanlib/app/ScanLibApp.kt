package fr.mastersd.sime.scanlib.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Point d'entrée principal de l'application ScanLib.
 *
 * Initialise Hilt pour permettre l'injection de dépendances dans toute l'application
 * Elle est référencée dans le manifeste comme `android:name`.
*/
@HiltAndroidApp
class ScanLibApplication : Application()
