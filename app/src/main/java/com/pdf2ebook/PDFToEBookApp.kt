package com.pdf2ebook

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * PDF转电子书应用主类
 * 负责初始化Hilt依赖注入和WorkManager
 */
@HiltAndroidApp
class PDFToEBookApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val processingChannel = NotificationChannel(
                CHANNEL_ID_PROCESSING,
                "文档处理",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示PDF处理进度"
            }

            val exportChannel = NotificationChannel(
                CHANNEL_ID_EXPORT,
                "导出任务",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "显示导出进度"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(processingChannel, exportChannel))
        }
    }

    companion object {
        const val CHANNEL_ID_PROCESSING = "pdf_processing"
        const val CHANNEL_ID_EXPORT = "pdf_export"
    }
}
