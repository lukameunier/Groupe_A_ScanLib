data class IndustryIdentifier(
    val type: String,
    val identifier: String
)

data class ReadingModes(
    val text: Boolean,
    val image: Boolean
)

data class PanelizationSummary(
    val containsEpubBubbles: Boolean,
    val containsImageBubbles: Boolean
)

data class Price(
    val amount: Double,
    val currencyCode: String
)

data class Book(
    // Identifiants
    val id: String,
    val etag: String?,
    val selfLink: String?,

    // Infos bibliographiques
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val industryIdentifiers: List<IndustryIdentifier>?,
    val pageCount: Int?,
    val printType: String?,
    val categories: List<String>?,
    val averageRating: Double?,
    val ratingsCount: Int?,
    val maturityRating: String?,
    val allowAnonLogging: Boolean?,
    val contentVersion: String?,
    val readingModes: ReadingModes?,
    val panelizationSummary: PanelizationSummary?,
    val language: String?,

    // Liens
    val thumbnailUrl: String?,
    val smallThumbnailUrl: String?,
    val previewLink: String?,
    val infoLink: String?,
    val canonicalVolumeLink: String?,

    // Vente
    val isEbook: Boolean?,
    val saleability: String?,
    val listPrice: Price?,
    val retailPrice: Price?,
    val buyLink: String?,

    // Accès
    val viewability: String?,
    val embeddable: Boolean?,
    val publicDomain: Boolean?,
    val textToSpeechPermission: String?,
    val epubAvailable: Boolean?,
    val epubTokenLink: String?,
    val pdfAvailable: Boolean?,
    val pdfTokenLink: String?,
    val webReaderLink: String?,
    val accessViewStatus: String?,
    val quoteSharingAllowed: Boolean?,

    // Snippet
    val textSnippet: String?
)
