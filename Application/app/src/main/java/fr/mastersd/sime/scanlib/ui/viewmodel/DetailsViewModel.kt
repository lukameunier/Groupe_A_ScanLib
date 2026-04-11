package fr.mastersd.sime.scanlib.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookRepository
import fr.mastersd.sime.scanlib.data.FavoriteGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _groups = MutableStateFlow<List<FavoriteGroup>>(emptyList())
    val groups: StateFlow<List<FavoriteGroup>> = _groups.asStateFlow()

    private val _bookGroupIds = MutableStateFlow<List<Long>>(emptyList())
    val bookGroupIds: StateFlow<List<Long>> = _bookGroupIds.asStateFlow()

    fun updateBook(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    fun loadGroups() {
        viewModelScope.launch {
            _groups.value = repository.getAllFavoriteGroups()
        }
    }

    fun loadBookGroupIds(bookId: String) {
        viewModelScope.launch {
            _bookGroupIds.value = repository.getGroupIdsForBook(bookId)
        }
    }

    fun addBookToGroup(bookId: String, groupId: Long) {
        viewModelScope.launch {
            repository.addBookToGroup(bookId, groupId)
        }
    }

    fun removeBookFromGroup(bookId: String, groupId: Long) {
        viewModelScope.launch {
            repository.removeBookFromGroup(bookId, groupId)
        }
    }

    fun checkOrCreateGroup(name: String, callback: (FavoriteGroup) -> Unit) {
        viewModelScope.launch {
            val existing = repository.findGroupByName(name)
            if (existing != null) {
                callback(existing)
            } else {
                val id = repository.insertFavoriteGroup(FavoriteGroup(name = name))
                callback(FavoriteGroup(id, name))
            }
        }
    }
}
