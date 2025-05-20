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

    fun searchBook(title: String, author: String): List<Book> {
        val books = mutableListOf<Book>()
        val query = "intitle:$title+inauthor:$author"
        val url = "https://www.googleapis.com/books/v1/volumes?q=$query"
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

        for (itemElement in items) {
            val item = itemElement.asJsonObject
            val volumeInfo = item.getAsJsonObject("volumeInfo")
            val saleInfo = item.getAsJsonObject("saleInfo")
            val accessInfo = item.getAsJsonObject("accessInfo")
            val imageLinks = volumeInfo.getAsJsonObject("imageLinks")

            val book = Book(
                id = item.get("id")?.asString ?: "",
                title = volumeInfo.get("title")?.asString ?: "",
                subtitle = volumeInfo.get("subtitle")?.asString,
                authors = volumeInfo.getAsJsonArray("authors")?.map { it.asString } ?: emptyList(),
                publisher = volumeInfo.get("publisher")?.asString,
                publishedDate = volumeInfo.get("publishedDate")?.asString,
                description = volumeInfo.get("description")?.asString,
                pageCount = volumeInfo.get("pageCount")?.asInt ?: 0,
                industryIdentifiers = volumeInfo.getAsJsonArray("industryIdentifiers")?.map {
                    it.asJsonObject.get("identifier")?.asString ?: ""
                } ?: emptyList(),
                readingModesText = volumeInfo.getAsJsonObject("readingModes")?.get("text")?.asBoolean ?: false,
                readingModesImage = volumeInfo.getAsJsonObject("readingModes")?.get("image")?.asBoolean ?: false,
                printType = volumeInfo.get("printType")?.asString,
                categories = volumeInfo.getAsJsonArray("categories")?.map { it.asString } ?: emptyList(),
                averageRating = volumeInfo.get("averageRating")?.asDouble,
                ratingsCount = volumeInfo.get("ratingsCount")?.asInt,
                maturityRating = volumeInfo.get("maturityRating")?.asString,
                allowAnonLogging = volumeInfo.get("allowAnonLogging")?.asBoolean ?: false,
                contentVersion = volumeInfo.get("contentVersion")?.asString,
                language = volumeInfo.get("language")?.asString,
                thumbnailUrl = imageLinks?.get("thumbnail")?.asString,
                smallThumbnailUrl = imageLinks?.get("smallThumbnail")?.asString,
                previewLink = volumeInfo.get("previewLink")?.asString,
                infoLink = volumeInfo.get("infoLink")?.asString,
                canonicalVolumeLink = volumeInfo.get("canonicalVolumeLink")?.asString,
                country = saleInfo?.get("country")?.asString,
                saleability = saleInfo?.get("saleability")?.asString,
                isEbook = saleInfo?.get("isEbook")?.asBoolean ?: false,
                listPrice = saleInfo?.getAsJsonObject("listPrice")?.get("amount")?.asDouble,
                retailPrice = saleInfo?.getAsJsonObject("retailPrice")?.get("amount")?.asDouble,
                currencyCode = saleInfo?.getAsJsonObject("retailPrice")?.get("currencyCode")?.asString,
                buyLink = saleInfo?.get("buyLink")?.asString,
                viewability = accessInfo?.get("viewability")?.asString,
                embeddable = accessInfo?.get("embeddable")?.asBoolean ?: false,
                publicDomain = accessInfo?.get("publicDomain")?.asBoolean ?: false,
                textToSpeechPermission = accessInfo?.get("textToSpeechPermission")?.asString,
                epubAvailable = accessInfo?.getAsJsonObject("epub")?.get("isAvailable")?.asBoolean ?: false,
                pdfAvailable = accessInfo?.getAsJsonObject("pdf")?.get("isAvailable")?.asBoolean ?: false,
                webReaderLink = accessInfo?.get("webReaderLink")?.asString,
                accessViewStatus = accessInfo?.get("accessViewStatus")?.asString,
                quoteSharingAllowed = accessInfo?.get("quoteSharingAllowed")?.asBoolean ?: false,
                textSnippet = item.getAsJsonObject("searchInfo")?.get("textSnippet")?.asString
            )

            books.add(book)
        }

        return books
    }
}
