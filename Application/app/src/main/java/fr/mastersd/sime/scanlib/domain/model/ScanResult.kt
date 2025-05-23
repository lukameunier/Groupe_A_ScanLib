package fr.mastersd.sime.scanlib.domain.model

/**
 * Représente un résultat de l'OCR sur une trenche de livre.
 *
 * Contient un champ texte combiné de type "Titre Auteur"
 *
 * Ce champ est utilisé en entrée pour interroger l'API Google Books
 *
 * @property titleAuthor Texte brut détecté sans séparateur
 */

data class ScanResult(
    val titleAuthor: String,
)