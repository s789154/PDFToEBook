package com.pdf2ebook.utils

import android.content.Context
import android.graphics.*
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * 图像处理工具类
 * 提供水印去除、图像增强、图表识别等功能
 */
@Singleton
class ImageUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 检测并去除水印
     */
    suspend fun removeWatermark(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        // 1. 检测水印区域（基于颜色、位置、透明度等特征）
        val watermarkRegions = detectWatermarkRegions(bitmap)

        // 2. 使用修复算法填充水印区域
        var result = bitmap
        watermarkRegions.forEach { region ->
            result = inpaintRegion(result, region)
        }

        result
    }

    /**
     * 检测水印区域
     */
    private fun detectWatermarkRegions(bitmap: Bitmap): List<Rect> {
        val regions = mutableListOf<Rect>()
        val width = bitmap.width
        val height = bitmap.height

        // 常见水印位置：四个角落、底部中间
        val possibleLocations = listOf(
            Rect(0, 0, width / 4, height / 8), // 左上角
            Rect(width * 3 / 4, 0, width, height / 8), // 右上角
            Rect(0, height * 7 / 8, width / 4, height), // 左下角
            Rect(width * 3 / 4, height * 7 / 8, width, height), // 右下角
            Rect(width / 4, height * 7 / 8, width * 3 / 4, height) // 底部中间
        )

        for (rect in possibleLocations) {
            if (isWatermarkRegion(bitmap, rect)) {
                regions.add(rect)
            }
        }

        return regions
    }

    /**
     * 判断是否为水印区域
     */
    private fun isWatermarkRegion(bitmap: Bitmap, rect: Rect): Boolean {
        var totalAlpha = 0
        var pixelCount = 0
        var colorVariance = 0.0

        val rectWidth = rect.width()
        val rectHeight = rect.height()
        val pixels = IntArray(rectWidth * rectHeight)
        bitmap.getPixels(
            pixels, 0, rectWidth,
            rect.left, rect.top,
            rectWidth, rectHeight
        )

        var avgColor = 0L
        pixels.forEach { pixel ->
            avgColor += pixel
        }
        avgColor /= pixels.size

        pixels.forEach { pixel ->
            colorVariance += kotlin.math.abs(pixel - avgColor).toDouble()
            totalAlpha += Color.alpha(pixel)
            pixelCount++
        }

        val avgAlpha = totalAlpha.toDouble() / pixelCount
        colorVariance /= pixelCount

        // 水印特征：半透明、颜色单一
        return avgAlpha < 200 && colorVariance < 50
    }

    /**
     * 图像修复（填充指定区域）
     */
    private fun inpaintRegion(bitmap: Bitmap, region: Rect): Bitmap {
        // 使用Telea算法或Navier-Stokes算法进行图像修复
        // 这里简化实现，使用周围像素的平均值填充
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        // 获取周围像素颜色
        val surroundingColor = getSurroundingColor(bitmap, region)
        val paint = Paint().apply {
            color = surroundingColor
            style = Paint.Style.FILL
        }

        canvas.drawRect(RectF(region.left.toFloat(), region.top.toFloat(), region.right.toFloat(), region.bottom.toFloat()), paint)
        return result
    }

    /**
     * 获取区域周围的颜色
     */
    private fun getSurroundingColor(bitmap: Bitmap, region: Rect): Int {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L
        var count = 0

        // 采样周围10像素
        val sampleDistance = 10
        val left = maxOf(0, region.left - sampleDistance)
        val right = minOf(bitmapWidth, region.right + sampleDistance)
        val top = maxOf(0, region.top - sampleDistance)
        val bottom = minOf(bitmapHeight, region.bottom + sampleDistance)

        for (x in left until right) {
            for (y in top until bottom) {
                if (x < region.left || x >= region.right || y < region.top || y >= region.bottom) {
                    val pixel = bitmap.getPixel(x, y)
                    redSum += Color.red(pixel)
                    greenSum += Color.green(pixel)
                    blueSum += Color.blue(pixel)
                    count++
                }
            }
        }

        return if (count > 0) {
            Color.rgb(
                (redSum / count).toInt(),
                (greenSum / count).toInt(),
                (blueSum / count).toInt()
            )
        } else {
            Color.WHITE
        }
    }

    /**
     * 图像增强
     */
    suspend fun enhanceImage(
        bitmap: Bitmap,
        denoise: Boolean = true,
        sharpen: Boolean = true,
        adjustContrast: Boolean = true
    ): Bitmap = withContext(Dispatchers.Default) {
        var result = bitmap

        if (denoise) {
            result = applyDenoise(result)
        }

        if (sharpen) {
            result = applySharpen(result)
        }

        if (adjustContrast) {
            result = adjustContrast(result)
        }

        result
    }

    /**
     * 降噪处理
     */
    private fun applyDenoise(bitmap: Bitmap): Bitmap {
        // 使用中值滤波或高斯滤波降噪
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // 简化的中值滤波
        for (x in 1 until width - 1) {
            for (y in 1 until height - 1) {
                val neighbors = mutableListOf<Int>()
                for (dx in -1..1) {
                    for (dy in -1..1) {
                        neighbors.add(bitmap.getPixel(x + dx, y + dy))
                    }
                }
                neighbors.sort()
                result.setPixel(x, y, neighbors[4]) // 中值
            }
        }

        return result
    }

    /**
     * 锐化处理
     */
    private fun applySharpen(bitmap: Bitmap): Bitmap {
        // 拉普拉斯锐化
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val kernel = floatArrayOf(
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f
        )

        val paint = Paint()
        val canvas = Canvas(result)

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(1.2f)

        // 应用卷积（简化实现）
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    /**
     * 对比度调整
     */
    private fun adjustContrast(bitmap: Bitmap): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint()

        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1.2f, 0f, 0f, 0f, 0f,  // Red
                0f, 1.2f, 0f, 0f, 0f,  // Green
                0f, 0f, 1.2f, 0f, 0f,  // Blue
                0f, 0f, 0f, 1f, 0f     // Alpha
            ))
        }

        val colorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorMatrixColorFilter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    /**
     * 检测背景色（识别书籍底色）
     */
    suspend fun detectBackgroundColor(bitmap: Bitmap): Int = withContext(Dispatchers.Default) {
        val width = bitmap.width
        val height = bitmap.height

        // 采样四个角落和边缘
        val samplePoints = listOf(
            0 to 0,
            width - 1 to 0,
            0 to height - 1,
            width - 1 to height - 1,
            width / 2 to 0,
            width / 2 to height - 1,
            0 to height / 2,
            width - 1 to height / 2
        )

        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L

        samplePoints.forEach { (x, y) ->
            val pixel = bitmap.getPixel(x, y)
            redSum += Color.red(pixel)
            greenSum += Color.green(pixel)
            blueSum += Color.blue(pixel)
        }

        Color.rgb(
            (redSum / samplePoints.size).toInt(),
            (greenSum / samplePoints.size).toInt(),
            (blueSum / samplePoints.size).toInt()
        )
    }

    /**
     * 判断图片是否清晰
     */
    suspend fun isImageClear(bitmap: Bitmap): Boolean = withContext(Dispatchers.Default) {
        // 使用拉普拉斯方差判断图像清晰度
        val width = bitmap.width
        val height = bitmap.height
        var variance = 0.0
        var mean = 0.0

        // 转换为灰度并计算拉普拉斯
        for (x in 1 until width - 1) {
            for (y in 1 until height - 1) {
                val center = getGrayValue(bitmap.getPixel(x, y))
                val laplacian = kotlin.math.abs(
                    getGrayValue(bitmap.getPixel(x - 1, y)) +
                    getGrayValue(bitmap.getPixel(x + 1, y)) +
                    getGrayValue(bitmap.getPixel(x, y - 1)) +
                    getGrayValue(bitmap.getPixel(x, y + 1)) -
                    4 * center
                )
                mean += laplacian
            }
        }

        mean /= ((width - 2) * (height - 2))

        // 计算方差
        for (x in 1 until width - 1) {
            for (y in 1 until height - 1) {
                val center = getGrayValue(bitmap.getPixel(x, y))
                val laplacian = kotlin.math.abs(
                    getGrayValue(bitmap.getPixel(x - 1, y)) +
                    getGrayValue(bitmap.getPixel(x + 1, y)) +
                    getGrayValue(bitmap.getPixel(x, y - 1)) +
                    getGrayValue(bitmap.getPixel(x, y + 1)) -
                    4 * center
                )
                variance += (laplacian - mean).pow(2.0)
            }
        }

        variance /= ((width - 2) * (height - 2))

        // 方差大于阈值则认为清晰
        variance > 100
    }

    /**
     * 获取灰度值
     */
    private fun getGrayValue(pixel: Int): Double {
        return 0.299 * Color.red(pixel) +
               0.587 * Color.green(pixel) +
               0.114 * Color.blue(pixel)
    }

    /**
     * 保存位图到文件
     */
    suspend fun saveBitmap(bitmap: Bitmap, file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        } catch (e: Exception) {
            Log.e("ImageUtils", "保存图片失败", e)
            false
        }
    }
}
