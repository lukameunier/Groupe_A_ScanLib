package fr.mastersd.sime.scanlib.data.local

import androidx.room.*
/*
 * Data Access Object (DAO) pour la table des livres dans Room
 *
 * Définit les opérations de base pour accéder à [BookEntity] dans la bd locale
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

    //==================================================================================
    //==================================================================================
    // méthode de mise à jour partielle @Update --> Modifications du user
    // suppression ciblée @Delete --> Supression par user
    //==================================================================================
    //==================================================================================
}