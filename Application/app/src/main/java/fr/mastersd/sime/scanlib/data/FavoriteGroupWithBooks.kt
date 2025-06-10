package fr.mastersd.sime.scanlib.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

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

