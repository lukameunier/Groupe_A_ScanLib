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
                    "&fields=items(id,volumeInfo(title,authors,publisher,publishedDate,pageCount,description," +
                    "industryIdentifiers,categories,averageRating,ratingsCount,infoLink,previewLink," +
                    "imageLinks/thumbnail,imageLinks/smallThumbnail),accessInfo/country,searchInfo/textSnippet)"

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
            val item = jsonObject.getAsJsonArray("items")
                ?.firstOrNull()?.asJsonObject ?: return@withContext null

            val volumeInfo = item.getAsJsonObject("volumeInfo") ?: return@withContext null
            val imageLinks = volumeInfo.getAsJsonObject("imageLinks") ?: JsonObject()
            val accessInfo = item.getAsJsonObject("accessInfo") ?: JsonObject()
            val searchInfo = item.getAsJsonObject("searchInfo") ?: JsonObject()

            return@withContext Book(
                id = item.get("id")?.asString ?: "",
                title = volumeInfo.get("title")?.asString ?: "",
                authors = volumeInfo.getAsJsonArray("authors")?.map { it.asString } ?: emptyList(),
                publisher = volumeInfo.get("publisher")?.asString,
                publishedDate = volumeInfo.get("publishedDate")?.asString,
                pageCount = volumeInfo.get("pageCount")?.asInt ?: 0,
                description = volumeInfo.get("description")?.asString,
                industryIdentifiers = volumeInfo.getAsJsonArray("industryIdentifiers")
                    ?.mapNotNull { it.asJsonObject.get("identifier")?.asString } ?: emptyList(),
                categories = volumeInfo.getAsJsonArray("categories")?.map { it.asString } ?: emptyList(),
                averageRating = volumeInfo.get("averageRating")?.asDouble,
                ratingsCount = volumeInfo.get("ratingsCount")?.asInt,
                thumbnailUrl = imageLinks.get("thumbnail")?.asString,
                smallThumbnailUrl = imageLinks.get("smallThumbnail")?.asString,
                previewLink = volumeInfo.get("previewLink")?.asString,
                infoLink = volumeInfo.get("infoLink")?.asString,
                country = accessInfo.get("country")?.asString,
                textSnippet = searchInfo.get("textSnippet")?.asString
            )

        } catch (e: Exception) {
            Log.e("GoogleBooksService", "Erreur réseau ou parsing", e)
            null
        }
    }
}
