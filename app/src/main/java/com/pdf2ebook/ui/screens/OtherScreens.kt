package com.pdf2ebook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 文档列表屏幕
 */
@Composable
fun DocumentsScreen(
    onDocumentClick: (Long) -> Unit,
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    // 文档列表实现
    Text("文档列表")
}

/**
 * 设置屏幕
 */
@Composable
fun SettingsScreen(
    onAPIConfigClick: () -> Unit
) {
    // 设置界面实现
    Text("设置")
}

/**
 * 处理进度屏幕
 */
@Composable
fun ProcessingScreen(
    documentId: Long,
    onComplete: () -> Unit
) {
    // 处理进度界面
    Text("处理中...")
}

/**
 * 预览屏幕
 */
@Composable
fun PreviewScreen(
    documentId: Long,
    onExport: () -> Unit
) {
    // 预览界面
    Text("预览")
}

/**
 * 导出屏幕
 */
@Composable
fun ExportScreen(
    onBack: () -> Unit
) {
    // 导出界面
    Text("导出")
}

// 视图模型
@HiltViewModel
class DocumentsViewModel @Inject constructor() : ViewModel()

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel()

@HiltViewModel
class ProcessingViewModel @Inject constructor() : ViewModel()

@HiltViewModel
class PreviewViewModel @Inject constructor() : ViewModel()

@HiltViewModel
class ExportViewModel @Inject constructor() : ViewModel()
