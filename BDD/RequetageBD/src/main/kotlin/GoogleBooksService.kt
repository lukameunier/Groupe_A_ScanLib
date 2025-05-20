import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request

class GoogleBooksService {

    private val client = OkHttpClient()
    private val gson = Gson()

    fun searchBook(title: String, author: String): Book? {
        val query = "intitle:$title+inauthor:$author"
        val url = "https://www.googleapis.com/books/v1/volumes?q=$query"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            println("Erreur API : ${response.code}")
            return null
        }

        val body = response.body?.string() ?: return null
        val jsonObject = gson.fromJson(body, JsonObject::class.java)
        val items = jsonObject.getAsJsonArray("items") ?: return null
        val item = items.firstOrNull()?.asJsonObject ?: return null

        val volumeInfo = item.getAsJsonObject("volumeInfo")
        val saleInfo = item.getAsJsonObject("saleInfo")
        val accessInfo = item.getAsJsonObject("accessInfo")
        val searchInfo = item.getAsJsonObject("searchInfo")

        val industryIdentifiers = volumeInfo.getAsJsonArray("industryIdentifiers")?.map {
            val obj = it.asJsonObject
            IndustryIdentifier(
                type = obj.get("type")?.asString ?: "",
                identifier = obj.get("identifier")?.asString ?: ""
            )
        }

        val readingModes = volumeInfo.getAsJsonObject("readingModes")?.let {
            ReadingModes(
                text = it.get("text")?.asBoolean ?: false,
                image = it.get("image")?.asBoolean ?: false
            )
        }

        val panelizationSummary = volumeInfo.getAsJsonObject("panelizationSummary")?.let {
            PanelizationSummary(
                containsEpubBubbles = it.get("containsEpubBubbles")?.asBoolean ?: false,
                containsImageBubbles = it.get("containsImageBubbles")?.asBoolean ?: false
            )
        }

        val imageLinks = volumeInfo.getAsJsonObject("imageLinks")
        val epub = accessInfo.getAsJsonObject("epub")
        val pdf = accessInfo.getAsJsonObject("pdf")

        return Book(
            id = item.get("id")?.asString ?: "",
            etag = item.get("etag")?.asString,
            selfLink = item.get("selfLink")?.asString,

            title = volumeInfo.get("title")?.asString ?: "",
            subtitle = volumeInfo.get("subtitle")?.asString,
            authors = volumeInfo.getAsJsonArray("authors")?.map { it.asString } ?: listOf(),
            publisher = volumeInfo.get("publisher")?.asString,
            publishedDate = volumeInfo.get("publishedDate")?.asString,
            description = volumeInfo.get("description")?.asString,
            industryIdentifiers = industryIdentifiers,
            pageCount = volumeInfo.get("pageCount")?.asInt,
            printType = volumeInfo.get("printType")?.asString,
            categories = volumeInfo.getAsJsonArray("categories")?.map { it.asString },
            averageRating = volumeInfo.get("averageRating")?.asDouble,
            ratingsCount = volumeInfo.get("ratingsCount")?.asInt,
            maturityRating = volumeInfo.get("maturityRating")?.asString,
            allowAnonLogging = volumeInfo.get("allowAnonLogging")?.asBoolean,
            contentVersion = volumeInfo.get("contentVersion")?.asString,
            readingModes = readingModes,
            panelizationSummary = panelizationSummary,
            language = volumeInfo.get("language")?.asString,

            thumbnailUrl = imageLinks?.get("thumbnail")?.asString,
            smallThumbnailUrl = imageLinks?.get("smallThumbnail")?.asString,
            previewLink = volumeInfo.get("previewLink")?.asString,
            infoLink = volumeInfo.get("infoLink")?.asString,
            canonicalVolumeLink = volumeInfo.get("canonicalVolumeLink")?.asString,

            isEbook = saleInfo?.get("isEbook")?.asBoolean,
            saleability = saleInfo?.get("saleability")?.asString,
            listPrice = saleInfo?.getAsJsonObject("listPrice")?.let {
                Price(
                    amount = it.get("amount")?.asDouble ?: 0.0,
                    currencyCode = it.get("currencyCode")?.asString ?: ""
                )
            },
            retailPrice = saleInfo?.getAsJsonObject("retailPrice")?.let {
                Price(
                    amount = it.get("amount")?.asDouble ?: 0.0,
                    currencyCode = it.get("currencyCode")?.asString ?: ""
                )
            },
            buyLink = saleInfo?.get("buyLink")?.asString,

            viewability = accessInfo?.get("viewability")?.asString,
            embeddable = accessInfo?.get("embeddable")?.asBoolean,
            publicDomain = accessInfo?.get("publicDomain")?.asBoolean,
            textToSpeechPermission = accessInfo?.get("textToSpeechPermission")?.asString,
            epubAvailable = epub?.get("isAvailable")?.asBoolean,
            epubTokenLink = epub?.get("acsTokenLink")?.asString,
            pdfAvailable = pdf?.get("isAvailable")?.asBoolean,
            pdfTokenLink = pdf?.get("acsTokenLink")?.asString,
            webReaderLink = accessInfo?.get("webReaderLink")?.asString,
            accessViewStatus = accessInfo?.get("accessViewStatus")?.asString,
            quoteSharingAllowed = accessInfo?.get("quoteSharingAllowed")?.asBoolean,

            textSnippet = searchInfo?.get("textSnippet")?.asString
        )
    }
}
