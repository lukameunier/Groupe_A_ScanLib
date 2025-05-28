package fr.mastersd.sime.scanlib.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookDatabaseModule {

    @Provides
    @Singleton
    fun provideBookDatabase(@ApplicationContext appContext: Context): BookDatabase =
        Room.databaseBuilder(
            appContext,
            BookDatabase::class.java,
            "scanlib_db"
        ).build()

    @Provides
    fun provideBookDao(database: BookDatabase): BookDao = database.bookDao()
}
