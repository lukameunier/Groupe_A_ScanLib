package fr.mastersd.sime.scanlib.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.data.BookRepository
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
}
