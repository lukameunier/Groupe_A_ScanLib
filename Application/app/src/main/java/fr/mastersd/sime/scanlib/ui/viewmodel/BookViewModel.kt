package fr.mastersd.sime.scanlib.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookDatabase
import fr.mastersd.sime.scanlib.data.BookRepository
import fr.mastersd.sime.scanlib.data.BookSyncResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private var imageCapture: ImageCapture? = null

    // Événement one-shot : chemin de l'image capturée → navigation vers ProcessingScreen
    private val _lastImagePath = MutableSharedFlow<String>(replay = 0)
    val lastImagePath: SharedFlow<String> = _lastImagePath.asSharedFlow()

    private val _syncResult = MutableStateFlow<BookSyncResult?>(null)
    val syncResult: StateFlow<BookSyncResult?> = _syncResult.asStateFlow()

    private val _booksFromDb = MutableStateFlow<List<Book>>(emptyList())
    val booksFromDb: StateFlow<List<Book>> = _booksFromDb.asStateFlow()

    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    val allBooks: StateFlow<List<Book>> = _allBooks.asStateFlow()

    fun clearSyncResult() {
        _syncResult.value = null
    }

    fun setImageCapture(capture: ImageCapture) {
        imageCapture = capture
    }

    fun captureImage(context: Context) {
        val captureDir = File(context.cacheDir, "captures").apply { mkdirs() }
        val photoFile = File(captureDir, "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewModelScope.launch {
                        _lastImagePath.emit(photoFile.absolutePath)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("BookViewModel", "Erreur de capture : ${exception.message}", exception)
                }
            }
        )
    }

    fun fetchBooksFromDb(context: Context) {
        viewModelScope.launch {
            val db = BookDatabase.getDatabase(context)
            _booksFromDb.value = db.bookDao().getAllBooks()
        }
    }

    fun syncBooksFromValTexts(texts: List<String>) {
        viewModelScope.launch {
            _syncResult.value = bookRepository.syncBooksFromValTexts(texts)
        }
    }

    fun loadBooksFromDb() {
        viewModelScope.launch {
            _allBooks.value = bookRepository.getAllBooks()
        }
    }

    fun insertBook(book: Book) {
        viewModelScope.launch {
            bookRepository.insertBook(book)
        }
    }
}
