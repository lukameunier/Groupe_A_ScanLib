import java.io.File
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanFileReader {

    fun readScanResults(filePath: String): List<ScanResult> {
        val results = mutableListOf<ScanResult>()

        File(filePath).forEachLine { line ->
            val parts = line.split(";") //separateur de découpage
            if (parts.size >= 2) {
                val title = parts[0].trim()
                val author = parts[1].trim()
                results.add(ScanResult(title, author))
            }
        }

        return results
    }
}

/*
fun searchBooksFromFile(service: GoogleBooksService, filePath: String) {
    val lines = File(filePath).readLines().filter { it.isNotBlank() }

    for (line in lines) {
        val parts = line.split(";") //separateur de découpage
        if (parts.size == 2) {
            val title = parts[0].trim()
            val author = parts[1].trim()
            val query = "intitle:$title+inauthor:$author" //creer la requete Google Books : https://www.googleapis.com/books/v1/volumes?q=intitle:$title+inauthor:$author

            val call = service.searchBook(query)

            call.enqueue(object : Callback<GoogleBooksResponse> { //envoie la requete de maniere non bloquante
                override fun onResponse(call: Call<GoogleBooksResponse>, response: Response<GoogleBooksResponse>) { //recuperer et afficher les livres
//                    println("🔎 Réponse JSON brute :")
//                    println(response.body())

                    val book = response.body()?.items?.firstOrNull()?.volumeInfo
                    if (book != null) {
                        println("📘 ${book.title} | ${book.authors?.joinToString()} | ${book.publisher} (${book.publishedDate})")
                    } else {
                        println("❌ Aucun résultat pour : $title de $author")
                    }
                }

                override fun onFailure(call: Call<GoogleBooksResponse>, t: Throwable) { //afficher l'erreur
                    println("⚠️ Erreur lors de la requête : ${t.message}")
                }
            })
        }
    }

    // attendre que les requêtes async finissent
    Thread.sleep(10000)

}
*/