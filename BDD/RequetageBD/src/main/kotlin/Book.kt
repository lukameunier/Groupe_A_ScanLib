/* Un Livre avec les informations à afficher */
data class Book(
    val id: String,
    val title: String,
    val subtitle: String,
    val authors: List<String>,
    val publisher: String,
    val publishedDate: String,
    val description: String,
    val pageCount: Int,
    val thumbnailUrl: String?,
    val previewLink: String?,
    val infoLink: String?,
    val buyLink: String?
)
