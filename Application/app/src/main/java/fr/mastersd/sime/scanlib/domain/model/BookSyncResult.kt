package fr.mastersd.sime.scanlib.domain.model

data class BookSyncResult(
    val foundBooks: List<Book>,
    val notFoundTitles: List<String>
)