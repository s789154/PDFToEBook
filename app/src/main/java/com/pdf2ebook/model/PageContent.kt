package com.pdf2ebook.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 页面内容
 */
@Entity(
    tableName = "pages",
    foreignKeys = [
        ForeignKey(
            entity = Document::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId"), Index("pageNumber")]
)
@Serializable
data class PageContent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentId: Long,
    val pageNumber: Int,
    val rawText: String = "", // OCR原始识别文本
    val processedText: String = "", // AI处理后的文本
    val confidence: Float = 0f, // OCR置信度
    val layout: PageLayout? = null,
    val images: List<PageImage> = emptyList(),
    val tables: List<PageTable> = emptyList(),
    val charts: List<PageChart> = emptyList(),
    val isProcessed: Boolean = false,
    val processingTime: Long = 0
)

/**
 * 页面布局
 */
@Serializable
data class PageLayout(
    val width: Float,
    val height: Float,
    val margins: Margins,
    val columns: Int = 1,
    val readingDirection: ReadingDirection = ReadingDirection.LEFT_TO_RIGHT,
    val backgroundColor: String? = null // 背景色，用于识别书籍底色
)

/**
 * 页边距
 */
@Serializable
data class Margins(
    val top: Float = 0f,
    val bottom: Float = 0f,
    val left: Float = 0f,
    val right: Float = 0f
)

/**
 * 阅读方向
 */
enum class ReadingDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    TOP_TO_BOTTOM
}

/**
 * 页面图片
 */
@Serializable
data class PageImage(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val imagePath: String,
    val enhancedImagePath: String? = null, // AI增强后的图片
    val caption: String? = null,
    val isClear: Boolean = true, // 图片是否清晰
    val needsEnhancement: Boolean = false
)

/**
 * 页面表格
 */
@Serializable
data class PageTable(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rows: Int,
    val columns: Int,
    val cells: List<TableCell>,
    val htmlRepresentation: String? = null
)

/**
 * 表格单元格
 */
@Serializable
data class TableCell(
    val row: Int,
    val column: Int,
    val rowSpan: Int = 1,
    val colSpan: Int = 1,
    val text: String,
    val isHeader: Boolean = false
)

/**
 * 页面图表
 */
@Serializable
data class PageChart(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val chartType: ChartType,
    val title: String? = null,
    val data: ChartData? = null,
    val editableRepresentation: String? = null // 可编辑的图表表示（如JSON）
)

/**
 * 图表类型
 */
enum class ChartType {
    BAR,
    LINE,
    PIE,
    SCATTER,
    AREA,
    OTHER
}

/**
 * 图表数据
 */
@Serializable
data class ChartData(
    val labels: List<String>,
    val datasets: List<ChartDataset>
)

@Serializable
data class ChartDataset(
    val label: String,
    val values: List<Double>,
    val color: String? = null
)
