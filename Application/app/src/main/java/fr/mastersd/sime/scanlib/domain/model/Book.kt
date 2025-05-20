package fr.mastersd.sime.scanlib.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: String,
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val pageCount: Int,

    // Identifiants standards
    val industryIdentifiers: List<String>?,

    // Lecture et affichage
    val readingModesText: Boolean,
    val readingModesImage: Boolean,
    val printType: String?,
    val categories: List<String>?,

    // Évaluation
    val averageRating: Double?,
    val ratingsCount: Int?,

    // Divers
    val maturityRating: String?,
    val allowAnonLogging: Boolean,
    val contentVersion: String?,
    val language: String?,

    // Liens
    val thumbnailUrl: String?,
    val smallThumbnailUrl: String?,
    val previewLink: String?,
    val infoLink: String?,
    val canonicalVolumeLink: String?,

    // Informations de vente
    val country: String?,
    val saleability: String?,
    val isEbook: Boolean,
    val listPrice: Double?,
    val retailPrice: Double?,
    val currencyCode: String?,
    val buyLink: String?,

    // Accès
    val viewability: String?,
    val embeddable: Boolean,
    val publicDomain: Boolean,
    val textToSpeechPermission: String?,
    val epubAvailable: Boolean,
    val pdfAvailable: Boolean,
    val webReaderLink: String?,
    val accessViewStatus: String?,
    val quoteSharingAllowed: Boolean,

    // Résultat de recherche
    val textSnippet: String?
) : Parcelable