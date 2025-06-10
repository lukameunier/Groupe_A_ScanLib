package fr.mastersd.sime.scanlib.data

import androidx.room.Entity

@Entity(primaryKeys = ["bookId", "groupId"])
data class BookGroupCrossRef(
    val bookId: String,
    val groupId: Long
)
