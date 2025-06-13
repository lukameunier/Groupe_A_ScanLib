package fr.mastersd.sime.scanlib.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookRepository
import fr.mastersd.sime.scanlib.data.FavoriteGroup
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {
    fun updateBook(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    val groups = MutableLiveData<List<FavoriteGroup>>()

    fun loadGroups() {
        viewModelScope.launch {
            val loadedGroups = repository.getAllFavoriteGroups()
            groups.postValue(loadedGroups)
        }
    }

    fun addBookToGroup(bookId: String, groupId: Long) {
        viewModelScope.launch {
            repository.addBookToGroup(bookId, groupId)
        }
    }

    //===============================================================
    val bookGroupIds = MutableLiveData<List<Long>>() // Les groupes du livre

    fun loadBookGroupIds(bookId: String) {
        viewModelScope.launch {
            val ids = repository.getGroupIdsForBook(bookId)
            bookGroupIds.postValue(ids)
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
