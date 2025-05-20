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

            val book = service.searchBook(result.title, result.author)

            if (book == null) {
                println("Aucun livre trouvé.")
                notFoundTitles.add("${result.title} - ${result.author}")
            } else {
                println("🔢 ID                : ${book.id}")
                println("🔖 ETag              : ${book.etag}")
                println("🔗 Self Link         : ${book.selfLink}")

                println("📘 Titre             : ${book.title}")
                println("📚 Sous-titre        : ${book.subtitle ?: "Non renseigné"}")
                println("👤 Auteur(s)         : ${book.authors.joinToString()}")
                println("🏢 Éditeur           : ${book.publisher ?: "Non renseigné"}")
                println("📅 Date de pub.      : ${book.publishedDate ?: "Inconnue"}")
                println("📝 Description        : ${book.description?.take(150) ?: "Aucune"}...")
                println("🔖 ISBN/OCLC         : ${book.industryIdentifiers?.joinToString { "${it.type}:${it.identifier}" } ?: "Non disponible"}")
                println("📄 Nombre de pages   : ${book.pageCount ?: "Inconnu"}")
                println("🖨️ Type d'impression : ${book.printType}")
                println("🏷️ Catégories        : ${book.categories?.joinToString() ?: "Aucune"}")
                println("⭐ Note moyenne       : ${book.averageRating ?: "Aucune"} (${book.ratingsCount ?: 0} avis)")
                println("🔞 Maturité          : ${book.maturityRating}")
                println("🕵️ Anonyme logging   : ${book.allowAnonLogging}")
                println("🆚 Version contenu    : ${book.contentVersion}")
                println("📖 Lecture texte/img : texte=${book.readingModes?.text} | image=${book.readingModes?.image}")
                println("🧩 Panel EPUB/Image   : EPUB=${book.panelizationSummary?.containsEpubBubbles} | Image=${book.panelizationSummary?.containsImageBubbles}")
                println("🌐 Langue            : ${book.language}")

                println("🖼️ Couverture         : ${book.thumbnailUrl ?: "Aucune"}")
                println("🖼️ Miniature          : ${book.smallThumbnailUrl ?: "Aucune"}")
                println("🔍 Lien aperçu        : ${book.previewLink}")
                println("ℹ️ Lien info          : ${book.infoLink}")
                println("📘 Lien canonique     : ${book.canonicalVolumeLink}")

                println("💶 eBook ?            : ${book.isEbook}")
                println("💸 Statut de vente    : ${book.saleability}")
                println("🛒 Prix catalogue     : ${book.listPrice?.amount} ${book.listPrice?.currencyCode}")
                println("💵 Prix actuel        : ${book.retailPrice?.amount} ${book.retailPrice?.currencyCode}")
                println("🔗 Lien d'achat       : ${book.buyLink ?: "Non disponible"}")

                println("🔓 Vue possible       : ${book.viewability}")
                println("🔗 Embeddable         : ${book.embeddable}")
                println("📖 Domaine public     : ${book.publicDomain}")
                println("🗣️ TTS autorisé       : ${book.textToSpeechPermission}")
                println("📘 EPUB dispo         : ${book.epubAvailable}")
                println("📥 Lien EPUB Token    : ${book.epubTokenLink ?: "Aucun"}")
                println("📘 PDF dispo          : ${book.pdfAvailable}")
                println("📥 Lien PDF Token     : ${book.pdfTokenLink ?: "Aucun"}")
                println("🔎 WebReader Link     : ${book.webReaderLink}")
                println("🔍 View Status        : ${book.accessViewStatus}")
                println("🔗 Citation autorisée : ${book.quoteSharingAllowed}")

                println("🧠 Extrait            : ${book.textSnippet ?: "Non disponible"}")

                foundBooks.add(book)
            }

            println("---------------------------------------------------------")

        }

    return BookSyncResult(foundBooks, notFoundTitles)
    }
}