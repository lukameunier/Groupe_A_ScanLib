package fr.mastersd.sime.scanlib.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.mastersd.sime.scanlib.ml.BookSpineDetector
import fr.mastersd.sime.scanlib.ml.BookSpineOCR
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScanModule {

    @Provides
    @Singleton
    fun provideBookSpineDetector(context: Context): BookSpineDetector {
        return BookSpineDetector(context.assets)
    }

    @Provides
    @Singleton
    fun provideBookSpineOCR(): BookSpineOCR {
        return BookSpineOCR()
    }
}
