package fr.mastersd.sime.scanlib.data

import androidx.room.*
import org.tensorflow.lite.support.label.Category

/**
 * Data Access Object (DAO) pour la table des livres dans Room
 *
 * Définit les opérations de base pour accéder à [BookEntity] dans la bd locale
 *
 * @see BookDatabase pour l’accès à la base
 * @see BookEntity pour le modèle stocké
 * @see BookRepository pour l’usage métier des méthodes DAO
 */
@Dao
interface BookDao {

    /* Opérations de base */
    /**
     * Insère une liste de livre dans la bd locale, remplace un livre existant
     *
     * @param books Liste de livres à insérer
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    /**
     * Récupère tous les livres stockés localement
     *
     * @return Une liste de tous les livres de la bd
     */
    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<Book>

    /**
     * Supprime tous les livres
     *
     * Utilisé lors des réinitialisations ou syschronisations
     */
    @Query("DELETE FROM books")
    suspend fun clearAll()

    //==================================================================================================================//
    /* Filtrages */

    /**
     * Filtre par catégorie
     *
     * @param category Catégorie selctionnée
     * @return Liste de livres de la catégorie selectionnée
     */
    @Query("SELECT * FROM books WHERE categories LIKE '%' || :category || '%'")
    suspend fun getBooksByCategory(category: String): List<Book>

    /**
     * Filtre par score
     *
     * @param minScore Note Moyenne selectionné
     * @return Liste de livre ayant un score suppérieur ou égale au score selectionné
     */
    @Query("SELECT * FROM books WHERE averageRating >= :minScore")
    suspend fun getBooksByMinimumScore(minScore: Double): List<Book>

    @Query("SELECT * FROM books WHERE averageRating IS NULL")
    suspend fun getBooksByNoScore(): List<Book>

    /**
     * Filtre par année d'édition
     *
     * @param year Date de publication selctionnée
     * @returnLiset de livre de l'année d'édition
     */
    @Query("SELECT * FROM books WHERE publishedDate LIKE :year || '%'")
    suspend fun getBooksByYear(year: String): List<Book>

    /**
     * Filtre par année d'ajout ?
     */

    //==================================================================================================================//
    /**
     * Recherche par mot-clé: titre ou auteur
     *
     * @param keyword Mot-clé tappé dans la barre de recherche
     * @return Liste de livre qui correspondent au mot-clé
     */
    @Query("SELECT * FROM books WHERE title LIKE '%' || :keyword || '%' OR authors LIKE '%' || :keyword || '%'")
    suspend fun getBooksByKeyword(keyword: String): List<Book>

    /**
     * Filtre par date d'ajout
     */
    @Query("SELECT * FROM books ORDER BY dateAjout DESC")
    suspend fun getBooksSortedByDateAjout(): List<Book>


    //==================================================================================================================//
    /* Mise à Jour ciblée */
    /**
     * Mise à jour des champs d'un livre
     *
     * Nécessite un controle par formulaire pour mettre à jour uniquement les champs modifié et non tout le livre
     *
     * @return Le livre avec les champs modifiés
     */
    @Update
    suspend fun updateBook(book: Book)

    //==================================================================================================================//
    /* Suppression ciblée */
    /**
     * Suppression d'un livre basé sur son id
     *
     * @param bookId Id de base du livre (récupéré de l'API)
     */
    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: String)

    /**
     * Récupèrer toutes les catégories de la bd
     * DISTINCT ne marche pas car List<String>
     *
     * @return Liste de catégories
     */
    @Query("SELECT DISTINCT categories FROM books")
    suspend fun getAllCategories(): List<String>

    @Delete
    suspend fun deleteBooks(books: List<Book>)

    //==================================================================================
    /* Favoris */
    @Query("SELECT * FROM books WHERE isFavorite = 1")
    suspend fun getFavoriteBooks(): List<Book>

    @Query("UPDATE books SET isFavorite = :favorite WHERE id = :bookId")
    suspend fun updateFavoriteStatus(bookId: String, favorite: Boolean)

}