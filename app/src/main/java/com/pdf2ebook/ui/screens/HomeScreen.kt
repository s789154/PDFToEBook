package com.pdf2ebook.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pdf2ebook.ui.components.FeatureCard
import com.pdf2ebook.ui.components.StatCard

/**
 * 首页屏幕
 */
@Composable
fun HomeScreen(
    onImportPDF: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 统计卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "已处理",
                value = uiState.processedCount.toString(),
                icon = Icons.Default.Description
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "API配置",
                value = uiState.apiConfigCount.toString(),
                icon = Icons.Default.Folder
            )
        }

        // 功能介绍
        Text(
            text = "核心功能",
            style = MaterialTheme.typography.titleMedium
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureCard(
                title = "PDF扫描识别",
                description = "高精度OCR识别，支持多语言",
                icon = Icons.Default.Description
            )
            FeatureCard(
                title = "AI智能处理",
                description = "多模型并行调用，自动纠错优化",
                icon = Icons.Default.Add
            )
            FeatureCard(
                title = "电子书导出",
                description = "支持EPUB、PDF、HTML等多种格式",
                icon = Icons.Default.Folder
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 导入按钮
        Button(
            onClick = onImportPDF,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("导入PDF文件")
        }
    }
}

/**
 * 首页视图模型
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        // 加载统计数据
        viewModelScope.launch {
            // 从数据库查询
        }
    }
}

/**
 * 首页UI状态
 */
data class HomeUiState(
    val processedCount: Int = 0,
    val apiConfigCount: Int = 0,
    val isLoading: Boolean = false
)
