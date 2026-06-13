package com.pdf2ebook.di

import android.content.Context
import androidx.room.Room
import com.pdf2ebook.data.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pdf2ebook_database"
        ).build()
    }

    @Provides
    fun provideAPIConfigDao(database: AppDatabase) = database.apiConfigDao()

    @Provides
    fun provideDocumentDao(database: AppDatabase) = database.documentDao()

    @Provides
    fun providePageContentDao(database: AppDatabase) = database.pageContentDao()
}
