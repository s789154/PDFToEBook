package com.pdf2ebook.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkInfo
import com.pdf2ebook.model.*
import com.pdf2ebook.ui.components.PDFProcessingProgress
import com.pdf2ebook.worker.PDFProcessingWorker

/**
 * 导入屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBack: () -> Unit,
    onProcessingStarted: (Long) -> Unit,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var settings by remember { mutableStateOf(ProcessingSettings()) }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            viewModel.loadPDFInfo(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入PDF") },
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
            // PDF选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { pdfPicker.launch(arrayOf("application/pdf")) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "选择PDF文件",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = uiState.fileName ?: "点击选择文件",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.Description, contentDescription = null)
                }
            }

            // OCR设置
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "识别设置",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // OCR引擎选择
                    var expandedOcrEngine by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedOcrEngine,
                        onExpandedChange = { expandedOcrEngine = it }
                    ) {
                        OutlinedTextField(
                            value = settings.ocrEngine.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("OCR引擎") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOcrEngine) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedOcrEngine,
                            onDismissRequest = { expandedOcrEngine = false }
                        ) {
                            OCREngine.values().forEach { engine ->
                                DropdownMenuItem(
                                    text = { Text(engine.name) },
                                    onClick = {
                                        settings = settings.copy(ocrEngine = engine)
                                        expandedOcrEngine = false
                                    }
                                )
                            }
                        }
                    }

                    // 语言选择
                    OutlinedTextField(
                        value = settings.ocrLanguage,
                        onValueChange = { settings = settings.copy(ocrLanguage = it) },
                        label = { Text("OCR语言") },
                        placeholder = { Text("chi_sim+eng") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // AI处理设置
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AI处理",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("启用AI优化")
                        Switch(
                            checked = settings.parallelAPICalls,
                            onCheckedChange = { settings = settings.copy(parallelAPICalls = it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("水印过滤")
                        Switch(
                            checked = settings.enableWatermarkRemoval,
                            onCheckedChange = { settings = settings.copy(enableWatermarkRemoval = it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("图片增强")
                        Switch(
                            checked = settings.enableImageEnhancement,
                            onCheckedChange = { settings = settings.copy(enableImageEnhancement = it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("图表识别")
                        Switch(
                            checked = settings.enableChartRecognition,
                            onCheckedChange = { settings = settings.copy(enableChartRecognition = it) }
                        )
                    }
                }
            }

            // 输出设置
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "输出设置",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // 输出格式
                    var expandedOutputFormat by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedOutputFormat,
                        onExpandedChange = { expandedOutputFormat = it }
                    ) {
                        OutlinedTextField(
                            value = settings.outputFormat.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("输出格式") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOutputFormat) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedOutputFormat,
                            onDismissRequest = { expandedOutputFormat = false }
                        ) {
                            OutputFormat.values().forEach { format ->
                                DropdownMenuItem(
                                    text = { Text(format.name) },
                                    onClick = {
                                        settings = settings.copy(outputFormat = format)
                                        expandedOutputFormat = false
                                    }
                                )
                            }
                        }
                    }

                    // 质量级别
                    var expandedQuality by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedQuality,
                        onExpandedChange = { expandedQuality = it }
                    ) {
                        OutlinedTextField(
                            value = settings.qualityLevel.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("质量级别") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedQuality) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedQuality,
                            onDismissRequest = { expandedQuality = false }
                        ) {
                            QualityLevel.values().forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level.name) },
                                    onClick = {
                                        settings = settings.copy(qualityLevel = level)
                                        expandedQuality = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 处理进度显示
            if (uiState.isProcessing) {
                PDFProcessingProgress(
                    stage = uiState.processingStage,
                    percent = uiState.processingPercent,
                    currentPage = uiState.processingCurrentPage,
                    totalPages = uiState.processingTotalPages
                )
            }

            // 开始处理按钮
            Button(
                onClick = {
                    selectedUri?.let { uri ->
                        viewModel.startProcessing(uri, settings) { docId ->
                            onProcessingStarted(docId)
                        }
                    }
                },
                enabled = selectedUri != null && !uiState.isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始处理")
                }
            }
        }
    }
}

/**
 * 导入视图模型
 */
@HiltViewModel
class ImportViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun loadPDFInfo(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // 加载PDF信息
            _uiState.update { it.copy(isLoading = false, fileName = "example.pdf") }
        }
    }

    fun startProcessing(uri: Uri, settings: ProcessingSettings, onStart: (Long) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            // 创建文档记录并启动WorkManager
            onStart(1L)
            
            // 监听WorkManager进度（示例）
            // 实际实现中应该通过WorkManager.getWorkInfoByIdLiveData监听
            // 这里简化处理，模拟进度更新
            viewModelScope.launch {
                // 模拟进度更新
                var progress = 0
                while (progress < 100) {
                    delay(1000)
                    progress += 10
                    _uiState.update { state ->
                        state.copy(
                            processingPercent = progress,
                            processingStage = when {
                                progress < 20 -> PDFProcessingWorker.STAGE_RENDERING
                                progress < 70 -> PDFProcessingWorker.STAGE_OCR
                                progress < 95 -> PDFProcessingWorker.STAGE_AI_PROCESSING
                                else -> PDFProcessingWorker.STAGE_SAVING
                            }
                        )
                    }
                }
            }
        }
    }
}

data class ImportUiState(
    val fileName: String? = null,
    val pageCount: Int = 0,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val processingStage: String = PDFProcessingWorker.STAGE_INITIALIZING,
    val processingPercent: Int = 0,
    val processingCurrentPage: Int = 0,
    val processingTotalPages: Int = 0
)
