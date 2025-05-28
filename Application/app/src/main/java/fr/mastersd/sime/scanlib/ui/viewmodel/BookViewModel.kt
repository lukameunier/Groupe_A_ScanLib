
package fr.mastersd.sime.scanlib.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.local.BookDatabase
import fr.mastersd.sime.scanlib.data.local.BookEntity
import fr.mastersd.sime.scanlib.data.repository.BookRepositoryImpl
import fr.mastersd.sime.scanlib.domain.model.Book
import fr.mastersd.sime.scanlib.domain.model.BookSyncResult
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel principal de l’application ScanLib, gère :
 * - capture d’images via CameraX
 * - synchronisation avec l’API Google Books
 * - accès aux données locales via Room
 *
 * Expose des `LiveData` vers l’iu et coordonne les appels aux repositories
 *
 * @see BookRepositoryImpl pour la logique métier sous-jacente
 * @see BookDatabase pour la base locale utilisée
 * @see Book pour le modèle affiché
 */
@HiltViewModel
class BookViewModel @Inject constructor() : ViewModel() {

    private lateinit var bookRepository: BookRepositoryImpl //!: ---> injection via Hilt

    private var imageCapture: ImageCapture? = null
    private var appContext: Context? = null

    private val _lastImagePath = MutableLiveData<String>()
    val lastImagePath: LiveData<String> get() = _lastImagePath //chemin de la dernière image capturée

    /**
     * Initialise le contexte application et la base de données
     *
     * appeler dans un Fragment/Activity avan tout -> !: injection Hilt ?
     */
    fun setContext(context: Context) {
        appContext = context.applicationContext
        val db = BookDatabase.getDatabase(appContext!!)
        val bookDao = db.bookDao()
        bookRepository = BookRepositoryImpl(bookDao = bookDao)
    }

    /**
     * Configure le composant CameraX pour capturer des photos
     */
    fun setImageCapture(capture: ImageCapture) {
        imageCapture = capture
    }

    /**
     * Capture une image et enregistre son chemin local dans [_lastImagePath]
     *
     * Crée un répertoire temporaire dans le cache de l’app
     */
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

    /**
     * Récupère toutes les images capturées localement
     *
     * @return Liste des fichiers images dans le dossier cache
     */
    fun getAllCapturedImages(): List<File> {
        val context = appContext ?: return emptyList()
        val dir = File(context.cacheDir, "captures")
        return dir.listFiles()?.toList() ?: emptyList()
    }

    /* --- Synchronisation avec API Google Books --- */

    //observer le résultat de la synchronisation
    private val _syncResult = MutableLiveData<BookSyncResult>()
    val syncResult: LiveData<BookSyncResult> get() = _syncResult

    /**
     * Lance une synchronisation des livres à partir d’un fichier assets ---> !: A SUPPRIMER CAR TEST
     *
     * @param context Contexte pour accéder aux assets
     * @param assetFileName Nom du fichier
     */
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

    /**
     * Lance une synchronisation à partir d’une liste de textes extraits de l’OCR
     *
     * @param texts Lignes OCR à enrichir avec l’API
     */
    fun syncBooksFromValTexts(texts: List<String>) {
        viewModelScope.launch {
            val result = bookRepository.syncBooksFromValTexts(texts)
            _syncResult.postValue(result)
        }
    }

    /* --- Lecture depuis la base locale --- */

    /* A SUPPRIMER car ANCIEN
    //LiveData pour l'observation dynamique des livres en bd
    private val _booksFromDb = MutableLiveData<List<BookEntity>>()
    val booksFromDb: LiveData<List<BookEntity>> get() = _booksFromDb

    /**
     * Récupère tous les livres en bd [BookEntity] et les expose via [_booksFromDb]
     *
     * @param context Contexte requis pour accéder à la base
     */
    fun fetchBooksFromDb(context: Context) {
        viewModelScope.launch {
            val db = BookDatabase.getDatabase(context)
            val books = db.bookDao().getAllBooks()
            _booksFromDb.postValue(books)
        }
    }
*/
    private val _allBooks = MutableLiveData<List<Book>>()
    val allBooks: LiveData<List<Book>> get() = _allBooks

    /**
     * Charge les livres depuis la base locale et les convertit en modèles métier [Book]
     *
     * @param context Contexte requis pour accéder à la base
     */
    fun loadBooksFromDb(context: Context) {
        viewModelScope.launch {
            val db = BookDatabase.getDatabase(context)
            val bookDao = db.bookDao()
            val repo = BookRepositoryImpl(bookDao)
            val books = repo.getAllBooks()
            _allBooks.postValue(books)
        }
    }


//================================================================================
//================================================================================
// !: injection Hilt de [BookRepository] avec un module central => di.[BookModule] de dépendances: [BookDataBase], [BookDao], [GoogleBookService], [ScanFileReader], [BookRepository] ---> A CORRIGER
// !: supprimer l'accès direct à Room ([BookDatabase], [BookDao]): vm connait les détails de la bd => Centraliser tout via [BookRepositoryImpl] : Respcter l'architecture clean
// ?: exposer les erreurs à la UI via des sealed class = remplacer les logs ---> A VOIR
//================================================================================
//================================================================================

}



