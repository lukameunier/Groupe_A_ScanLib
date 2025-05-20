package fr.mastersd.sime.scanlib.data.repository

import android.content.Context
import android.util.Log
import fr.mastersd.sime.scanlib.data.file.ScanFileReader
import fr.mastersd.sime.scanlib.data.remote.GoogleBooksService
import fr.mastersd.sime.scanlib.domain.model.Book
import fr.mastersd.sime.scanlib.domain.model.BookSyncResult
import fr.mastersd.sime.scanlib.domain.model.ScanResult
import fr.mastersd.sime.scanlib.domain.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookRepositoryImpl(
    private val scanFileReader: ScanFileReader = ScanFileReader(),
    val googleBooksService: GoogleBooksService = GoogleBooksService() // public seulement pour logs temporaires
) : BookRepository {

    override suspend fun syncBooksFromScanFile(filePath: String): BookSyncResult = withContext(Dispatchers.IO) {
        val scanResults = scanFileReader.readScanResults(filePath)
        return@withContext fetchBooksAndLog(scanResults)
    }

    override suspend fun syncBooksFromAssets(context: Context, assetFileName: String): BookSyncResult = withContext(Dispatchers.IO) {
        val scanResults = scanFileReader.readScanResultsFromAssetsOneString(context, assetFileName)
        return@withContext fetchBooksAndLog(scanResults)
    }

    // Factorise la logique de fetch et logs
    private fun fetchBooksAndLog(scanResults: List<ScanResult>): BookSyncResult {
        val foundBooks = mutableListOf<Book>()
        val notFoundTitles = mutableListOf<String>()

        for (result in scanResults) {
            val books = googleBooksService.searchBook(result.titleAuthor)
            if (books.isEmpty()) {
                Log.d("BookSync", "Aucun livre trouvé pour: ${result.titleAuthor}")
                notFoundTitles.add("${result.titleAuthor}")
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
                }
                foundBooks.addAll(books)
            }
        }
        return BookSyncResult(foundBooks, notFoundTitles)
    }

    override suspend fun getAllBooks(): List<Book> = emptyList()
}
