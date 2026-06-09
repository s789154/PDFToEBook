package com.pdf2ebook.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.pdf2ebook.model.Document
import com.pdf2ebook.model.PageContent
import com.pdf2ebook.model.OutputFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubWriter
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 电子书生成器
 * 支持EPUB、PDF、TXT、HTML等多种格式导出
 */
@Singleton
class EBookGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 生成电子书
     */
    suspend fun generate(
        document: Document,
        pages: List<PageContent>,
        format: OutputFormat,
        outputPath: String,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            when (format) {
                OutputFormat.EPUB -> generateEPUB(document, pages, outputPath, onProgress)
                OutputFormat.PDF -> generatePDF(document, pages, outputPath, onProgress)
                OutputFormat.TXT -> generateTXT(pages, outputPath)
                OutputFormat.HTML -> generateHTML(document, pages, outputPath)
                OutputFormat.DOCX -> generateDOCX(document, pages, outputPath)
                OutputFormat.JSON -> generateJSON(pages, outputPath)
            }
        } catch (e: Exception) {
            Log.e("EBookGenerator", "生成电子书失败", e)
            null
        }
    }

    /**
     * 生成EPUB格式
     */
    private fun generateEPUB(
        document: Document,
        pages: List<PageContent>,
        outputPath: String,
        onProgress: (Int) -> Unit
    ): File {
        val book = Book()

        // 设置元数据
        book.metadata.apply {
            title = document.metadata?.title ?: document.fileName
            document.metadata?.author?.let { addAuthor(it) }
            document.metadata?.language?.let { language = it }
        }

        // 添加封面（如果有的话）
        // book.coverImage = ...

        // 添加章节
        pages.forEachIndexed { index, page ->
            val html = buildPageHTML(page, index + 1)
            val resource = Resource(
                ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8)),
                "chapter_${index + 1}.html"
            )
            resource.title = "第 ${index + 1} 页"
            book.addSection(resource.title, resource)

            onProgress((index + 1) * 100 / pages.size)
        }

        // 写入文件
        val file = File(outputPath)
        FileOutputStream(file).use { out ->
            EpubWriter().write(book, out)
        }

        return file
    }

    /**
     * 生成PDF格式
     */
    private fun generatePDF(
        document: Document,
        pages: List<PageContent>,
        outputPath: String,
        onProgress: (Int) -> Unit
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

        pages.forEachIndexed { index, page ->
            val pdfPage = pdfDocument.startPage(pageInfo)
            val canvas = pdfPage.canvas

            // 绘制文本内容
            drawPageContent(canvas, page, pageInfo.pageWidth, pageInfo.pageHeight)

            pdfDocument.finishPage(pdfPage)
            onProgress((index + 1) * 100 / pages.size)
        }

        val file = File(outputPath)
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    /**
     * 生成TXT格式
     */
    private fun generateTXT(pages: List<PageContent>, outputPath: String): File {
        val file = File(outputPath)
        file.writeText(pages.joinToString("\n\n") { it.processedText })
        return file
    }

    /**
     * 生成HTML格式
     */
    private fun generateHTML(document: Document, pages: List<PageContent>, outputPath: String): File {
        val html = buildString {
            append("<!DOCTYPE html>\n")
            append("<html lang=\"${document.metadata?.language ?: "zh-CN"}\">\n")
            append("<head>\n")
            append("<meta charset=\"UTF-8\">\n")
            append("<title>${document.metadata?.title ?: document.fileName}</title>\n")
            append("<style>\n")
            append("body { font-family: 'Noto Sans SC', sans-serif; line-height: 1.8; max-width: 800px; margin: 0 auto; padding: 20px; }\n")
            append("h1 { text-align: center; }\n")
            append("p { text-indent: 2em; margin: 1em 0; }\n")
            append("img { max-width: 100%; height: auto; }\n")
            append("table { border-collapse: collapse; width: 100%; margin: 1em 0; }\n")
            append("td, th { border: 1px solid #ddd; padding: 8px; }\n")
            append("</style>\n")
            append("</head>\n")
            append("<body>\n")

            pages.forEachIndexed { index, page ->
                append("<div class=\"page\" id=\"page-${index + 1}\">\n")
                append("<h2>第 ${index + 1} 页</h2>\n")
                append(formatTextAsHTML(page.processedText))

                // 添加图片
                page.images.forEach { img ->
                    append("<div class=\"image\"><img src=\"${img.imagePath}\" alt=\"${img.caption ?: ""}\"></div>\n")
                }

                // 添加表格
                page.tables.forEach { table ->
                    append(table.htmlRepresentation ?: "")
                }

                append("</div>\n\n")
            }

            append("</body>\n")
            append("</html>")
        }

        val file = File(outputPath)
        file.writeText(html)
        return file
    }

    /**
     * 生成DOCX格式
     */
    private fun generateDOCX(document: Document, pages: List<PageContent>, outputPath: String): File {
        // DOCX生成需要Apache POI库
        // 这里简化实现
        val file = File(outputPath)
        file.writeText("DOCX格式暂不支持，请使用其他格式")
        return file
    }

    /**
     * 生成JSON格式
     */
    private fun generateJSON(pages: List<PageContent>, outputPath: String): File {
        val json = buildString {
            append("{\n")
            append("  \"pages\": [\n")
            pages.forEachIndexed { index, page ->
                append("    {\n")
                append("      \"pageNumber\": ${page.pageNumber},\n")
                append("      \"text\": \"${escapeJSON(page.processedText)}\",\n")
                append("      \"confidence\": ${page.confidence}\n")
                append("    }")
                if (index < pages.size - 1) append(",")
                append("\n")
            }
            append("  ]\n")
            append("}")
        }

        val file = File(outputPath)
        file.writeText(json)
        return file
    }

    /**
     * 构建页面HTML
     */
    private fun buildPageHTML(page: PageContent, pageNumber: Int): String {
        return buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            append("<!DOCTYPE html>\n")
            append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n")
            append("<head>\n")
            append("<title>第 $pageNumber 页</title>\n")
            append("<style>\n")
            append("body { font-family: sans-serif; line-height: 1.8; }\n")
            append("p { text-indent: 2em; margin: 0.5em 0; }\n")
            append("</style>\n")
            append("</head>\n")
            append("<body>\n")
            append(formatTextAsHTML(page.processedText))
            append("</body>\n")
            append("</html>")
        }
    }

    /**
     * 格式化文本为HTML
     */
    private fun formatTextAsHTML(text: String): String {
        val paragraphs = text.split("\n\n")
        return paragraphs.joinToString("\n") { para ->
            if (para.isNotBlank()) {
                "<p>${escapeHTML(para.trim())}</p>"
            } else {
                ""
            }
        }
    }

    /**
     * 绘制页面内容到PDF
     */
    private fun drawPageContent(canvas: Canvas, page: PageContent, width: Int, height: Int) {
        // 简化实现：绘制文本
        val lines = page.processedText.split("\n")
        var y = 50f

        lines.forEach { line ->
            canvas.drawText(line, 50f, y, android.graphics.Paint())
            y += 20f
        }
    }

    /**
     * HTML转义
     */
    private fun escapeHTML(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    /**
     * JSON转义
     */
    private fun escapeJSON(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
