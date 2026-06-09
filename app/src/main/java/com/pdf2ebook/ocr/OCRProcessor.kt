package com.pdf2ebook.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import com.pdf2ebook.model.OCREngine
import com.pdf2ebook.model.PageContent
import com.pdf2ebook.model.PageLayout
import com.pdf2ebook.model.PageImage
import com.pdf2ebook.utils.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OCR处理器
 * 支持Tesseract、ML Kit和PaddleOCR
 */
@Singleton
class OCRProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tessBaseAPI: TessBaseAPI? = null

    /**
     * 初始化OCR引擎
     */
    suspend fun initialize(engine: OCREngine, language: String): Boolean {
        return withContext(Dispatchers.IO) {
            when (engine) {
                OCREngine.TESSERACT -> initializeTesseract(language)
                OCREngine.MLKIT -> true // ML Kit不需要初始化
                OCREngine.PADDLEOCR -> initializePaddleOCR()
            }
        }
    }

    /**
     * 初始化Tesseract OCR
     */
    private fun initializeTesseract(language: String): Boolean {
        return try {
            val tessBasePath = File(context.filesDir, "tesseract")
            if (!tessBasePath.exists()) {
                tessBasePath.mkdirs()
            }

            val tessDataPath = File(tessBasePath, "tessdata")
            if (!tessDataPath.exists()) {
                tessDataPath.mkdirs()
            }

            // 检查语言数据文件是否存在
            val languages = language.split("+")
            languages.forEach { lang ->
                val langFile = File(tessDataPath, "$lang.traineddata")
                if (!langFile.exists()) {
                    // 从assets复制语言数据
                    copyAssetToFile("tessdata/$lang.traineddata", langFile)
                }
            }

            tessBaseAPI = TessBaseAPI().apply {
                init(tessBasePath.absolutePath, language)
            }

            true
        } catch (e: Exception) {
            Log.e("OCRProcessor", "初始化Tesseract失败", e)
            false
        }
    }

    /**
     * 初始化PaddleOCR
     */
    private fun initializePaddleOCR(): Boolean {
        // PaddleOCR初始化逻辑（需要集成PaddleOCR Android库）
        return true
    }

    /**
     * 识别图像中的文本
     */
    suspend fun recognizeText(
        bitmap: Bitmap,
        engine: OCREngine = OCREngine.TESSERACT
    ): OCRResult = withContext(Dispatchers.IO) {
        when (engine) {
            OCREngine.TESSERACT -> recognizeWithTesseract(bitmap)
            OCREngine.MLKIT -> recognizeWithMLKit(bitmap)
            OCREngine.PADDLEOCR -> recognizeWithPaddleOCR(bitmap)
        }
    }

    /**
     * 使用Tesseract识别
     */
    private fun recognizeWithTesseract(bitmap: Bitmap): OCRResult {
        val api = tessBaseAPI ?: throw IllegalStateException("Tesseract未初始化")

        api.setImage(bitmap)
        val text = api.utF8Text
        val confidence = api.meanConfidence / 100f

        // 获取单词级别的识别结果
        val words = api.words.map { rect ->
            WordResult(
                text = rect.second,
                rect = rect.first,
                confidence = confidence
            )
        }

        return OCRResult(
            text = text,
            confidence = confidence,
            words = words
        )
    }

    /**
     * 使用ML Kit识别
     */
    private fun recognizeWithMLKit(bitmap: Bitmap): OCRResult {
        // ML Kit OCR实现（需要添加ML Kit依赖）
        // 这里返回模拟结果
        return OCRResult(
            text = "",
            confidence = 0f,
            words = emptyList()
        )
    }

    /**
     * 使用PaddleOCR识别
     */
    private fun recognizeWithPaddleOCR(bitmap: Bitmap): OCRResult {
        // PaddleOCR实现
        return OCRResult(
            text = "",
            confidence = 0f,
            words = emptyList()
        )
    }

    /**
     * 复制assets文件到本地
     */
    private fun copyAssetToFile(assetPath: String, destFile: File) {
        context.assets.open(assetPath).use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        tessBaseAPI?.end()
        tessBaseAPI = null
    }
}

/**
 * OCR识别结果
 */
data class OCRResult(
    val text: String,
    val confidence: Float,
    val words: List<WordResult>
)

/**
 * 单词识别结果
 */
data class WordResult(
    val text: String,
    val rect: android.graphics.Rect,
    val confidence: Float
)
