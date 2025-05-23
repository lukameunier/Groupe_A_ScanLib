package fr.mastersd.sime.scanlib.domain.model

/**
 * Modèle de résultat de synchronisation entre les titres OCR et les data récupérées
 *
 * Contient 2 listes :
 * - [foundBooks]: livres trouvés via API Google Books (+métadonnées)
 * - [notFoundTitles]: titres ayant aucune correspondance dans l'API
 *
 * Distinction des 2 possiblités de retour de l'API Google Books
 *
 * @property foundBooks Liste de livres enrichis avec succès
 * @property notFoundTitles Liste des titres non résolus ou ambigus
 *
 * @see ScanResult pour la source OCR brute
 * @see Book pour le modèle enrichi
 * @see BookRepositoryImpl pour l’étape de synchronisation entre OCR et données API
 */
data class BookSyncResult(
    val foundBooks: List<Book>,
    val notFoundTitles: List<String>
)