package com.pdf2ebook.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.pdf2ebook.model.Document
import com.pdf2ebook.model.PageContent
import com.pdf2ebook.model.OutputFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 电子书生成器
 * 支持EPUB、PDF、TXT、HTML等多种格式导出
 * 使用原生Android API实现，不依赖外部库
 */
@Singleton
class EBookGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "EBookGenerator"
    }

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
            Log.e(TAG, "生成电子书失败", e)
            null
        }
    }

    /**
     * 生成EPUB格式
     * EPUB本质上是ZIP文件，包含HTML内容、CSS样式和元数据
     */
    private fun generateEPUB(
        document: Document,
        pages: List<PageContent>,
        outputPath: String,
        onProgress: (Int) -> Unit
    ): File {
        val file = File(outputPath)
        
        ZipOutputStream(FileOutputStream(file)).use { zipOut ->
            // 1. mimetype - 必须在ZIP中的第一个文件，且不能压缩
            zipOut.setMethod(ZipOutputStream.STORED)
            val mimetypeBytes = "application/epub+zip".toByteArray(Charsets.UTF_8)
            val mimetypeEntry = ZipEntry("mimetype").apply {
                size = mimetypeBytes.size.toLong()
                compressedSize = mimetypeBytes.size.toLong()
                crc = calculateCRC32(mimetypeBytes)
            }
            zipOut.putNextEntry(mimetypeEntry)
            zipOut.write(mimetypeBytes)
            zipOut.closeEntry()
            
            // 恢复压缩
            zipOut.setMethod(ZipOutputStream.DEFLATED)
            
            // 2. META-INF/container.xml
            zipOut.putNextEntry(ZipEntry("META-INF/container.xml"))
            zipOut.write(getContainerXML().toByteArray(Charsets.UTF_8))
            zipOut.closeEntry()
            
            // 3. OEBPS/content.opf
            zipOut.putNextEntry(ZipEntry("OEBPS/content.opf"))
            zipOut.write(getContentOPF(document, pages).toByteArray(Charsets.UTF_8))
            zipOut.closeEntry()
            
            // 4. OEBPS/toc.ncx
            zipOut.putNextEntry(ZipEntry("OEBPS/toc.ncx"))
            zipOut.write(getTOCNCX(document, pages).toByteArray(Charsets.UTF_8))
            
            // 5. OEBPS/style.css
            zipOut.putNextEntry(ZipEntry("OEBPS/style.css"))
            zipOut.write(getCSS().toByteArray(Charsets.UTF_8))
            zipOut.closeEntry()
            
            // 6. 内容页面
            pages.forEachIndexed { index, page ->
                val html = buildPageHTML(page, index + 1)
                zipOut.putNextEntry(ZipEntry("OEBPS/chapter_${index + 1}.html"))
                zipOut.write(html.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()
                
                onProgress((index + 1) * 100 / pages.size)
            }
        }
        
        return file
    }
    
    /**
     * 计算CRC32校验和
     */
    private fun calculateCRC32(data: ByteArray): Long {
        val crc = java.util.zip.CRC32()
        crc.update(data)
        return crc.value
    }
    
    /**
     * META-INF/container.xml
     */
    private fun getContainerXML(): String {
        return """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
    <rootfiles>
        <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
    </rootfiles>
</container>"""
    }
    
    /**
     * OEBPS/content.opf - 包文件
     */
    private fun getContentOPF(document: Document, pages: List<PageContent>): String {
        val title = escapeXML(document.metadata?.title ?: document.fileName)
        val author = escapeXML(document.metadata?.author ?: "Unknown")
        val language = document.metadata?.language ?: "zh-CN"
        
        val manifestItems = pages.mapIndexed { index, _ ->
            """    <item id="chapter${index + 1}" href="chapter_${index + 1}.html" media-type="application/xhtml+xml"/>"""
        }.joinToString("\n")
        
        val spineItems = pages.mapIndexed { index, _ ->
            """    <itemref idref="chapter${index + 1}"/>"""
        }.joinToString("\n")
        
        return """<?xml version="1.0" encoding="UTF-8"?>
<package version="2.0" xmlns="http://www.idpf.org/2007/opf">
    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:title>$title</dc:title>
        <dc:creator>$author</dc:creator>
        <dc:language>$language</dc:language>
        <dc:identifier id="bookid">urn:uuid:${java.util.UUID.randomUUID()}</dc:identifier>
    </metadata>
    <manifest>
        <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
        <item id="css" href="style.css" media-type="text/css"/>
$manifestItems
    </manifest>
    <spine toc="ncx">
$spineItems
    </spine>
</package>"""
    }
    
    /**
     * OEBPS/toc.ncx - 目录文件
     */
    private fun getTOCNCX(document: Document, pages: List<PageContent>): String {
        val title = escapeXML(document.metadata?.title ?: document.fileName)
        
        val navPoints = pages.mapIndexed { index, _ ->
            """        <navPoint id="navpoint-${index + 1}" playOrder="${index + 1}">
            <navLabel>
                <text>第 ${index + 1} 页</text>
            </navLabel>
            <content src="chapter_${index + 1}.html"/>
        </navPoint>"""
        }.joinToString("\n")
        
        return """<?xml version="1.0" encoding="UTF-8"?>
<ncx version="2005-1" xmlns="http://www.daisy.org/z3986/2005/ncx/">
    <head>
        <meta name="dtb:uid" content="urn:uuid:${java.util.UUID.randomUUID()}"/>
        <meta name="dtb:depth" content="1"/>
        <meta name="dtb:totalPageCount" content="0"/>
        <meta name="dtb:maxPageNumber" content="0"/>
    </head>
    <docTitle>
        <text>$title</text>
    </docTitle>
    <navMap>
$navPoints
    </navMap>
</ncx>"""
    }
    
    /**
     * CSS样式
     */
    private fun getCSS(): String {
        return """body {
    font-family: 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
    line-height: 1.8;
    margin: 2em;
    color: #333;
}

h1, h2, h3 {
    color: #222;
    margin-top: 1.5em;
    margin-bottom: 0.5em;
}

p {
    text-indent: 2em;
    margin: 0.8em 0;
    text-align: justify;
}

img {
    max-width: 100%;
    height: auto;
    display: block;
    margin: 1em auto;
}

table {
    border-collapse: collapse;
    width: 100%;
    margin: 1em 0;
}

td, th {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: left;
}

th {
    background-color: #f5f5f5;
}"""
    }
    
    /**
     * 构建页面HTML
     */
    private fun buildPageHTML(page: PageContent, pageNumber: Int): String {
        return """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>第 $pageNumber 页</title>
    <link rel="stylesheet" href="style.css" type="text/css"/>
</head>
<body>
    <h2>第 $pageNumber 页</h2>
    ${formatTextAsHTML(page.processedText)}
    ${page.images.joinToString("\n") { img ->
        if (img.imagePath.isNotEmpty()) {
            """<img src="${img.imagePath}" alt="${escapeXML(img.caption ?: "")}"/>"""
        } else ""
    }}
    ${page.tables.joinToString("\n") { table ->
        table.htmlRepresentation ?: ""
    }}
</body>
</html>"""
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
            append("<title>${escapeHTML(document.metadata?.title ?: document.fileName)}</title>\n")
            append("<style>\n")
            append(getCSS())
            append("</style>\n")
            append("</head>\n")
            append("<body>\n")

            pages.forEachIndexed { index, page ->
                append("<div class=\"page\" id=\"page-${index + 1}\">\n")
                append("<h2>第 ${index + 1} 页</h2>\n")
                append(formatTextAsHTML(page.processedText))

                // 添加图片
                page.images.forEach { img ->
                    append("<div class=\"image\"><img src=\"${img.imagePath}\" alt=\"${escapeHTML(img.caption ?: "")}\"></div>\n")
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
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
        }
        
        val lines = page.processedText.split("\n")
        var y = 50f
        val lineHeight = 20f

        lines.forEach { line ->
            if (y + lineHeight < height - 50) {
                canvas.drawText(line, 50f, y, paint)
                y += lineHeight
            }
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
     * XML转义
     */
    private fun escapeXML(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
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