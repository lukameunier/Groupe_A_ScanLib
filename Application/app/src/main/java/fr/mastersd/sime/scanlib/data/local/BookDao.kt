package fr.mastersd.sime.scanlib.data.local

import androidx.room.*
/**
 * Data Access Object (DAO) pour la table des livres dans Room
 *
 * Définit les opérations de base pour accéder à [BookEntity] dans la bd locale
 *
 * @see BookDatabase pour l’accès à la base
 * @see BookEntity pour le modèle stocké
 * @see BookRepositoryImpl pour l’usage métier des méthodes DAO
 */
@Dao
interface BookDao {

    /*
      * Insère une liste de livre dans la bd locale, remplace un livre existant
      *
      * @param books Liste de livres à insérer
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)


    /*
      * Récupère tous les livres stockés localement
      *
      * @return Une liste de tous les livres de la bd
     */
    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<BookEntity>


    /*
      * Supprime tous les livres
      *
      * Utilisé lors des réinitialisations ou syschronisations
     */
    @Query("DELETE FROM books")
    suspend fun clearAll()

    //=====================================================================================================================================================
    //=====================================================================================================================================================
    // !: mise à jour individuelle [@Update] ---> updateBook
    // !: suppression ciblée [@Delete] ---> deleteById, deleteAllBut
    // !: chercher par title, author ---> mot-clé
    // !: trier par genre, rating, date ---> getBooksByGenre, getBooksByScore, getBooksByDate => appliquer les filtres dans Repository, les appeler dans VM
    // ?: trier les livres par date d'ajout ---> add un champ dateAjout à [BookEntity] + getAllBooksByDate
    // ?: tests unitaires ---> peut être
    //======================================================================================================================================================
    //======================================================================================================================================================
}