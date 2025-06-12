package fr.mastersd.sime.scanlib.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private var imageCapture: ImageCapture? = null

    // Contient le chemin de la dernière image capturée
    private val _lastImagePath = MutableLiveData<String?>()
    val lastImagePath: LiveData<String?> get() = _lastImagePath

    // Résultat du processus de synchronisation avec Google Books API
    private val _syncResult = MutableLiveData<BookSyncResult>()
    val syncResult: LiveData<BookSyncResult> get() = _syncResult

    // Liste des livres récupérés depuis la base de données locale (Room)
    private val _booksFromDb = MutableLiveData<List<Book>>()
    val booksFromDb: LiveData<List<Book>> get() = _booksFromDb

    // Liste complète des livres chargée via une autre méthode
    private val _allBooks = MutableLiveData<List<Book>>()
    val allBooks: LiveData<List<Book>> get() = _allBooks

    fun clearLastImagePath() {
        _lastImagePath.value = null
    }


    /**
     * Enregistre une instance d'ImageCapture (CameraX) pour effectuer les captures plus tard.
     */
    fun setImageCapture(capture: ImageCapture) {
        imageCapture = capture
    }

    /**
     * Capture une image à l’aide de CameraX et sauvegarde le fichier dans un répertoire temporaire.
     * Si la capture réussit, le chemin est publié dans le LiveData lastImagePath.
     */
    fun captureImage(context: Context) {
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

    /**
     * Récupère tous les livres de la base de données locale Room et les publie via booksFromDb.
     */
    fun fetchBooksFromDb(context: Context) {
        viewModelScope.launch {
            val db = BookDatabase.getDatabase(context)
            val books = db.bookDao().getAllBooks()
            _booksFromDb.postValue(books)
        }
    }

    /**
     * Lance une requête vers Google Books API pour récupérer les informations associées
     * aux textes détectés (issus de l’OCR). Le résultat est publié dans syncResult.
     */
    fun syncBooksFromValTexts(texts: List<String>) {
        viewModelScope.launch {
            val result = bookRepository.syncBooksFromValTexts(texts)
            _syncResult.postValue(result)
        }
    }

    /**
     * Charge tous les livres depuis la base de données en utilisant le repository.
     * Les livres sont publiés dans allBooks.
     */
    fun loadBooksFromDb() {
        viewModelScope.launch {
            val books = bookRepository.getAllBooks()
            _allBooks.postValue(books)
        }
    }

    /**
     * Insère un livre dans la base de données locale.
     * À utiliser uniquement après sélection manuelle par l'utilisateur.
     */
    fun insertBook(book: Book) {
        viewModelScope.launch {
            bookRepository.insertBook(book)
        }
    }
}
