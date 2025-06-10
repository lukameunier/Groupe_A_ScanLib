package fr.mastersd.sime.scanlib.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookRepository
import fr.mastersd.sime.scanlib.data.FavoriteGroup
import fr.mastersd.sime.scanlib.data.FilterState
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    // Donnée privée modifiable
    private val _books = MutableLiveData<List<Book>>()

    // Donnée publique observable (pour le Fragment)
    val books: LiveData<List<Book>> get() = _books

    private val _genres = MutableLiveData<List<String>>()
    val genres: LiveData<List<String>> get() = _genres

    private val _years = MutableLiveData<List<String>>()
    val years: LiveData<List<String>> get() = _years

    private val _scores = MutableLiveData<List<Double>>()
    val scores: LiveData<List<Double>> get() = _scores

    // État actuel des filtres (observable)
    private val _filters = MutableLiveData(FilterState())
    val filters: LiveData<FilterState> get() = _filters

    // Chargement des livres au lancement du ViewModel
    init {
        loadBooks()
    }

    // Fonction pour charger (ou recharger) la liste des livres
    fun loadBooks() {
        viewModelScope.launch {
            val list = bookRepository.getAllBooks()
            _books.postValue(list)
        }
    }

    /**
     * Chargement des genres disponibles (à afficher dans les filtres)
     */
    fun loadGenres() {
        viewModelScope.launch {
            val list = bookRepository.getAllGenres()
            _genres.postValue(list)
        }
    }

    fun loadYears() {
        viewModelScope.launch {
            val list = bookRepository.getAllYears()
            _years.postValue(list)
        }
    }

    fun loadScores() {
        viewModelScope.launch {
            val list = bookRepository.getAllScores()
            _scores.postValue(list)
        }
    }

    /**
     * Recherche par mot-clé (titre ou auteur)
     */
    fun searchByKeyword(keyword: String) {
        viewModelScope.launch {
            val result = bookRepository.getBooksByKeyword(keyword)
            _books.postValue(result)
        }
    }

    fun deleteBooks(books: List<Book>) {
        viewModelScope.launch {
            bookRepository.deleteBooks(books)
            loadBooks()
        }
    }

    /**
     * Mise à jour des filtres à partir du fragment
     */
    fun updateFilter(newFilter: FilterState) {
        _filters.value = newFilter
    }

    /**
     * Réinitialisation de tous les filtres
     */
    fun resetFilters() {
        _filters.value = FilterState()
    }

    /**
     * Filtrage combiné par genre + score + année
     *
     * Appliquer les filtres combinés à partir du FilterState
     */
     fun applyCombinedFilters(filters: FilterState) {
        viewModelScope.launch {
            val books = bookRepository.getAllBooks()

            val filtered = books.filter { book ->
                val categoryMatch = filters.category?.let { cat ->
                    book.categories?.any { it.contains(cat, ignoreCase = true) } ?: false
                } != false

                val scoreMatch = when {
                    filters.scoreUnknown -> book.averageRating == null
                    filters.minScore != null -> book.averageRating?.let { it >= filters.minScore } == true
                    else -> true
                }

                val yearMatch = when {
                    filters.year != null -> book.publishedDate?.startsWith(filters.year) == true
                    else -> true
                }

                categoryMatch && scoreMatch && yearMatch
            }

            val finalResult = if (filters.sortByDateAjout)
                filtered.sortedByDescending { it.dateAjout }
            else
                filtered

            _books.postValue(finalResult)
        }
    }

    private val _groups = MutableLiveData<List<FavoriteGroup>>()
    val groups: LiveData<List<FavoriteGroup>> get() = _groups

    fun loadGroups() {
        viewModelScope.launch {
            val list = bookRepository.getAllFavoriteGroups()
            _groups.postValue(list)
        }
    }

    fun filterByGroup(groupId: Long) {
        viewModelScope.launch {
            val books = bookRepository.getBooksByGroup(groupId)
            _books.postValue(books)
        }
    }

}
