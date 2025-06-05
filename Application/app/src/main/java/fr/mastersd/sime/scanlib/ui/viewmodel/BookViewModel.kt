package fr.mastersd.sime.scanlib.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookDatabase
import fr.mastersd.sime.scanlib.data.BookRepository
import fr.mastersd.sime.scanlib.data.BookSyncResult
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor() : ViewModel() {

    private lateinit var bookRepository: BookRepository
    private var imageCapture: ImageCapture? = null
    private var appContext: Context? = null

    private val _lastImagePath = MutableLiveData<String>()
    val lastImagePath: LiveData<String> get() = _lastImagePath

    private val _syncResult = MutableLiveData<BookSyncResult>()
    val syncResult: LiveData<BookSyncResult> get() = _syncResult

    private val _booksFromDb = MutableLiveData<List<Book>>()
    val booksFromDb: LiveData<List<Book>> get() = _booksFromDb

    private val _allBooks = MutableLiveData<List<Book>>()
    val allBooks: LiveData<List<Book>> get() = _allBooks

    // Initialise Room + Repository
    fun setContext(context: Context) {
        appContext = context.applicationContext
        val db = BookDatabase.getDatabase(appContext!!)
        val bookDao = db.bookDao()
        bookRepository = BookRepository(bookDao = bookDao)
    }

    fun setImageCapture(capture: ImageCapture) {
        imageCapture = capture
    }

    fun captureImage() {
        val context = appContext ?: return
        val captureDir = File(context.cacheDir, "captures").apply { mkdirs() }
        val photoFile = File(captureDir, "${System.currentTimeMillis()}.jpg")
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

    fun loadBooksFromDb() {
        if (!::bookRepository.isInitialized) return
        viewModelScope.launch {
            val books = bookRepository.getAllBooks()
            _allBooks.postValue(books)
        }
    }

    fun insertBook(book: Book) {
        if (!::bookRepository.isInitialized) return
        viewModelScope.launch {
            bookRepository.insertBook(book)
        }
    }
}
