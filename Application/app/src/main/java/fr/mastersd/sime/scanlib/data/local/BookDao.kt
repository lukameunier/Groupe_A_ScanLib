package fr.mastersd.sime.scanlib.data.local

import androidx.room.*

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("DELETE FROM books")
    suspend fun clearAll()
}