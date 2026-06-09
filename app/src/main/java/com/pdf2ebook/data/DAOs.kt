package com.pdf2ebook.data

import androidx.room.*
import com.pdf2ebook.model.APIConfig
import com.pdf2ebook.model.Document
import com.pdf2ebook.model.PageContent
import com.pdf2ebook.model.ProcessingStatus
import kotlinx.coroutines.flow.Flow

/**
 * API配置DAO
 */
@Dao
interface APIConfigDao {
    @Query("SELECT * FROM api_configs WHERE isEnabled = 1 ORDER BY priority DESC")
    fun getEnabledConfigs(): Flow<List<APIConfig>>

    @Query("SELECT * FROM api_configs ORDER BY name ASC")
    fun getAllConfigs(): Flow<List<APIConfig>>

    @Query("SELECT * FROM api_configs WHERE id = :id")
    suspend fun getConfigById(id: Long): APIConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: APIConfig): Long

    @Update
    suspend fun updateConfig(config: APIConfig)

    @Delete
    suspend fun deleteConfig(config: APIConfig)

    @Query("DELETE FROM api_configs WHERE id = :id")
    suspend fun deleteConfigById(id: Long)
}

/**
 * 文档DAO
 */
@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): Document?

    @Query("SELECT * FROM documents WHERE status = :status")
    fun getDocumentsByStatus(status: ProcessingStatus): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("UPDATE documents SET status = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ProcessingStatus, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET processedPages = :pages WHERE id = :id")
    suspend fun updateProcessedPages(id: Long, pages: Int)

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :documentId")
    suspend fun getDocumentWithPages(documentId: Long): DocumentWithPages?
}

/**
 * 页面内容DAO
 */
@Dao
interface PageContentDao {
    @Query("SELECT * FROM pages WHERE documentId = :documentId ORDER BY pageNumber ASC")
    fun getPagesForDocument(documentId: Long): Flow<List<PageContent>>

    @Query("SELECT * FROM pages WHERE id = :id")
    suspend fun getPageById(id: Long): PageContent?

    @Query("SELECT * FROM pages WHERE documentId = :documentId AND pageNumber = :pageNumber")
    suspend fun getPageByNumber(documentId: Long, pageNumber: Int): PageContent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageContent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<PageContent>)

    @Update
    suspend fun updatePage(page: PageContent)

    @Delete
    suspend fun deletePage(page: PageContent)

    @Query("DELETE FROM pages WHERE documentId = :documentId")
    suspend fun deletePagesForDocument(documentId: Long)

    @Query("SELECT COUNT(*) FROM pages WHERE documentId = :documentId AND isProcessed = 1")
    suspend fun getProcessedPageCount(documentId: Long): Int
}

/**
 * 文档与页面的关联
 */
data class DocumentWithPages(
    @Embedded val document: Document,
    @Relation(
        parentColumn = "id",
        entityColumn = "documentId"
    )
    val pages: List<PageContent>
)
