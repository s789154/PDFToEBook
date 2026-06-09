package com.pdf2ebook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkInfo
import com.pdf2ebook.ui.components.PDFProcessingProgress
import com.pdf2ebook.worker.PDFProcessingWorker
import kotlinx.coroutines.flow.map

/**
 * 处理进度屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingProgressScreen(
    documentId: Long,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: ProcessingProgressViewModel = hiltViewModel()
) {
    val workInfo by viewModel.workInfo.collectAsState()
    
    // 监听处理状态
    LaunchedEffect(workInfo) {
        workInfo?.let { info ->
            if (info.state == WorkInfo.State.SUCCEEDED) {
                onCompleted()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("处理进度") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            workInfo?.let { info ->
                when (info.state) {
                    WorkInfo.State.RUNNING -> {
                        // 获取进度数据
                        val progress = info.progress
                        val stage = progress.getString(PDFProcessingWorker.PROGRESS_STAGE) 
                            ?: PDFProcessingWorker.STAGE_INITIALIZING
                        val percent = progress.getInt(PDFProcessingWorker.PROGRESS_PERCENT, 0)
                        val currentPage = progress.getInt(PDFProcessingWorker.PROGRESS_CURRENT_PAGE, 0)
                        val totalPages = progress.getInt(PDFProcessingWorker.PROGRESS_TOTAL_PAGES, 0)
                        
                        // 显示进度
                        PDFProcessingProgress(
                            stage = stage,
                            percent = percent,
                            currentPage = currentPage,
                            totalPages = totalPages
                        )
                        
                        // 提示信息
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "提示：处理过程中可以返回，应用会在后台继续处理",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        // 成功状态
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "处理完成！",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = "您的PDF已成功转换为电子书格式",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Button(onClick = onCompleted) {
                                    Text("查看电子书")
                                }
                            }
                        }
                    }
                    WorkInfo.State.FAILED -> {
                        // 失败状态
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "处理失败",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "处理过程中出现错误，请重试",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                                OutlinedButton(onClick = onBack) {
                                    Text("返回")
                                }
                            }
                        }
                    }
                    else -> {
                        // 其他状态（ENQUEUED, BLOCKED等）
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "准备处理...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            } ?: run {
                // 没有工作信息
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * 处理进度视图模型
 */
@HiltViewModel
class ProcessingProgressViewModel @Inject constructor(
    // 注入WorkManager
    // private val workManager: WorkManager
) : ViewModel() {
    
    var workInfo by mutableStateOf<WorkInfo?>(null)
        private set
    
    init {
        // 监听工作状态
        // workManager.getWorkInfoByIdLiveData(workId).observeForever {
        //     workInfo = it
        // }
    }
}
