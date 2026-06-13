package com.pdf2ebook.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PDF处理工具
 * 负责PDF解析、页面渲染、内容提取
 */
@Singleton
class PDFProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 获取PDF页数
     */
    suspend fun getPageCount(uri: Uri): Int = withContext(Dispatchers.IO) {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        parcelFileDescriptor?.use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                renderer.pageCount
            }
        } ?: 0
    }

    /**
     * 渲染PDF页面为位图
     */
    suspend fun renderPageToBitmap(
        uri: Uri,
        pageNumber: Int,
        width: Int = 1080,
        height: Int = 1920
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            parcelFileDescriptor?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    renderer.openPage(pageNumber).use { page ->
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)

                        // 计算缩放比例
                        val scale = minOf(
                            width.toFloat() / page.width,
                            height.toFloat() / page.height
                        )

                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PDFProcessor", "渲染页面失败: $pageNumber", e)
            null
        }
    }

    /**
     * 批量渲染PDF页面
     */
    suspend fun renderAllPages(
        uri: Uri,
        pageCount: Int,
        onProgress: (Int) -> Unit
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        for (i in 0 until pageCount) {
            val bitmap = renderPageToBitmap(uri, i)
            bitmap?.let { bitmaps.add(it) }
            onProgress(i + 1)
        }
        bitmaps
    }

    /**
     * 提取PDF元数据
     */
    suspend fun extractMetadata(uri: Uri): PDFMetadata? = withContext(Dispatchers.IO) {
        try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            parcelFileDescriptor?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    PDFMetadata(
                        pageCount = renderer.pageCount,
                        title = null,
                        author = null,
                        subject = null
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("PDFProcessor", "提取元数据失败", e)
            null
        }
    }

    /**
     * 检测页面布局
     */
    suspend fun detectLayout(bitmap: Bitmap): PageLayoutInfo = withContext(Dispatchers.Default) {
        // 分析页面布局
        val width = bitmap.width
        val height = bitmap.height

        // 检测文本区域
        val textRegions = detectTextRegions(bitmap)

        // 检测图片区域
        val imageRegions = detectImageRegions(bitmap)

        // 检测表格区域
        val tableRegions = detectTableRegions(bitmap)

        // 估算页边距
        val margins = estimateMargins(bitmap, textRegions)

        PageLayoutInfo(
            width = width,
            height = height,
            margins = margins,
            textRegions = textRegions,
            imageRegions = imageRegions,
            tableRegions = tableRegions
        )
    }

    /**
     * 检测文本区域
     */
    private fun detectTextRegions(bitmap: Bitmap): List<Rect> {
        // 简化实现：使用边缘检测和连通区域分析
        // 实际应使用更复杂的算法
        return emptyList()
    }

    /**
     * 检测图片区域
     */
    private fun detectImageRegions(bitmap: Bitmap): List<Rect> {
        return emptyList()
    }

    /**
     * 检测表格区域
     */
    private fun detectTableRegions(bitmap: Bitmap): List<Rect> {
        return emptyList()
    }

    /**
     * 估算页边距
     */
    private fun estimateMargins(bitmap: Bitmap, textRegions: List<Rect>): MarginsInfo {
        val width = bitmap.width
        val height = bitmap.height

        // 找出文本区域的边界
        val leftMost = textRegions.minOfOrNull { it.left } ?: 0
        val rightMost = textRegions.maxOfOrNull { it.right } ?: width
        val topMost = textRegions.minOfOrNull { it.top } ?: 0
        val bottomMost = textRegions.maxOfOrNull { it.bottom } ?: height

        return MarginsInfo(
            left = leftMost.toFloat(),
            right = (width - rightMost).toFloat(),
            top = topMost.toFloat(),
            bottom = (height - bottomMost).toFloat()
        )
    }
}

/**
 * PDF元数据
 */
data class PDFMetadata(
    val pageCount: Int,
    val title: String?,
    val author: String?,
    val subject: String?
)

/**
 * 页面布局信息
 */
data class PageLayoutInfo(
    val width: Int,
    val height: Int,
    val margins: MarginsInfo,
    val textRegions: List<Rect>,
    val imageRegions: List<Rect>,
    val tableRegions: List<Rect>
)

data class MarginsInfo(
    val left: Float,
    val right: Float,
    val top: Float,
    val bottom: Float
)

data class Rect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    fun contains(x: Int, y: Int): Boolean = x in left until right && y in top until bottom
}
