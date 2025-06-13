package fr.mastersd.sime.scanlib.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Base de données locale de l'app, implémentée avec Romm
 *
 * Contient une seule table: [Book], représentant les livres scannés ou récupérés
 * Utilise des [BookConverters] personalisés pour les types complexes (List<String>)
 *
 * @see BookDao pour les opérations sur la base
 * @see Book pour le modèle persistant
 * @see BookRepository pour le point d'entrée principal dans les opérations de lecture/écriture
 */
@Database(
    entities = [Book::class, FavoriteGroup::class, BookGroupCrossRef::class],
    version = 2
) //changement de version car structure modifée
@TypeConverters(BookConverters::class)
abstract class BookDatabase : RoomDatabase() {

    /**
     * Point d'accès DAO pour manipuler les entités de livres
     *
     * Permet d'exécuter les requêtes CRUD (insersions, lectures, suppressions) sur la table via Room
     */
    abstract fun bookDao(): BookDao

    abstract fun favoriteGroupDao(): GroupBookDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        /**
         * Fournit une instance unique de bd
         *
         * Utilise le contexte d'app pour éviter les fuites mémoires
         * Une seule instance créee même en accès concurent, grâce à la sysnchronisation
         *
         * @param context Contexte d'app, qui initialise la bd
         * @return Instance partagée de [BookDatabase]
         */
        fun getDatabase(context: Context): BookDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "scanlib_db"
                ).build().also { INSTANCE = it }
            }
    }
}
