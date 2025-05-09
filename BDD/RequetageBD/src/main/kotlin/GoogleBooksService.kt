import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request

class GoogleBooksService {

    private val client = OkHttpClient()
    private val gson = Gson()

    fun searchBook(title: String, author: String): List<Book> {
        val books = mutableListOf<Book>()
        val query = "intitle:$title+inauthor:$author"
        val url = "https://www.googleapis.com/books/v1/volumes?q=$query" //creer la requete Google Books : https://www.googleapis.com/books/v1/volumes?q=intitle:$title+inauthor:$author

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            println("Erreur API : ${response.code}")
            return emptyList()
        }

        val body = response.body?.string() ?: return emptyList()

        val jsonObject = gson.fromJson(body, JsonObject::class.java)
        val items = jsonObject.getAsJsonArray("items") ?: return emptyList()

        for (itemElement in items) {
            val item = itemElement.asJsonObject
            val volumeInfo = item.getAsJsonObject("volumeInfo")

            val book = Book(
                id = item.get("id")?.asString ?: "",
                title = volumeInfo.get("title")?.asString ?: "",
                subtitle = volumeInfo.get("subtitle")?.asString ?: "",
                authors = volumeInfo.getAsJsonArray("authors")?.map { it.asString } ?: listOf(),
                publisher = volumeInfo.get("publisher")?.asString ?: "",
                publishedDate = volumeInfo.get("publishedDate")?.asString ?: "",
                description = volumeInfo.get("description")?.asString ?: "",
                pageCount = volumeInfo.get("pageCount")?.asInt ?: 0,
                thumbnailUrl = volumeInfo.getAsJsonObject("imageLinks")?.get("thumbnail")?.asString,
                previewLink = volumeInfo.get("previewLink")?.asString,
                infoLink = volumeInfo.get("infoLink")?.asString,
                buyLink = item.getAsJsonObject("saleInfo")?.get("buyLink")?.asString
            )

            books.add(book)
        }

        return books
    }
}
/*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksService {
    @GET("volumes") //apeller l'endpoint : GET https://www.googleapis.com/books/v1/volumes
    fun searchBooks(@Query("q") query: String): Call<GoogleBooksResponse> //retourne une requête asynchrone
}

 */

