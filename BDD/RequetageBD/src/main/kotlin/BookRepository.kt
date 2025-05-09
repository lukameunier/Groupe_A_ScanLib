class BookRepository(
    private val reader: ScanFileReader,
    private val service: GoogleBooksService
) {
    fun syncBooksFromScanFile(filePath: String): BookSyncResult {
        val scanResults = reader.readScanResults(filePath)
        val foundBooks = mutableListOf<Book>()
        val notFoundTitles = mutableListOf<String>()

        for (result in scanResults) {
            println("Recherche du livre : ${result.title} de ${result.author}")

            val books = service.searchBook(result.title, result.author)

            if (books.isEmpty()) {
                println("Aucun livre trouvé.")
                notFoundTitles.add("${result.title} - ${result.author}")
            } else {
                println("${books.size} édition(s) trouvée(s) :")

                books.forEachIndexed { index, book ->
                    println("🗂️ Édition ${index + 1}")
                    println("📘 Titre         : ${book.title}")
                    println("👤 Auteur(s)     : ${book.authors.joinToString()}")
                    println("🏢 Éditeur       : ${book.publisher ?: "Éditeur inconnu"}")
                    println("📅 Date de pub.  : ${book.publishedDate}")
                    println("🔗 Lien          : ${book.infoLink}")
                    println("🖼️ Couverture    : ${book.thumbnailUrl ?: "Pas d'image disponible"}")
                    println()
                }

                // Ajout de toutes les éditions dans le résultat
                foundBooks.addAll(books)
            }

            println("---------------------------------------------------------")
        }

    return BookSyncResult(foundBooks, notFoundTitles)
    }
}
/*
println("Résultat(s) trouvé(s) :")
books.take(3).forEach { book -> //prend 3
    println("📘 Titre         : ${book.title}")
    println("👤 Auteur(s)     : ${book.authors.joinToString()}")
    println("🏢 Éditeur       : ${book.publisher ?: "Éditeur inconnu"}")
    println("📅 Date de pub.  : ${book.publishedDate}")
    println("🔗 Lien          : ${book.infoLink}")
    println("🖼️ Couverture    : ${book.thumbnailUrl ?: "Pas d'image disponible"}")
}
 */