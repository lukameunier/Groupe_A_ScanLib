package fr.mastersd.sime.scanlib.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Index
import androidx.room.Junction
import androidx.room.Relation

/**
 * Table de croisement pour la relation N-N entre Book et FavoriteGroup
 */
@Entity(primaryKeys = ["bookId", "groupId"])
data class BookGroupCrossRef(
    val bookId: String,
    val groupId: Long
)

/**
 * Entité représentant un groupe
 */
@Entity(
    tableName = "favorite_groups",
    indices = [Index(value = ["name"], unique = true)]
)
data class FavoriteGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)


/**
 * Relation entre un groupe de favoris et la liste de livres qui lui sont associés
 */
data class FavoriteGroupWithBooks(
    @Embedded val group: FavoriteGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookGroupCrossRef::class,
            parentColumn = "groupId",
            entityColumn = "bookId"
        )
    )
    val books: List<Book>
)
