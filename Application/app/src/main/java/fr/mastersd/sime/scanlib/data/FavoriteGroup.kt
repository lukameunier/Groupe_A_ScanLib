package fr.mastersd.sime.scanlib.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_groups")
data class FavoriteGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

