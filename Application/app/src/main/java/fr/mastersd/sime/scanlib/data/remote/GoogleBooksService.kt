package fr.mastersd.sime.scanlib.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.mastersd.sime.scanlib.domain.model.Book
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Service responsable de l’interrogation de l’API Google Books pour rechercher des métadonnées d’un livre à partir du texte de l'OCR
 *
 * Effectue une requête HTTP via OkHttp, parse la réponse JSON avec Gson,
 * et retourne une liste de [Book] enrichis.
 *
 * @see Book pour le modèle enrichi utilisé dans l’application
 * @see BookRepositoryImpl pour son intégration dans le pipeline de synchronisation
 * @see ScanResult comme source initiale des requêtes
 */
class GoogleBooksService {

    private val client = OkHttpClient() //client HTTP réutilisable
    private val gson = Gson() //pour désérialiser les réponse JSON

    /**
     * Recherche un livre correspondant au texte brut en interrogeant l’API Google Books
     *
     * @param titleAuthor Texte brut de l'OCR 'titre auteur'
     * @return Liste de [Book] enrichi ou une liste vide si pas correspondance
     *
     * !!! ---> A MODIFIER: retourne le résultat le plus pertinant pour l'instant
     */
    fun searchBook(titleAuthor: String): List<Book> {
        val books = mutableListOf<Book>()

        //construire la requête
        val query = "$titleAuthor"
        val url = "https://www.googleapis.com/books/v1/volumes?q=${query}&maxResults=1"
        Log.d("GoogleBooksService", "Requête envoyée à: $url")

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        //si le requête échoue
        if (!response.isSuccessful) {
            println("Erreur API : ${response.code}")
            return emptyList()
        }

        val body = response.body?.string() ?: return emptyList()
        Log.d("GoogleBooksService", "Réponse brute: $body")

        //parsing JSON global
        val jsonObject = gson.fromJson(body, JsonObject::class.java)
        val items = jsonObject.getAsJsonArray("items") ?: return emptyList()

        val item = items.firstOrNull()?.asJsonObject ?: return emptyList()
        val volumeInfo = item.getAsJsonObject("volumeInfo")
        val saleInfo = item.getAsJsonObject("saleInfo")
        val accessInfo = item.getAsJsonObject("accessInfo")
        val imageLinks = volumeInfo.getAsJsonObject("imageLinks")

        //construire l'objet [Book] à partir des champs JSON
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
        return books
    }
}
