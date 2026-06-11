package com.pdf2ebook.data

import androidx.room.TypeConverter
import com.pdf2ebook.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room 数据库类型转换器
 * 用于处理复杂对象的存储和读取
 */
class Converters {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // ========== DocumentMetadata ==========
    @TypeConverter
    fun fromDocumentMetadata(value: DocumentMetadata?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toDocumentMetadata(value: String?): DocumentMetadata? {
        return value?.let { json.decodeFromString<DocumentMetadata>(it) }
    }
    
    // ========== ProcessingSettings ==========
    @TypeConverter
    fun fromProcessingSettings(value: ProcessingSettings?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toProcessingSettings(value: String?): ProcessingSettings? {
        return value?.let { json.decodeFromString<ProcessingSettings>(it) }
    }
    
    // ========== PageLayout ==========
    @TypeConverter
    fun fromPageLayout(value: PageLayout?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toPageLayout(value: String?): PageLayout? {
        return value?.let { json.decodeFromString<PageLayout>(it) }
    }
    
    // ========== List<PageImage> ==========
    @TypeConverter
    fun fromPageImageList(value: List<PageImage>?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toPageImageList(value: String?): List<PageImage>? {
        return value?.let { json.decodeFromString<List<PageImage>>(it) }
    }
    
    // ========== List<PageTable> ==========
    @TypeConverter
    fun fromPageTableList(value: List<PageTable>?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toPageTableList(value: String?): List<PageTable>? {
        return value?.let { json.decodeFromString<List<PageTable>>(it) }
    }
    
    // ========== List<PageChart> ==========
    @TypeConverter
    fun fromPageChartList(value: List<PageChart>?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toPageChartList(value: String?): List<PageChart>? {
        return value?.let { json.decodeFromString<List<PageChart>>(it) }
    }
    
    // ========== Map<String, String> ==========
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return value?.let { json.decodeFromString<Map<String, String>>(it) }
    }
}
