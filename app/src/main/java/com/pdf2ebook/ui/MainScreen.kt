package com.pdf2ebook.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pdf2ebook.R
import com.pdf2ebook.ui.screens.*

/**
 * 主屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getScreenTitle(currentRoute)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("首页") },
                    selected = currentRoute == Screen.Home.route,
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    label = { Text("文档") },
                    selected = currentRoute == Screen.Documents.route,
                    onClick = { navController.navigate(Screen.Documents.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("设置") },
                    selected = currentRoute == Screen.Settings.route,
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onImportPDF = { navController.navigate(Screen.Import.route) }
                )
            }
            composable(Screen.Documents.route) {
                DocumentsScreen(
                    onDocumentClick = { docId ->
                        navController.navigate("${Screen.Preview.route}/$docId")
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onAPIConfigClick = { navController.navigate(Screen.APIConfig.route) }
                )
            }
            composable(Screen.Import.route) {
                ImportScreen(
                    onBack = { navController.popBackStack() },
                    onProcessingStarted = { docId ->
                        navController.navigate("${Screen.Processing.route}/$docId") {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
            composable("${Screen.Processing.route}/{documentId}") { backStackEntry ->
                val documentId = backStackEntry.arguments?.getString("documentId")?.toLongOrNull()
                ProcessingScreen(
                    documentId = documentId ?: 0,
                    onComplete = { navController.navigate(Screen.Documents.route) }
                )
            }
            composable("${Screen.Preview.route}/{documentId}") { backStackEntry ->
                val documentId = backStackEntry.arguments?.getString("documentId")?.toLongOrNull()
                PreviewScreen(
                    documentId = documentId ?: 0,
                    onExport = { navController.navigate(Screen.Export.route) }
                )
            }
            composable(Screen.Export.route) {
                ExportScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.APIConfig.route) {
                APIConfigScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * 屏幕路由定义
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Documents : Screen("documents")
    object Settings : Screen("settings")
    object Import : Screen("import")
    object Processing : Screen("processing")
    object Preview : Screen("preview")
    object Export : Screen("export")
    object APIConfig : Screen("api_config")
}

/**
 * 获取屏幕标题
 */
@Composable
fun getScreenTitle(route: String?): String {
    return when (route) {
        Screen.Home.route -> "PDF转电子书"
        Screen.Documents.route -> "我的文档"
        Screen.Settings.route -> "设置"
        Screen.Import.route -> "导入PDF"
        Screen.Processing.route -> "处理中"
        Screen.Preview.route -> "预览"
        Screen.Export.route -> "导出"
        Screen.APIConfig.route -> "API配置"
        else -> "PDF转电子书"
    }
}
