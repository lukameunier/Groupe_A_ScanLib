data class BookSyncResult(
    val foundBooks: List<Book>,
    val notFoundTitles: List<String>
)
