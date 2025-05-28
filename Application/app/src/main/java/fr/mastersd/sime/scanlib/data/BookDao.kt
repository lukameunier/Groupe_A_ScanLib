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


    /* Filtrages */
    /**
     * Filtre par catégorie et genre
     *
     * @return Liste de livres de la catégorie selectionnée
     */
    @Query("SELECT * FROM books WHERE categories LIKE '%' || :category || '%'")
    suspend fun getBooksByCategory(category: String): List<Book>


    /**
     * Filtre par score (note moyenne)
     *
     * @return Liste de livre ayant un score suppérieur OU égale au score selectionné
     */
    @Query("SELECT * FROM books WHERE averageRating >= :minScore")
    suspend fun getBooksByMinimumScore(minScore: Double): List<Book>


    /**
     * Recherche par mot-clé: titre ou auteur
     *
     * @return Liste de livre qui correspondent au mot-clé
     */
    @Query("SELECT * FROM books WHERE title LIKE '%' || :keyword || '%' OR authors LIKE '%' || :keyword || '%'")
    suspend fun getBooksByKeyword(keyword: String): List<Book>

    /* Mise à Jour ciblée */
    //
    /**
     * Mise à jour des champs d'un livre
     *
     * Nécessite un controle par formulaire pour mettre à jour uniquement les champs modifié et non tout le livre
     *
     * @return Le livre avec les champs modifiés
     */
    @Update
    suspend fun updateBook(book: Book)


    /* Suppression ciblée */
    /**
     * Suppression d'un livre basé sur son id
     */
    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: String)


    //==================================================================================
    //==================================================================================
    // //?//: méthode de mise à jour partielle d'un champ
    //==================================================================================
    //==================================================================================
}