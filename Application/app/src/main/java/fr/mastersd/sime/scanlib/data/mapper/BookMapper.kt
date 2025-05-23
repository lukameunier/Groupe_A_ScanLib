package fr.mastersd.sime.scanlib.data.mapper

import fr.mastersd.sime.scanlib.data.local.BookEntity
import fr.mastersd.sime.scanlib.domain.model.Book

/**
 * Mapper utilitaire responsable de la converstion entre les modèles de domaine [Book] et les entités de ma bd [BookEntity]
 *
 * Utilisé pour séparer la logique métier (domaine) et la persistance (bd): clean architecture
 *
 * @see Book pour la structure métier issue de l'API
 * @see BookEntity pour la structure adaptée à Room
 * @see BookRepositoryImpl pour son utilisation dans le flux de données
 */
object BookMapper {

    /**
     * Convertit un objet [Book] (modèle métier) en [BookEntity] (entité Room)
     *
     * A utiliser lors de stocke d'un livre récupéré depuis l'API dans la bd locale
     *
     * @param book Le modèle métier représentant un livre enrichi
     * @return L'entité correspondante à persister dans Room
     */
    fun toEntity(book: Book): BookEntity {
        return BookEntity(
            id = book.id,
            title = book.title,
            subtitle = book.subtitle,
            authors = book.authors,
            publisher = book.publisher,
            publishedDate = book.publishedDate,
            description = book.description,
            pageCount = book.pageCount,
            industryIdentifiers = book.industryIdentifiers,
            readingModesText = book.readingModesText,
            readingModesImage = book.readingModesImage,
            printType = book.printType,
            categories = book.categories,
            averageRating = book.averageRating,
            ratingsCount = book.ratingsCount,
            maturityRating = book.maturityRating,
            allowAnonLogging = book.allowAnonLogging,
            contentVersion = book.contentVersion,
            language = book.language,
            thumbnailUrl = book.thumbnailUrl,
            smallThumbnailUrl = book.smallThumbnailUrl,
            previewLink = book.previewLink,
            infoLink = book.infoLink,
            canonicalVolumeLink = book.canonicalVolumeLink,
            country = book.country,
            saleability = book.saleability,
            isEbook = book.isEbook,
            listPrice = book.listPrice,
            retailPrice = book.retailPrice,
            currencyCode = book.currencyCode,
            buyLink = book.buyLink,
            viewability = book.viewability,
            embeddable = book.embeddable,
            publicDomain = book.publicDomain,
            textToSpeechPermission = book.textToSpeechPermission,
            epubAvailable = book.epubAvailable,
            pdfAvailable = book.pdfAvailable,
            webReaderLink = book.webReaderLink,
            accessViewStatus = book.accessViewStatus,
            quoteSharingAllowed = book.quoteSharingAllowed,
            textSnippet = book.textSnippet
        )
    }

    /**
     * Convertit une entité [BookEntity] en modèle métier [Book]
     *
     * A utiliser lorsqu'on lit un livre stocké localement dans la bd, pour l'afficher dans l'interface ou réutiliser dans un code
     *
     * @param entity L'entité stockée en bd
     * @return Le modèle métier équivalent
     */
    fun fromEntity(entity: BookEntity): Book {
        return Book(
            id = entity.id,
            title = entity.title,
            subtitle = entity.subtitle,
            authors = entity.authors,
            publisher = entity.publisher,
            publishedDate = entity.publishedDate,
            description = entity.description,
            pageCount = entity.pageCount,
            industryIdentifiers = entity.industryIdentifiers,
            readingModesText = entity.readingModesText,
            readingModesImage = entity.readingModesImage,
            printType = entity.printType,
            categories = entity.categories,
            averageRating = entity.averageRating,
            ratingsCount = entity.ratingsCount,
            maturityRating = entity.maturityRating,
            allowAnonLogging = entity.allowAnonLogging,
            contentVersion = entity.contentVersion,
            language = entity.language,
            thumbnailUrl = entity.thumbnailUrl,
            smallThumbnailUrl = entity.smallThumbnailUrl,
            previewLink = entity.previewLink,
            infoLink = entity.infoLink,
            canonicalVolumeLink = entity.canonicalVolumeLink,
            country = entity.country,
            saleability = entity.saleability,
            isEbook = entity.isEbook,
            listPrice = entity.listPrice,
            retailPrice = entity.retailPrice,
            currencyCode = entity.currencyCode,
            buyLink = entity.buyLink,
            viewability = entity.viewability,
            embeddable = entity.embeddable,
            publicDomain = entity.publicDomain,
            textToSpeechPermission = entity.textToSpeechPermission,
            epubAvailable = entity.epubAvailable,
            pdfAvailable = entity.pdfAvailable,
            webReaderLink = entity.webReaderLink,
            accessViewStatus = entity.accessViewStatus,
            quoteSharingAllowed = entity.quoteSharingAllowed,
            textSnippet = entity.textSnippet
        )
    }
}
