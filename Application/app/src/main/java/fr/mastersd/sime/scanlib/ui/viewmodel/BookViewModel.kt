package fr.mastersd.sime.scanlib.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.local.BookDatabase
import fr.mastersd.sime.scanlib.data.local.BookEntity
import fr.mastersd.sime.scanlib.data.remote.GoogleBooksService
import fr.mastersd.sime.scanlib.data.repository.BookRepositoryImpl
import fr.mastersd.sime.scanlib.domain.model.BookSyncResult
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor() : ViewModel() {

    private lateinit var bookRepository: BookRepositoryImpl

    private var imageCapture: ImageCapture? = null
    private var appContext: Context? = null

    private val _lastImagePath = MutableLiveData<String>()
    val lastImagePath: LiveData<String> get() = _lastImagePath


    fun setContext(context: Context) {
        appContext = context.applicationContext
        val db = BookDatabase.getDatabase(appContext!!)
        val bookDao = db.bookDao()
        bookRepository = BookRepositoryImpl(bookDao = bookDao)
    }

    fun setImageCapture(capture: ImageCapture) {
        imageCapture = capture
    }

    fun captureImage() {
        val context = appContext ?: return

        val captureDir = File(context.cacheDir, "captures").apply { mkdirs() }

        val photoFile = File(
            captureDir,
            "${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    _lastImagePath.postValue(photoFile.absolutePath)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("BookViewModel", "Erreur de capture : ${exception.message}", exception)
                }
            }
        )
    }

    fun getAllCapturedImages(): List<File> {
        val context = appContext ?: return emptyList()
        val dir = File(context.cacheDir, "captures")
        return dir.listFiles()?.toList() ?: emptyList()
    }

    // ---------------------
    // AJOUT DE LA PARTIE REQUÊTAGE GOOGLE BOOKS
    // ---------------------

    private val _syncResult = MutableLiveData<BookSyncResult>()
    val syncResult: LiveData<BookSyncResult> get() = _syncResult

    fun syncBooksFromAssets(context: Context, assetFileName: String = "scan.txt") {
        viewModelScope.launch {
            val db = BookDatabase.getDatabase(context)
            val bookDao = db.bookDao()
            val bookRepository = BookRepositoryImpl(bookDao = bookDao)

            try {
                val result = bookRepository.syncBooksFromAssets(context, assetFileName)
                _syncResult.postValue(result)
            } catch (e: Exception) {
                Log.e("BookViewModel", "Erreur de synchronisation : ${e.message}", e)
            }
        }
    }

    //-------------------------
    // TEST INSERTION MANUELLE
    //-------------------------
    fun insertSampleBook(context: Context) {
        viewModelScope.launch {
            val db = BookDatabase.getDatabase(context)
            val repo = BookRepositoryImpl(bookDao = db.bookDao())

            val book = BookEntity(
                id = "test123",
                title = "Mon Livre Test",
                subtitle = "Sous-titre test",
                authors = listOf("Auteur Un", "Auteur Deux"),
                publisher = "Éditeur Test",
                publishedDate = "2025-01-01",
                description = "Ceci est un livre fictif pour test.",
                pageCount = 123,
                industryIdentifiers = listOf("ISBN1234"),
                readingModesText = true,
                readingModesImage = true,
                printType = "BOOK",
                categories = listOf("Test"),
                averageRating = 4.5,
                ratingsCount = 20,
                maturityRating = "NOT_MATURE",
                allowAnonLogging = true,
                contentVersion = "1.0.0",
                language = "fr",
                thumbnailUrl = null,
                smallThumbnailUrl = null,
                previewLink = null,
                infoLink = null,
                canonicalVolumeLink = null,
                country = "FR",
                saleability = "FOR_SALE",
                isEbook = false,
                listPrice = 10.99,
                retailPrice = 8.99,
                currencyCode = "EUR",
                buyLink = null,
                viewability = "PARTIAL",
                embeddable = true,
                publicDomain = false,
                textToSpeechPermission = "ALLOWED",
                epubAvailable = true,
                pdfAvailable = false,
                webReaderLink = null,
                accessViewStatus = "SAMPLE",
                quoteSharingAllowed = true,
                textSnippet = "Un petit extrait de test."
            )

            repo.insertBook(book)
            Log.d("BOOK_INSERT", "✅ Livre inséré : ${book.title}")
        }
    }

    fun getAllBooks(context: Context) {
        viewModelScope.launch {
            val db = BookDatabase.getDatabase(context)
            val books = db.bookDao().getAllBooks()
            books.forEach {
                Log.d("BOOKS_DB", "📘 ${it.title} par ${it.authors.joinToString()}")
            }
        }
    }

    private val _booksFromDb = MutableLiveData<List<BookEntity>>()
    val booksFromDb: LiveData<List<BookEntity>> get() = _booksFromDb

    fun fetchBooksFromDb(context: Context) {
        viewModelScope.launch {
            val db = BookDatabase.getDatabase(context)
            val books = db.bookDao().getAllBooks()
            _booksFromDb.postValue(books)
        }
    }

    fun syncBooksFromValTexts(texts: List<String>) {
        viewModelScope.launch {
            val result = bookRepository.syncBooksFromValTexts(texts)
            _syncResult.postValue(result)
        }
    }


}
