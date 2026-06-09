package com.pdf2ebook.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.Date

/**
 * 文档项目
 */
@Entity(
    tableName = "documents",
    indices = [Index("createdAt"), Index("status")]
)
@Serializable
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val totalPages: Int = 0,
    val processedPages: Int = 0,
    val status: ProcessingStatus = ProcessingStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val metadata: DocumentMetadata? = null,
    val settings: ProcessingSettings = ProcessingSettings()
)

/**
 * 处理状态
 */
enum class ProcessingStatus {
    PENDING,      // 待处理
    PROCESSING,   // 处理中
    PAUSED,       // 已暂停
    COMPLETED,    // 已完成
    FAILED,       // 失败
    CANCELLED     // 已取消
}

/**
 * 文档元数据
 */
@Serializable
data class DocumentMetadata(
    val title: String? = null,
    val author: String? = null,
    val subject: String? = null,
    val keywords: List<String> = emptyList(),
    val language: String = "zh-CN",
    val fileSize: Long = 0
)

/**
 * 处理设置
 */
@Serializable
data class ProcessingSettings(
    val ocrEngine: OCREngine = OCREngine.TESSERACT,
    val ocrLanguage: String = "chi_sim+eng", // 简体中文+英文
    val enableImageEnhancement: Boolean = true,
    val enableWatermarkRemoval: Boolean = true,
    val enableChartRecognition: Boolean = true,
    val preserveLayout: Boolean = true,
    val qualityLevel: QualityLevel = QualityLevel.BALANCED,
    val outputFormat: OutputFormat = OutputFormat.EPUB,
    val compressionLevel: Int = 80, // 0-100
    val parallelAPICalls: Boolean = true,
    val selectedAPIs: List<Long> = emptyList() // 选择的API配置ID
)

/**
 * OCR引擎
 */
enum class OCREngine {
    TESSERACT,   // Tesseract OCR
    MLKIT,       // Google ML Kit
    PADDLEOCR    // 百度PaddleOCR
}

/**
 * 质量级别
 */
enum class QualityLevel {
    FAST,        // 快速处理，较低质量
    BALANCED,    // 平衡模式
    HIGH_QUALITY // 高质量处理
}

/**
 * 输出格式
 */
enum class OutputFormat(val extension: String, val mimeType: String) {
    EPUB("epub", "application/epub+zip"),
    PDF("pdf", "application/pdf"),
    TXT("txt", "text/plain"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    HTML("html", "text/html"),
    JSON("json", "application/json")
}
