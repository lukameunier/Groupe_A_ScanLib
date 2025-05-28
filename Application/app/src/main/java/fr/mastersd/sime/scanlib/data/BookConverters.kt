package fr.mastersd.sime.scanlib.data

import androidx.room.TypeConverter

/**
 * Classe pour les conversions de types non pris en charge nativement par Room
 *
 * Convertit une List<String> en String (et inversement) pour permettre leur persistance dans SQLite
 *
 * ! A évaluer : peut être retirée si les champs ne sont plus nécessaires
 */
class BookConverters {

    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(", ")?.map { it.trim() } ?: emptyList()
    }

    @TypeConverter
    fun listToString(list: List<String>?): String {
        return list?.joinToString(", ") ?: ""
    }

//================================================================================
//================================================================================
// !: utiliser un encodage plus robuste pour des pb de ',' deja existant ===> JSON
// gson.fronJson, gson.toJson
//================================================================================
//================================================================================
}