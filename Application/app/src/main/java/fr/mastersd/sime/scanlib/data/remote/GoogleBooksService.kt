package fr.mastersd.sime.scanlib.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.mastersd.sime.scanlib.domain.model.Book
import okhttp3.OkHttpClient
import okhttp3.Request

class GoogleBooksService {

    private val client = OkHttpClient()
    private val gson = Gson()

    fun searchBook(titleAuthor: String): List<Book> {
        val books = mutableListOf<Book>()
        val query = "$titleAuthor"
        val url = "https://www.googleapis.com/books/v1/volumes?q=${query}&maxResults=1"
        Log.d("GoogleBooksService", "Requête envoyée à: $url")

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            println("Erreur API : ${response.code}")
            return emptyList()
        }

        val body = response.body?.string() ?: return emptyList()
        Log.d("GoogleBooksService", "Réponse brute: $body")

        val jsonObject = gson.fromJson(body, JsonObject::class.java)
        val items = jsonObject.getAsJsonArray("items") ?: return emptyList()

        val item = items.firstOrNull()?.asJsonObject ?: return emptyList()
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
        return books
    }
}
