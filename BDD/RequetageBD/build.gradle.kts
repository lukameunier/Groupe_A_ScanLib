import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Test
    testImplementation(kotlin("test"))

    // Retrofit (appel API REST)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Gson (conversion JSON ↔ objets Kotlin)
    implementation("com.google.code.gson:gson:2.10.1")

    // SQLite via JDBC (pour JVM pur, non Android)
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")

    // Logger (optionnel)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    //Requete et reponse HTTP
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt") // nom de la classe avec la fonction main
}
