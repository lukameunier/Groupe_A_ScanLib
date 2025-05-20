package fr.mastersd.sime.scanlib.domain.repository

import fr.mastersd.sime.scanlib.domain.model.Book
import fr.mastersd.sime.scanlib.domain.model.BookSyncResult
import android.content.Context
import fr.mastersd.sime.scanlib.data.local.BookEntity

interface BookRepository {
    suspend fun syncBooksFromScanFile(filePath: String): BookSyncResult
    suspend fun syncBooksFromAssets(context: Context, assetFileName: String): BookSyncResult
    suspend fun getAllBooks(): List<Book>
    suspend fun insertBook(book: BookEntity)
}
