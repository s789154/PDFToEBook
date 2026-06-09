package com.pdf2ebook.worker

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.pdf2ebook.R
import com.pdf2ebook.data.DocumentDao
import com.pdf2ebook.data.PageContentDao
import com.pdf2ebook.model.*
import com.pdf2ebook.network.LLMApiManager
import com.pdf2ebook.ocr.OCRProcessor
import com.pdf2ebook.utils.EBookGenerator
import com.pdf2ebook.utils.ImageUtils
import com.pdf2ebook.utils.PDFProcessor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * PDF处理Worker
 * 在后台执行PDF扫描识别、OCR、AI处理等耗时任务
 */
@HiltWorker
class PDFProcessingWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val documentDao: DocumentDao,
    private val pageContentDao: PageContentDao,
    private val pdfProcessor: PDFProcessor,
    private val ocrProcessor: OCRProcessor,
    private val imageUtils: ImageUtils,
    private val llmApiManager: LLMApiManager,
    private val eBookGenerator: EBookGenerator
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_DOCUMENT_ID = "document_id"
        const val KEY_PDF_URI = "pdf_uri"
        const val KEY_SETTINGS = "settings"
        
        // 进度状态常量
        const val PROGRESS_STAGE = "stage"
        const val PROGRESS_PERCENT = "percent"
        const val PROGRESS_CURRENT_PAGE = "current_page"
        const val PROGRESS_TOTAL_PAGES = "total_pages"
        
        // 处理阶段
        const val STAGE_INITIALIZING = "初始化中..."
        const val STAGE_RENDERING = "渲染PDF页面..."
        const val STAGE_OCR = "OCR识别中..."
        const val STAGE_AI_PROCESSING = "AI处理中..."
        const val STAGE_SAVING = "保存数据中..."
        const val STAGE_COMPLETED = "处理完成"
    }

    override suspend fun doWork(): Result = try {
        val documentId = inputData.getLong(KEY_DOCUMENT_ID, -1)
        val pdfUriString = inputData.getString(KEY_PDF_URI) ?: return Result.failure()

        if (documentId == -1L) return Result.failure()

        // 发送初始化进度
        updateProgress(STAGE_INITIALIZING, 0, 0, 0)

        // 更新文档状态为处理中
        documentDao.updateStatus(documentId, ProcessingStatus.PROCESSING)

        // 获取文档和设置
        val document = documentDao.getDocumentById(documentId) ?: return Result.failure()
        val settings = document.settings

        // 初始化OCR引擎
        ocrProcessor.initialize(settings.ocrEngine, settings.ocrLanguage)

        // 处理PDF
        processPDF(documentId, Uri.parse(pdfUriString), settings)

        // 更新文档状态为已完成
        documentDao.updateStatus(documentId, ProcessingStatus.COMPLETED)
        
        // 发送完成进度
        updateProgress(STAGE_COMPLETED, 100, 0, 0)

        Result.success()
    } catch (e: Exception) {
        Log.e("PDFProcessingWorker", "处理失败", e)
        val documentId = inputData.getLong(KEY_DOCUMENT_ID, -1)
        if (documentId != -1L) {
            documentDao.updateStatus(documentId, ProcessingStatus.FAILED)
        }
        Result.failure()
    }
    
    /**
     * 更新处理进度
     */
    private suspend fun updateProgress(
        stage: String,
        percent: Int,
        currentPage: Int,
        totalPages: Int
    ) {
        setProgressAsync(
            androidx.work.Data.Builder()
                .putString(PROGRESS_STAGE, stage)
                .putInt(PROGRESS_PERCENT, percent)
                .putInt(PROGRESS_CURRENT_PAGE, currentPage)
                .putInt(PROGRESS_TOTAL_PAGES, totalPages)
                .build()
        )
    }

    /**
     * 处理PDF文件
     */
    private suspend fun processPDF(documentId: Long, pdfUri: Uri, settings: ProcessingSettings) {
        // 1. 获取PDF页数
        updateProgress(STAGE_RENDERING, 5, 0, 0)
        val pageCount = pdfProcessor.getPageCount(pdfUri)

        // 2. 更新文档总页数
        val document = documentDao.getDocumentById(documentId)
        document?.let {
            documentDao.updateDocument(it.copy(totalPages = pageCount))
        }
        
        updateProgress(STAGE_RENDERING, 10, 0, pageCount)

        // 3. 逐页处理
        for (pageNumber in 0 until pageCount) {
            // 渲染阶段 (10-20%)
            updateProgress(STAGE_RENDERING, 10 + (pageNumber * 10 / pageCount), pageNumber + 1, pageCount)
            
            // OCR阶段 (20-70%)
            updateProgress(STAGE_OCR, 20 + (pageNumber * 50 / pageCount), pageNumber + 1, pageCount)
            
            processPage(documentId, pdfUri, pageNumber, settings)

            // 更新已处理页数
            documentDao.updateProcessedPages(documentId, pageNumber + 1)

            // AI处理和保存阶段 (70-95%)
            updateProgress(STAGE_AI_PROCESSING, 70 + (pageNumber * 25 / pageCount), pageNumber + 1, pageCount)
            
            updateProgress(STAGE_SAVING, 95, pageNumber + 1, pageCount)
        }
    }

    /**
     * 处理单个页面
     */
    private suspend fun processPage(
        documentId: Long,
        pdfUri: Uri,
        pageNumber: Int,
        settings: ProcessingSettings
    ) = withContext(Dispatchers.IO) {
        // 1. 渲染PDF页面为图片
        val bitmap = pdfProcessor.renderPageToBitmap(pdfUri, pageNumber)
            ?: return@withContext

        // 2. 图像预处理
        var processedBitmap = bitmap
        if (settings.enableWatermarkRemoval) {
            processedBitmap = imageUtils.removeWatermark(processedBitmap)
        }
        if (settings.enableImageEnhancement) {
            processedBitmap = imageUtils.enhanceImage(processedBitmap)
        }

        // 3. 检测背景色
        val backgroundColor = imageUtils.detectBackgroundColor(processedBitmap)

        // 4. OCR识别
        val ocrResult = ocrProcessor.recognizeText(processedBitmap, settings.ocrEngine)

        // 5. 使用AI处理文本
        val processedText = if (settings.parallelAPICalls && settings.selectedAPIs.isNotEmpty()) {
            enhanceTextWithAI(ocrResult.text, settings.selectedAPIs)
        } else {
            ocrResult.text
        }

        // 6. 检测图表
        val charts = if (settings.enableChartRecognition) {
            detectCharts(processedBitmap)
        } else {
            emptyList()
        }

        // 7. 保存页面内容
        val pageContent = PageContent(
            documentId = documentId,
            pageNumber = pageNumber + 1,
            rawText = ocrResult.text,
            processedText = processedText,
            confidence = ocrResult.confidence,
            layout = PageLayout(
                width = bitmap.width.toFloat(),
                height = bitmap.height.toFloat(),
                margins = com.pdf2ebook.model.Margins()
            ),
            images = emptyList(),
            tables = emptyList(),
            charts = charts,
            isProcessed = true
        )

        pageContentDao.insertPage(pageContent)
    }

    /**
     * 使用AI增强文本
     */
    private suspend fun enhanceTextWithAI(
        text: String,
        apiIds: List<Long>
    ): String {
        val prompt = """
            请对以下OCR识别的文本进行优化和修正：
            
            要求：
            1. 修正OCR识别错误
            2. 保持原有段落结构
            3. 修正标点符号
            4. 保持原文语义不变
            
            文本：
            $text
        """.trimIndent()

        // 获取API配置（这里简化，实际应从数据库查询）
        // val configs = apiConfigDao.getConfigsByIds(apiIds)
        // val result = llmApiManager.callParallel(configs, prompt)

        return text // 简化实现
    }

    /**
     * 检测图表
     */
    private suspend fun detectCharts(bitmap: android.graphics.Bitmap): List<PageChart> {
        // 图表检测逻辑（可使用AI模型）
        return emptyList()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            1,
            android.app.Notification.Builder(context, "pdf_processing")
                .setContentTitle("正在处理PDF")
                .setContentText("处理中...")
                .setSmallIcon(R.drawable.ic_notification)
                .build()
        )
    }
}
