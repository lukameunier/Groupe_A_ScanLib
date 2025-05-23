package fr.mastersd.sime.scanlib.domain.model

/**
 * Représente un résultat de l'OCR sur une trenche de livre.
 *
 * Contient un champ texte combiné de type "Titre Auteur"
 *
 * Ce champ est utilisé en entrée pour interroger l'API Google Books
 *
 * @property titleAuthor Texte brut détecté sans séparateur
 *
 * @see BookSpineOCR pour l’extraction de texte OCR
 * @see GoogleBooksService pour l’enrichissement des résultats OCR via l’API Google
 * @see BookSyncResult pour le résultat de la synchronisation entre OCR et données enrichies
 */

data class ScanResult(
    val titleAuthor: String,
)