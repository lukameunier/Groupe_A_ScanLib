package fr.mastersd.sime.scanlib.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookRepository
import fr.mastersd.sime.scanlib.data.FilterState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    // ==================== Données observables ====================

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _genres = MutableStateFlow<List<String>>(emptyList())
    val genres: StateFlow<List<String>> = _genres.asStateFlow()

    private val _years = MutableStateFlow<List<String>>(emptyList())
    val years: StateFlow<List<String>> = _years.asStateFlow()

    private val _scores = MutableStateFlow<List<Double>>(emptyList())
    val scores: StateFlow<List<Double>> = _scores.asStateFlow()

    // ==================== Filtres dynamiques ====================

    private val _filters = MutableStateFlow(FilterState())

    init {
        observeFilters()
        loadGenres()
        loadYears()
        loadScores()
    }

    /**
     * Observe les filtres et recharge les livres dès qu'ils changent
     */
    private fun observeFilters() {
        _filters
            .onEach { applyCombinedFilters(it) }
            .launchIn(viewModelScope)
    }

    /**
     * Charger les livres en base sans filtre
     */
    fun loadBooks() {
        viewModelScope.launch {
            _books.value = bookRepository.getAllBooks()
        }
    }

    // ==================== Chargements simples ====================

    fun loadGenres() {
        viewModelScope.launch {
            _genres.value = bookRepository.getAllGenres()
        }
    }

    fun loadYears() {
        viewModelScope.launch {
            _years.value = bookRepository.getAllYears()
        }
    }

    fun loadScores() {
        viewModelScope.launch {
            _scores.value = bookRepository.getAllScores()
        }
    }

    fun searchByKeyword(keyword: String) {
        viewModelScope.launch {
            _books.value = bookRepository.getBooksByKeyword(keyword)
        }
    }

    fun resetFilters() {
        _filters.value = FilterState()
    }

    // ==================== Mise à jour des filtres ====================

    fun updateFilter(newState: FilterState) {
        _filters.value = newState
    }

    fun filterByCategory(category: String) {
        _filters.update { it.copy(category = category) }
    }

    fun filterByScore(score: Double) {
        _filters.update { it.copy(minScore = score, scoreUnknown = false) }
    }

    fun filterByNoScore() {
        _filters.update { it.copy(minScore = null, scoreUnknown = true) }
    }

    fun filterByYear(year: String?) {
        _filters.update { it.copy(year = year, yearUnknown = year == null) }
    }

    // ==================== Application des filtres ====================

    private suspend fun applyCombinedFilters(filters: FilterState) {
        val books = bookRepository.getAllBooks()

        val filtered = books.filter { book ->
            val categoryMatch = filters.category?.let { cat ->
                book.categories?.any { it.contains(cat, ignoreCase = true) } ?: false
            } ?: true

            val scoreMatch = when {
                filters.scoreUnknown -> book.averageRating == null
                filters.minScore != null -> book.averageRating?.let { it >= filters.minScore } ?: false
                else -> true
            }

            val yearMatch = when {
                filters.yearUnknown -> book.publishedDate.isNullOrBlank()
                filters.year != null -> book.publishedDate?.startsWith(filters.year) ?: false
                else -> true
            }

            categoryMatch && scoreMatch && yearMatch
        }

        _books.value = filtered
    }
}
