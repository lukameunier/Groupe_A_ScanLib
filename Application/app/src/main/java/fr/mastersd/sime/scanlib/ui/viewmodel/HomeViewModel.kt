package fr.mastersd.sime.scanlib.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookRepository
import fr.mastersd.sime.scanlib.data.FavoriteGroup
import fr.mastersd.sime.scanlib.data.FilterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _genres = MutableStateFlow<List<String>>(emptyList())
    val genres: StateFlow<List<String>> = _genres.asStateFlow()

    private val _years = MutableStateFlow<List<String>>(emptyList())
    val years: StateFlow<List<String>> = _years.asStateFlow()

    private val _scores = MutableStateFlow<List<Double>>(emptyList())
    val scores: StateFlow<List<Double>> = _scores.asStateFlow()

    private val _filters = MutableStateFlow(FilterState())
    val filters: StateFlow<FilterState> = _filters.asStateFlow()

    private val _groups = MutableStateFlow<List<FavoriteGroup>>(emptyList())
    val groups: StateFlow<List<FavoriteGroup>> = _groups.asStateFlow()

    private val _currentGroupName = MutableStateFlow("Tous")
    val currentGroupName: StateFlow<String> = _currentGroupName.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _books.value = bookRepository.getAllBooks()
            _currentGroupName.value = "Tous"
        }
    }

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

    fun deleteBooks(books: List<Book>) {
        viewModelScope.launch {
            bookRepository.deleteBooks(books)
            loadBooks()
        }
    }

    fun updateFilter(newFilter: FilterState) {
        _filters.value = newFilter
    }

    fun resetFilters() {
        _filters.value = FilterState()
    }

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

            _books.value = if (filters.sortByDateAjout)
                filtered.sortedByDescending { it.dateAjout }
            else
                filtered
        }
    }

    fun loadGroups() {
        viewModelScope.launch {
            _groups.value = bookRepository.getAllFavoriteGroups()
        }
    }

    fun filterByGroup(groupId: Long) {
        viewModelScope.launch {
            _books.value = bookRepository.getBooksByGroup(groupId)
            val group = _groups.value.find { it.id == groupId }
            if (group != null) _currentGroupName.value = group.name
        }
    }

    fun addGroupLocally(group: FavoriteGroup) {
        val current = _groups.value.toMutableList()
        if (current.none { it.id == group.id }) {
            current.add(group)
            _groups.value = current
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            bookRepository.deleteGroup(groupId)
            loadGroups()
        }
    }

    fun renameGroup(groupId: Long, newName: String) {
        viewModelScope.launch {
            bookRepository.renameGroup(groupId, newName)
            loadGroups()
        }
    }

    fun checkOrCreateGroup(name: String, callback: (FavoriteGroup) -> Unit) {
        viewModelScope.launch {
            val existing = bookRepository.findGroupByName(name)
            if (existing != null) {
                callback(existing)
            } else {
                val id = bookRepository.insertFavoriteGroup(FavoriteGroup(name = name))
                callback(FavoriteGroup(id, name))
            }
        }
    }
}
