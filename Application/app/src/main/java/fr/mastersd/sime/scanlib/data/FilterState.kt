package fr.mastersd.sime.scanlib.data

enum class SortBy {
    DATE_ADDED,
    TITLE
}

data class FilterState(
    val category: String? = null,
    val minScore: Double? = null,
    val scoreUnknown: Boolean = false,
    val year: String? = null,
    val sortBy: SortBy? = null
)

