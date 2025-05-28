package fr.mastersd.sime.scanlib.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GoogleBooksService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun searchBook(titleAuthor: String): Book? = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(titleAuthor, "UTF-8")
            val url = "https://www.googleapis.com/books/v1/volumes?q=$encodedQuery&maxResults=1" +
                    "&fields=items(id,volumeInfo(title,authors,publisher,publishedDate,infoLink,imageLinks/thumbnail))"

            Log.d("GoogleBooksService", "Requête envoyée à: $url")

            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("GoogleBooksService", "Erreur HTTP : ${response.code}")
                return@withContext null
            }

            val body = response.body?.string() ?: return@withContext null
            Log.d("GoogleBooksService", "Réponse brute: $body")

            val jsonObject = gson.fromJson(body, JsonObject::class.java)
            val items = jsonObject.getAsJsonArray("items") ?: return@withContext null
            val item = items.firstOrNull()?.asJsonObject ?: return@withContext null

            val volumeInfo = item.getAsJsonObject("volumeInfo") ?: return@withContext null
            val imageLinks = volumeInfo.getAsJsonObject("imageLinks") ?: JsonObject()

            return@withContext Book(
                id = item.get("id")?.asString ?: "",
                title = volumeInfo.get("title")?.asString ?: "",
                authors = volumeInfo.getAsJsonArray("authors")?.map { it.asString } ?: emptyList(),
                publisher = volumeInfo.get("publisher")?.asString,
                publishedDate = volumeInfo.get("publishedDate")?.asString,
                previewLink = null,
                infoLink = volumeInfo.get("infoLink")?.asString,
                thumbnailUrl = imageLinks.get("thumbnail")?.asString,
                description = null,
                pageCount = 0,
                industryIdentifiers = emptyList(),
                categories = emptyList(),
                averageRating = null,
                ratingsCount = null,
                smallThumbnailUrl = null,
                country = null,
                textSnippet = null
            )

        } catch (e: Exception) {
            Log.e("GoogleBooksService", "Erreur réseau ou parsing", e)
            null
        }
    }
}
