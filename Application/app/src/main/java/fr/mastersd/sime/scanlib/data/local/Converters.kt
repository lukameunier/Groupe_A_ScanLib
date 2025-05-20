package fr.mastersd.sime.scanlib.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(", ")?.map { it.trim() } ?: emptyList()
    }

    @TypeConverter
    fun listToString(list: List<String>?): String {
        return list?.joinToString(", ") ?: ""
    }
}
