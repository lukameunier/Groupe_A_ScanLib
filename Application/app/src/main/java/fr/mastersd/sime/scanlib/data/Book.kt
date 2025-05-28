package fr.mastersd.sime.scanlib.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Modèle métier d'un livre récupéré via API Google Books
 *
 * Utilisée pour manipuler les data livre indépendament de la bd locale
 *
 * [Parcelable] pour le passage entre fragments
 */

@Entity(tableName = "books")
@Parcelize
data class Book(
    @PrimaryKey val id: String, //ID unique du volume (retourné par Google Books)
    val title: String, //titre
    val authors: List<String>, //auteurs
    val publisher: String?, //maison d'édition
    val publishedDate: String?, //date de publication: AAAA/AAAA-MM-JJ
    val description: String?, //résumé, description
    val pageCount: Int, //nombre de page
    val industryIdentifiers: List<String>?, //identifiants: ISBN-10/ISBN-13/OCLC
    val categories: List<String>?, //catégories : thèmes, genres
    val averageRating: Double?, //note moyenne par les user
    val ratingsCount: Int?, //nombre de votes
    val thumbnailUrl: String?, //url de la couverture
    /*?*/ val smallThumbnailUrl: String?, //url small couverture
    val previewLink: String?, //lien aperçu
    val infoLink: String?, //lien fiche d'info
    /*?*/ val country: String?, //pays origine ou publication
    /*?*/ val textSnippet: String? //extrait de recherche: mots-clés
) : Parcelable