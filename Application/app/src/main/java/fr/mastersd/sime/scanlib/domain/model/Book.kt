package fr.mastersd.sime.scanlib.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
 * Modèle métier d'un livre récupéré via API Google Books
 *
 * Utilisée pour manipuler les data livre indépendament de la bd locale
 *
 * [Parcelable] pour le passage entre fragments
 */

//==================================================================================
//==================================================================================
    // /*!*/ --> Sera supprimé plus tard
    // /*?*/ --> Peut être utile ?
//==================================================================================
//==================================================================================
@Parcelize
data class Book(
    val id: String, //ID unique du volume (retoruné par Google Books)

    val title: String, //titre
    /*!*/ val subtitle: String?, //sous-titre -> collections/tomes
    val authors: List<String>, //auteurs
    val publisher: String?, //maison d'édition
    val publishedDate: String?, //date de publication: AAAA/AAAA-MM-JJ
    val description: String?, //résumé, description
    val pageCount: Int, //nombre de page


    val industryIdentifiers: List<String>?, //identifiants: ISBN-10/ISBN-13/OCLC

    /* Lecture et affichage */
    /*!*/ val readingModesText: Boolean, //lisible numériquement ?(=ou non)
    /*!*/ val readingModesImage: Boolean, //dispo format image ?
    /*?*/ val printType: String?, //type d'impression : BOOK, MAGAZINE
    val categories: List<String>?, //catégories : thèmes, genres


    /* Évaluation */
    val averageRating: Double?, //note moyenne par les user
    val ratingsCount: Int?, //nombre de votes


    /*!*/ /* Divers --> Section à supprimer */
    val maturityRating: String?, //maturité: NOT_MATURE, MATURE
    val allowAnonLogging: Boolean, //anonyme autorisé ?
    val contentVersion: String?, //version du contenu
    val language: String?, //langue


    /* Liens */
    val thumbnailUrl: String?, //url de la couverture
    /*?*/ val smallThumbnailUrl: String?, //url small couverture
    val previewLink: String?, //lien aperçu
    val infoLink: String?, //lien fiche d'info
    /*!*/ val canonicalVolumeLink: String?, //lien stable


    /* Informations de vente */
    /*?*/ val country: String?, //pays origine ou publication
    /*!*/ val saleability: String?, //status de vente: FOR_SALE, NOT_FOR_SALE,FREE
    /*!*/ val isEbook: Boolean, //format e-book ?
    /*!*/ val listPrice: Double?, //prix public
    /*!*/ val retailPrice: Double?, //prix actuel
    /*!*/ val currencyCode: String?, //devise du prix
    /*!*/ val buyLink: String?, //lien d'achat


    /*!*/ /* Accès ---> section à supprimer */
    val viewability: String?, //visibilité: NONE/PARTIAL/ALL_PAGES
    val embeddable: Boolean, //contenu intégrable (iframe) ?
    val publicDomain: Boolean, //dans domaine public ?
    val textToSpeechPermission: String?, //lecture audio autorisée ?
    val epubAvailable: Boolean, //format EPUB ?
    val pdfAvailable: Boolean, //format PDF ?
    val webReaderLink: String?, //lien lecture web
    val accessViewStatus: String?, //état accés vue complète: SAMPLE/FULL/NONE
    val quoteSharingAllowed: Boolean, //partage de citation autorisé ?


    /*?*/ val textSnippet: String? //extrait de recherche: mots-clés
) : Parcelable