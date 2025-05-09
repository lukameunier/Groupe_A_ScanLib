import java.io.File

fun generateSampleScanFile(path: String) {
    val exemples = listOf(
        "harry potter;rowling",
        "le petit prince;antoine de saint-exupéry",
        "1984;george orwell",
        "l’étranger;albert camus",
        "bel-ami;guy de maupassant"
    )

    val file = File(path)
    file.printWriter().use { out ->
        exemples.shuffled().forEach { line ->
            out.println(line)
        }
    }

    println("✅ Fichier $path généré avec ${exemples.size} lignes.")
}
