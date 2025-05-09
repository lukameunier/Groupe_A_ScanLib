import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

fun main() {
    val filePath = "scan.txt"

    val reader = ScanFileReader()
    val service = GoogleBooksService()
    val repository = BookRepository(reader, service)

    repository.syncBooksFromScanFile(filePath)
}

/*
fun main(){
    val scanFile = "scan.txt"
    generateSampleScanFile(scanFile) //generer un fichier aléatoir pour le test

    /* Affichage de la Requête et Réponse HTTP */
//    val logging = HttpLoggingInterceptor()
//    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
//    val client = OkHttpClient.Builder()
//        .addInterceptor(logging)
//        .build()

    val retorfit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/books/v1/") //racine de l'api
//        .client(client) -> pour le HTTP mais ne sert à rien dans ce cas = tester
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retorfit.create(GoogleBooksService::class.java) //generer une implementation de l'interface GoogleBooksService

    searchBooksFromFile(service, scanFile)

    //===== Tester sur une requete avec des mots clé predefini
//    val call = service.searchBooks("intitle:harry +inauthor:rowling")
}
 */