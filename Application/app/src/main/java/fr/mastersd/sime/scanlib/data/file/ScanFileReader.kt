package fr.mastersd.sime.scanlib.data.file

import android.content.Context
import android.util.Log
import fr.mastersd.sime.scanlib.domain.model.ScanResult
import java.io.File

class ScanFileReader {

    // Lecture classique depuis le stockage
    fun readScanResults(filePath: String): List<ScanResult> {
        val results = mutableListOf<ScanResult>()
        File(filePath).forEachLine { line ->
            parseLine(line)?.let { results.add(it) }
        }
        return results
    }

    // Lecture depuis assets
    fun readScanResultsFromAssets(context: Context, assetFileName: String): List<ScanResult> {
        val results = mutableListOf<ScanResult>()
        val assetManager = context.assets
        assetManager.open(assetFileName).bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(";")
                if (parts.size >= 2) {
                    results.add(ScanResult(parts[0].trim(), parts[1].trim()))
                }
            }
        }
        Log.d("ScanFileReader", "Résultats lus : $results")
        return results
    }

    // Factorisation du parsing de ligne (évite la duplication)
    private fun parseLine(line: String): ScanResult? {
        val parts = line.split(";")
        return if (parts.size >= 2) {
            ScanResult(parts[0].trim(), parts[1].trim())
        } else null
    }
}
