package fr.mastersd.sime.scanlib.data.repository

import android.content.Context
import android.util.Log
import fr.mastersd.sime.scanlib.data.file.ScanFileReader
import fr.mastersd.sime.scanlib.data.local.BookDao
import fr.mastersd.sime.scanlib.data.local.BookEntity
import fr.mastersd.sime.scanlib.data.mapper.BookMapper
import fr.mastersd.sime.scanlib.data.remote.GoogleBooksService
import fr.mastersd.sime.scanlib.domain.model.Book
import fr.mastersd.sime.scanlib.domain.model.BookSyncResult
import fr.mastersd.sime.scanlib.domain.model.ScanResult
import fr.mastersd.sime.scanlib.domain.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implémentation concrète de [BookRepository]
 *
 * Coordonne et assure la synchronisation entre :
 * - lecture de résultats OCR -> [ScanFileReader]
 * - interrogation de l’API Google Books -> [GoogleBooksService]
 * - persistance locale -> [BookDao]
 *
 * @see BookRepository pour l’interface métier
 * @see BookDao pour l’accès Room
 * @see GoogleBooksService pour la récupération distante
 * @see ScanFileReader pour la lecture des entrées OCR
 * @see BookMapper pour la transformation des modèles
 */
class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val scanFileReader: ScanFileReader = ScanFileReader(),
    val googleBooksService: GoogleBooksService = GoogleBooksService()
) : BookRepository {

    /**
     * Synchronise les livres à partir d’un fichier texte dans assets
     *
     * Chaque ligne du fichier est un résultat OCR brut et les correspondances sont recherchées via l’API
     *
     * @param context Contexte pour accéder aux assets
     * @param assetFileName Nom du fichier texte
     * @return [BookSyncResult] contenant livres trouvés et titres non résolus
     */
    override suspend fun syncBooksFromAssets(context: Context, assetFileName: String): BookSyncResult =
        withContext(Dispatchers.IO) {
            val scanResults = scanFileReader.readScanResultsFromAssetsOneString(context, assetFileName)
            return@withContext fetchBooksAndLog(scanResults)
        }

    /**
     * Synchronise les livres à partir d’une liste de chaînes de texte OCR
     *
     * Pour récuperer le texte resultant de BookSpineOCR
     *
     * @param valTexts Résultats bruts de l’OCR depuis [BookSpineOCR]
     * @return [BookSyncResult] contenant les livres enrichis et les titres échoués
     *
     * @see ml.BookSpineOCR
     */
    override suspend fun syncBooksFromValTexts(valTexts: List<String>): BookSyncResult =
        withContext(Dispatchers.IO) {
            val scanResults = valTexts.map { ScanResult(it) }
            return@withContext fetchBooksAndLog(scanResults)
        }

    /**
     * Insère un livre dans la bd locale Room
     *
     * @param book Entité persistante [BookEntity]
     */
    override suspend fun insertBook(book: BookEntity) {
        bookDao.insertBooks(listOf(book))
    }

    /**
     * Récupère tous les livres enregistrés localement et les convertit en objets métier [Book]
     *
     * @return Liste des livres persistés
     */
    override suspend fun getAllBooks(): List<Book> = withContext(Dispatchers.IO) {
        bookDao.getAllBooks().map { BookMapper.fromEntity(it) }
    }

    /**
     * Méthode privée factorisée : effectue la recherche API pour chaque [ScanResult], insère les livres trouvés dans Room, et génère [BookSyncResult]
     *
     * Gère les logs et erreurs éventuelles.
     *
     * @param scanResults Liste de résultats OCR
     * @return Résultat de synchronisation
     *
     * !!! ---> A SEPARER: fait trop de choses à la fois ===> récupèration(googlebooks), transformation (mapper), insertion (new fonction: saveBooksToRoom)
     *
     * !!! ---> A AMELIORER: injection des dépendances [ScanFileResult], [GoogleBooksService] via le constructeur (module Hilt) au lieu de les instancier directement
     */
    private suspend fun fetchBooksAndLog(scanResults: List<ScanResult>): BookSyncResult {
        val foundBooks = mutableListOf<Book>()
        val notFoundTitles = mutableListOf<String>()

        for (result in scanResults) {
            val books = googleBooksService.searchBook(result.titleAuthor)
            if (books.isEmpty()) {
                Log.d("BookSync", "Aucun livre trouvé pour: ${result.titleAuthor}")
                notFoundTitles.add(result.titleAuthor)
            } else {
                Log.d("BookSync", "${books.size} édition(s) trouvée(s) pour: ${result.titleAuthor}")
                books.forEachIndexed { index, book ->
                    Log.d("BookSync", "🗂️ Édition ${index + 1}")
                    Log.d("BookSync", "📘 Titre         : ${book.title}")
                    Log.d("BookSync", "👤 Auteur(s)     : ${book.authors.joinToString()}")
                    Log.d("BookSync", "🏢 Éditeur       : ${book.publisher}")
                    Log.d("BookSync", "📅 Date de pub.  : ${book.publishedDate}")
                    Log.d("BookSync", "🔗 Lien          : ${book.infoLink}")
                    Log.d("BookSync", "🖼️ Couverture    : ${book.thumbnailUrl ?: "Pas d'image disponible"}")

                    //ajout automatique dans la bd locale
                    val bookEntity = BookMapper.toEntity(book)
                    try {
                        bookDao.insertBooks(listOf(bookEntity))
                        Log.d("BookSync", "✅ Livre inséré dans Room : ${book.title}")
                    } catch (e: Exception) {
                        Log.e("BookSync", "❌ Erreur insertion : ${e.message}")
                    }
                }
                foundBooks.addAll(books)
            }
        }

        return BookSyncResult(foundBooks, notFoundTitles)
    }


//================================================================================
//================================================================================
// ?: extraire les logs dans une autre classe [BookLogger]: peut être inutile
// ?: extraire fetchBooksAndLog comme use-case/heplers pour les tests: -> A VOIR
// !: capturer les erreurs réseaux dans searchBook() avec try/catch: -> NECESSAIRE
// ?: ajouter des codes d'erreurs dans [BookSyncResult]: peut être
//================================================================================
//================================================================================

}