package fr.mastersd.sime.scanlib.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String, // ID de Google Books utilisé comme clé primaire

    val title: String,
    val subtitle: String?,
    val authors: String,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val pageCount: Int,
    val thumbnailUrl: String?,
    val previewLink: String?,
    val infoLink: String?,
    val buyLink: String?
)
