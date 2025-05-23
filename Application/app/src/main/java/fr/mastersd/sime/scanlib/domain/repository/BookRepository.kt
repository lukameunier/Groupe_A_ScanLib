package fr.mastersd.sime.scanlib.domain.repository

import fr.mastersd.sime.scanlib.domain.model.Book
import fr.mastersd.sime.scanlib.domain.model.BookSyncResult
import android.content.Context
import fr.mastersd.sime.scanlib.data.local.BookEntity

/**
 * Interface centralisant les opérations de synchronsation: OCR, API + les accès à la bd
 *
 * Permet de découpler la logique métier des implèmentations: clean architecture
 *
 * @see BookRepositoryImpl pour l’implémentation concrète
 * @see Book pour le modèle enrichi de livre
 * @see BookEntity pour l’entité persistée dans Room
 * @see BookSyncResult pour le résultat de la synchronisation OCR + API
 */
interface BookRepository {
    /**
     * Synchronise les titres extraits d'un fichier d'assets avec les résultats obtenus depuis l'API
     *
     * @param context Contexte pour accéder aux assets
     * @param assetFileName Nom du fichier d'OCR
     * @return Résultats de synchronisation: livres trouvés et titres non résolus
     */
    suspend fun syncBooksFromAssets(context: Context, assetFileName: String): BookSyncResult

    /**
     * Synchronise directement une liste de textes (de l’OCR) avec les data de l'API
     *
     * @param valTexts Liste de chaînes
     * @return Résultat structuré contenat les livres trouvés / titres non trouvés
     */
    suspend fun syncBooksFromValTexts(valTexts: List<String>): BookSyncResult

    /**
     * Récupère tous les livres enregistrés dans la bd locale
     *
     * @return Liste des livres disponibles localement
     */
    suspend fun getAllBooks(): List<Book>

    /**
     * Insère un livre dans la base locale (Room)
     *
     * @param book Entité persistante = un livre enrichi
     */
    suspend fun insertBook(book: BookEntity)
}
