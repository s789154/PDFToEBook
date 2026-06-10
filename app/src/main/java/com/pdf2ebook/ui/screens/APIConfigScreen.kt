package com.pdf2ebook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.pdf2ebook.model.APIProvider
import com.pdf2ebook.model.ModelVersions

/**
 * API配置屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun APIConfigScreen(
    onBack: () -> Unit,
    viewModel: APIConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingConfigId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API配置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.configs) { config ->
                APIConfigCard(
                    config = config,
                    onEdit = { editingConfigId = config.id },
                    onDelete = { viewModel.deleteConfig(config.id) },
                    onToggle = { viewModel.toggleConfig(config.id, !config.isEnabled) }
                )
            }
        }

        // 添加/编辑对话框
        if (showAddDialog || editingConfigId != null) {
            APIConfigDialog(
                configId = editingConfigId,
                onDismiss = {
                    showAddDialog = false
                    editingConfigId = null
                },
                onSave = { config ->
                    viewModel.saveConfig(config)
                    showAddDialog = false
                    editingConfigId = null
                }
            )
        }
    }
}

/**
 * API配置卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun APIConfigCard(
    config: com.pdf2ebook.model.APIConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = config.provider.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = config.modelVersion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = { onToggle() }
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}

/**
 * API配置对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun APIConfigDialog(
    configId: Long?,
    onDismiss: () -> Unit,
    onSave: (com.pdf2ebook.model.APIConfig) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf(APIProvider.OPENAI) }
    var apiKey by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf(selectedProvider.defaultBaseUrl) }
    var selectedModel by remember { mutableStateOf("") }
    var maxTokens by remember { mutableStateOf("4096") }
    var temperature by remember { mutableStateOf("0.7") }
    var priority by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (configId == null) "添加API配置" else "编辑API配置") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("配置名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 提供商选择
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = selectedProvider.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("API提供商") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // API Key
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Base URL
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 模型版本选择
                val availableModels = when (selectedProvider) {
                    APIProvider.OPENAI -> ModelVersions.OPENAI_VERSIONS
                    APIProvider.ANTHROPIC -> ModelVersions.ANTHROPIC_VERSIONS
                    APIProvider.BAIDU -> ModelVersions.BAIDU_VERSIONS
                    APIProvider.ALIBABA -> ModelVersions.ALIBABA_VERSIONS
                    else -> emptyList()
                }

                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = { selectedModel = it },
                        label = { Text("模型版本") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Max Tokens
                OutlinedTextField(
                    value = maxTokens,
                    onValueChange = { maxTokens = it },
                    label = { Text("Max Tokens") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Temperature
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Temperature") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("优先级") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        com.pdf2ebook.model.APIConfig(
                            id = configId ?: 0,
                            name = name,
                            provider = selectedProvider,
                            apiKey = apiKey,
                            baseUrl = baseUrl,
                            modelVersion = selectedModel,
                            maxTokens = maxTokens.toIntOrNull() ?: 4096,
                            temperature = temperature.toFloatOrNull() ?: 0.7f,
                            priority = priority.toIntOrNull() ?: 0
                        )
                    )
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * API配置视图模型
 */
@HiltViewModel
class APIConfigViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(APIConfigUiState())
    val uiState: StateFlow<APIConfigUiState> = _uiState.asStateFlow()

    init {
        loadConfigs()
    }

    private fun loadConfigs() {
        viewModelScope.launch {
            // 从数据库加载配置
        }
    }

    fun saveConfig(config: com.pdf2ebook.model.APIConfig) {
        viewModelScope.launch {
            // 保存配置
        }
    }

    fun deleteConfig(id: Long) {
        viewModelScope.launch {
            // 删除配置
        }
    }

    fun toggleConfig(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            // 切换配置状态
        }
    }
}

data class APIConfigUiState(
    val configs: List<com.pdf2ebook.model.APIConfig> = emptyList(),
    val isLoading: Boolean = false
)
