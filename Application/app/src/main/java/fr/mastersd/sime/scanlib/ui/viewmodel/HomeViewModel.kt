package fr.mastersd.sime.scanlib.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookRepository
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.label.Category
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    // Donnée privée modifiable
    private val _books = MutableLiveData<List<Book>>()

    // Donnée publique observable (pour le Fragment)
    val books: LiveData<List<Book>> get() = _books

    // Chargement des livres au lancement du ViewModel
    init {
        loadBooks()
    }

    // Fonction pour charger (ou recharger) la liste des livres
    fun loadBooks() {
        viewModelScope.launch {
            val list = bookRepository.getAllBooks()
            _books.postValue(list) // postValue pour éviter les bugs si appelé hors du thread UI
        }
    }

    /**
     * Filtre par catégorie
     */
    private val _genres = MutableLiveData<List<String>>()
    val genres: LiveData<List<String>> get() = _genres

    fun loadGenres() {
        viewModelScope.launch {
            val list = bookRepository.getAllGenres()
            _genres.postValue(list)
        }
    }

    fun filterByCategory(category: String) {
        viewModelScope.launch {
            val result = bookRepository.getBooksByCategory(category)
            _books.postValue(result)
        }
    }

    /**
     * Filtre par score de livre
     */
    fun filterByScore(minScore: Double) {
        viewModelScope.launch {
            val result = bookRepository.getBooksByMinimumScore(minScore)
            _books.postValue(result)
        }
    }

    /**
     * Recherche par mot-clé
     */
    fun searchByKeyword(keyword: String ){
        viewModelScope.launch {
            val result = bookRepository.getBooksByKeyword(keyword)
            _books.postValue(result)
        }
    }


}
