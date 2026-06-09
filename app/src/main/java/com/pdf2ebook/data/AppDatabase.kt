package com.pdf2ebook.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pdf2ebook.model.APIConfig
import com.pdf2ebook.model.Document
import com.pdf2ebook.model.PageContent

/**
 * 应用数据库
 */
@Database(
    entities = [APIConfig::class, Document::class, PageContent::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apiConfigDao(): APIConfigDao
    abstract fun documentDao(): DocumentDao
    abstract fun pageContentDao(): PageContentDao
}
