package fr.mastersd.sime.scanlib.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface GroupBookDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroup(group: FavoriteGroup): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(ref: BookGroupCrossRef)

    /**
     * Récupère tous les groupes de favoris avec leurs livres associés.
     */
    @Transaction
    @Query("SELECT * FROM favorite_groups")
    suspend fun getAllGroups(): List<FavoriteGroupWithBooks>

    /**
     * Récupère tous les livres appartenant à un groupe de favoris donné.
     *
     * @param groupId ID du groupe favori
     * @return Liste des livres liés au groupe
     */
    @Query("""
        SELECT books.* FROM books
        INNER JOIN bookgroupcrossref ON books.id = bookgroupcrossref.bookId
        WHERE bookgroupcrossref.groupId = :groupId
    """)
    suspend fun getBooksByGroup(groupId: Long): List<Book>

    @Query("SELECT * FROM favorite_groups")
    suspend fun getAllGroupsSimple(): List<FavoriteGroup>

    @Query("DELETE FROM BookGroupCrossRef WHERE bookId = :bookId AND groupId = :groupId")
    suspend fun removeBookFromGroup(bookId: String, groupId: Long)

    @Query("SELECT groupId FROM BookGroupCrossRef WHERE bookId = :bookId")
    suspend fun getGroupIdsForBook(bookId: String): List<Long>

    @Query("DELETE FROM favorite_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)

    @Query("DELETE FROM BookGroupCrossRef WHERE groupId = :groupId")
    suspend fun removeAllBooksFromGroup(groupId: Long)

    @Query("UPDATE favorite_groups SET name = :newName WHERE id = :groupId")
    suspend fun renameGroup(groupId: Long, newName: String)

    @Query("SELECT * FROM favorite_groups WHERE name = :name LIMIT 1")
    suspend fun findGroupByName(name: String): FavoriteGroup?



}
