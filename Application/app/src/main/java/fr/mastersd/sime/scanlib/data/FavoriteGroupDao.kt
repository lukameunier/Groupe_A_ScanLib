package fr.mastersd.sime.scanlib.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FavoriteGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: FavoriteGroup): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(ref: BookGroupCrossRef)

    @Transaction
    @Query("SELECT * FROM favorite_groups")
    suspend fun getAllGroupsWithBooks(): List<FavoriteGroupWithBooks>

    @Query("""
        SELECT books.* FROM books
        INNER JOIN bookgroupcrossref ON books.id = bookgroupcrossref.bookId
        WHERE bookgroupcrossref.groupId = :groupId
    """)
    suspend fun getBooksByGroup(groupId: Long): List<Book>
}
