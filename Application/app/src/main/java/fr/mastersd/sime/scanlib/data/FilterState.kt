package fr.mastersd.sime.scanlib.data

data class FilterState(
    val category: String? = null,
    val minScore: Double? = null,
    val scoreUnknown: Boolean = false,
    val year: String? = null,
//    val yearUnknown: Boolean = false
)
