package fr.mastersd.sime.scanlib.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val bookDao: BookDao,
    val googleBooksService: GoogleBooksService = GoogleBooksService()
) {

    suspend fun syncBooksFromValTexts(valTexts: List<String>): BookSyncResult = withContext(Dispatchers.IO) {
        val scanResults = valTexts.map { ScanResult(it) }
        return@withContext fetchBooksAndLog(scanResults)
    }

    suspend fun insertBook(book: Book) {
        bookDao.insertBooks(listOf(book))
    }

    private suspend fun fetchBooksAndLog(scanResults: List<ScanResult>): BookSyncResult {
        val foundBooks = mutableListOf<Book>()
        val notFoundTitles = mutableListOf<String>()

        for (result in scanResults) {
            val book = googleBooksService.searchBook(result.titleAuthor)

            if (book == null) {
                Log.d("BookSync", "❌ Aucun livre trouvé pour : ${result.titleAuthor}")
                notFoundTitles.add(result.titleAuthor)
            } else {
                Log.d("BookSync", "✅ Livre trouvé pour : ${result.titleAuthor}")
                Log.d("BookSync", "📘 Titre         : ${book.title}")
                Log.d("BookSync", "👤 Auteur(s)     : ${book.authors.joinToString()}")
                Log.d("BookSync", "🏢 Éditeur       : ${book.publisher}")
                Log.d("BookSync", "📅 Date de pub.  : ${book.publishedDate}")
                Log.d("BookSync", "🔗 Lien          : ${book.infoLink}")
                Log.d("BookSync", "🖼️ Couverture    : ${book.thumbnailUrl ?: "Pas d'image disponible"}")

                try {
                    bookDao.insertBooks(listOf(book))
                    Log.d("BookSync", "💾 Livre inséré dans Room : ${book.title}")
                } catch (e: Exception) {
                    Log.e("BookSync", "❌ Erreur lors de l'insertion : ${e.message}")
                }

                foundBooks.add(book)
            }
        }

        return BookSyncResult(foundBooks, notFoundTitles)
    }

    suspend fun getAllBooks(): List<Book> = withContext(Dispatchers.IO) {
        bookDao.getAllBooks()
    }

    suspend fun getBooksByCategory(category: String): List<Book> {
        return bookDao.getBooksByCategory(category)
    }

    suspend fun getBooksByMinimumScore(minScore: Double): List<Book> {
        return bookDao.getBooksByMinimumScore(minScore)
    }

    suspend fun getBooksByKeyword(keyword: String): List<Book> {
        return bookDao.getBooksByKeyword(keyword)
    }

    suspend fun getBooksByYear(year: String): List<Book> {
        return bookDao.getBooksByYear(year)
    }

    suspend fun getAllYears(): List<String> {
        return bookDao.getAllBooks()
            .mapNotNull { it.publishedDate?.take(4) }
            .filter { it.matches(Regex("\\d{4}")) }
            .distinct()
            .sortedDescending()
    }

    /**
     * Récuperer les catégories existantes
     *
     * @return Liste des catégories distinct
     */
    suspend fun getAllGenres(): List<String> {
        val allBooks = bookDao.getAllBooks()
        val genres = allBooks.flatMap { it.categories ?: emptyList() }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        Log.d("DEBUG_GENRES", "Genres présents dans la base : $genres")
        return genres
    }

    suspend fun getAllScores(): List<Double> {
        return bookDao.getAllBooks()
            .mapNotNull { it.averageRating }
            .map { String.format(Locale.US, "%.1f", it).toDouble() } // pour normaliser les décimales 3.0
            .distinct()
            .sortedDescending()
    }

    suspend fun getBooksWithoutScore(): List<Book> {
        return bookDao.getBooksByNoScore()
    }

}