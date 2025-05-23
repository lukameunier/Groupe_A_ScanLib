package fr.mastersd.sime.scanlib.data.file

import android.content.Context
import android.util.Log
import fr.mastersd.sime.scanlib.domain.model.ScanResult
import java.io.File

/**
 * Service de lecture de fichiers OCR au format texte
 *
 * Permet de charger les lignes "titreauteur" depuis les assets de l'applications
 *
 * Chaque ligne est transformée en objet [ScanResult]
 */
class ScanFileReader {

    /**
     * Lit un fichier stocké dans les asserts de l'app
     * Chaque ligne non vide est trabsformée en [ScanResult]
     *
     * Injection des données sans accès au système de fichier
     *
     * @param context Contexte Android, nécessaire pour accéder aux assets
     * @param assetFileName Nom du fichier dans le dossier 'assets/'
     * @return Liste de résultats OCR formatés
     */
    fun readScanResultsFromAssetsOneString(context: Context, assetFileName: String): List<ScanResult> {
        val results = mutableListOf<ScanResult>()
        val assetManager = context.assets
        assetManager.open(assetFileName).bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val content = line.trim()
                if (content.isNotEmpty()) {
                    results.add(ScanResult(content)) // ou null si le second champ est nullable
                }
            }
        }
        Log.d("ScanFileReader", "Résultats lus : $results")
        return results
    }


    /**
     * Parse une ligne de texte pour en faire un objet [ScanResult]
     *
     * Factorisation du parsing de ligne pour éviter la duplication
     *
     * @param line Ligne du fichier texte
     * @return Objet [ScanResult] ou 'null' si la ligne est vide ou invalide
     */
    private fun parseLine(line: String): ScanResult? {
        val parts = line
        return if (parts.length >= 1) {
            ScanResult(parts)
        } else null
    }
}
