package fr.mastersd.sime.scanlib.data.mapper

import fr.mastersd.sime.scanlib.data.local.BookEntity
import fr.mastersd.sime.scanlib.domain.model.Book

object BookMapper {

    fun toEntity(book: Book): BookEntity {
        return BookEntity(
            id = book.id,
            title = book.title,
            subtitle = book.subtitle,
            authors = book.authors.joinToString(", "),
            publisher = book.publisher,
            publishedDate = book.publishedDate,
            description = book.description,
            pageCount = book.pageCount,
            thumbnailUrl = book.thumbnailUrl,
            previewLink = book.previewLink,
            infoLink = book.infoLink,
            buyLink = book.buyLink
        )
    }

    fun fromEntity(entity: BookEntity): Book {
        return Book(
            id = entity.id,
            title = entity.title,
            subtitle = entity.subtitle ?: "",
            authors = entity.authors.split(", ").filter { it.isNotBlank() },
            publisher = entity.publisher ?: "",
            publishedDate = entity.publishedDate ?: "",
            description = entity.description ?: "",
            pageCount = entity.pageCount,
            thumbnailUrl = entity.thumbnailUrl,
            previewLink = entity.previewLink,
            infoLink = entity.infoLink,
            buyLink = entity.buyLink
        )
    }
}