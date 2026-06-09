# PDF转电子书 Android应用

一款功能强大的PDF扫描版转电子书工具，支持AI智能处理、多模型并行调用、多种格式导出。

## 功能特性

### 核心功能

- **PDF扫描识别**
  - 高精度OCR识别（Tesseract/ML Kit/PaddleOCR）
  - 多语言支持（中文、英文、日文等）
  - 自动检测背景色和书籍底色

- **AI智能处理**
  - 多模型并行调用（OpenAI、Claude、文心一言、通义千问等）
  - 自动纠错和文本优化
  - 图表识别与可编辑转换

- **图像处理**
  - 水印自动检测与去除
  - 图片增强与修复
  - 模糊图片清晰化处理

- **电子书生成**
  - 支持EPUB、PDF、TXT、HTML、JSON多种格式
  - 保持原版视觉效果
  - 文档体积优化

## 安装说明

### 系统要求

- Android 8.0 (API 26) 或更高版本
- 至少2GB可用存储空间
- 网络连接（用于AI API调用）

### 安装方式

1. **从GitHub Release下载**
   - 访问项目的Release页面
   - 下载最新的APK文件
   - 安装到Android设备

2. **自行编译**
   ```bash
   git clone https://github.com/yourusername/PDFToEBook.git
   cd PDFToEBook
   ./gradlew assembleRelease
   ```

## 使用指南

### 1. 配置API密钥

首次使用需要配置大模型API密钥：

1. 打开应用，进入"设置"页面
2. 点击"API配置"
3. 点击右上角"+"添加配置
4. 填写以下信息：
   - **配置名称**：自定义名称（如"我的GPT-4"）
   - **API提供商**：选择提供商（OpenAI、Claude等）
   - **API Key**：输入您的API密钥
   - **Base URL**：使用默认或自定义URL
   - **模型版本**：选择要使用的模型

#### 支持的API提供商

| 提供商 | 获取API Key | 默认Base URL |
|--------|------------|--------------|
| OpenAI | https://platform.openai.com/api-keys | https://api.openai.com/v1 |
| Anthropic | https://console.anthropic.com/ | https://api.anthropic.com/v1 |
| 百度文心一言 | https://console.bce.baidu.com/qianfan/ | https://aip.baidubce.com/rpc/2.0/ai_custom/v1 |
| 阿里通义千问 | https://dashscope.console.aliyun.com/ | https://dashscope.aliyuncs.com/api/v1 |
| 智谱AI | https://open.bigmodel.cn/ | https://open.bigmodel.cn/api/paas/v3 |
| Moonshot | https://platform.moonshot.cn/ | https://api.moonshot.cn/v1 |
| DeepSeek | https://platform.deepseek.com/ | https://api.deepseek.com/v1 |

#### 百度文心一言特殊说明

百度API Key格式为：`API Key,Secret Key`

例如：`your_api_key,your_secret_key`

### 2. 导入PDF文件

1. 在首页点击"导入PDF文件"按钮
2. 选择存储中的PDF文件
3. 应用会自动读取PDF信息

### 3. 设置处理参数

#### OCR设置

- **OCR引擎**
  - **Tesseract**：开源OCR，支持多语言，准确率中等
  - **ML Kit**：Google提供的OCR，识别速度快
  - **PaddleOCR**：百度开源OCR，中文识别效果好

- **OCR语言**
  - 中文简体：`chi_sim`
  - 中文繁体：`chi_tra`
  - 英文：`eng`
  - 日文：`jpn`
  - 多语言：使用`+`连接，如`chi_sim+eng`

#### AI处理设置

- **启用AI优化**：开启后使用大模型优化OCR结果
- **水印过滤**：自动检测并去除水印
- **图片增强**：增强图片清晰度
- **图表识别**：识别图表并转换为可编辑格式

#### 输出设置

- **输出格式**
  - **EPUB**：标准电子书格式，支持大多数阅读器
  - **PDF**：重新排版的PDF文档
  - **TXT**：纯文本格式
  - **HTML**：网页格式，可在浏览器中查看
  - **JSON**：结构化数据，便于程序处理

- **质量级别**
  - **FAST**：快速处理，质量较低
  - **BALANCED**：平衡模式（推荐）
  - **HIGH_QUALITY**：高质量处理，耗时较长

### 4. 开始处理

点击"开始处理"按钮，应用会：

1. 渲染PDF页面
2. 进行OCR识别
3. 使用AI优化文本
4. 检测和处理图片、图表
5. 生成电子书文件

处理过程中可在通知栏查看进度。

### 5. 查看和导出

处理完成后：

1. 在"我的文档"中查看列表
2. 点击文档预览内容
3. 选择导出格式
4. 保存到设备或分享

## 高级功能

### 多API并行调用

启用"并行API调用"后，应用会同时调用多个配置的API：

1. 提高处理成功率
2. 自动选择最佳结果
3. 根据优先级排序

配置优先级：
- 数字越大优先级越高
- 建议为常用API设置较高优先级

### 自定义API配置

支持自定义API配置：

1. 选择"自定义"提供商
2. 填写自定义Base URL
3. 配置自定义Headers（如需要）
4. 适配OpenAI兼容格式的API

### OCR语言数据管理

首次使用OCR需要下载语言数据：

- Tesseract语言数据存储在：`/data/data/com.pdf2ebook/files/tesseract/tessdata/`
- 支持手动复制语言数据文件
- 语言数据文件格式：`[语言代码].traineddata`

## 性能优化建议

### 提高处理速度

1. 使用FAST质量级别
2. 关闭图片增强功能
3. 选择较快的OCR引擎（ML Kit）
4. 使用响应速度快的API

### 提高处理质量

1. 使用HIGH_QUALITY质量级别
2. 开启所有处理选项
3. 使用高精度OCR引擎（PaddleOCR）
4. 使用高质量大模型（GPT-4、Claude 3 Opus）

### 降低成本

1. 使用国内API（文心一言、通义千问）
2. 合理设置Max Tokens
3. 仅在必要时启用AI优化

## 故障排除

### OCR识别不准确

- 确认选择了正确的OCR语言
- 尝试不同的OCR引擎
- 检查PDF扫描质量
- 启用图片增强功能

### API调用失败

- 检查API Key是否正确
- 确认Base URL是否正确
- 检查网络连接
- 查看API余额是否充足

### 百度API特殊问题

- 确认API Key格式：`API Key,Secret Key`
- 检查是否已开通相应服务
- 确认账户余额充足

### 导出文件过大

- 降低压缩质量
- 关闭图片保留选项
- 使用TXT格式导出

## 开发说明

### 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose
- **架构**：MVVM + Clean Architecture
- **依赖注入**：Hilt
- **数据库**：Room
- **网络**：Retrofit + OkHttp
- **异步处理**：Kotlin Coroutines + Flow
- **后台任务**：WorkManager

### 项目结构

```
app/src/main/java/com/pdf2ebook/
├── data/           # 数据层
│   ├── AppDatabase.kt
│   └── DAOs.kt
├── model/          # 数据模型
│   ├── APIConfig.kt
│   ├── Document.kt
│   └── PageContent.kt
├── network/        # 网络层
│   ├── APIModels.kt
│   ├── LLMApiService.kt
│   └── LLMApiManager.kt
├── ocr/            # OCR模块
│   └── OCRProcessor.kt
├── utils/          # 工具类
│   ├── ImageUtils.kt
│   ├── PDFProcessor.kt
│   └── EBookGenerator.kt
├── worker/         # 后台任务
│   └── PDFProcessingWorker.kt
└── ui/             # UI层
    ├── MainScreen.kt
    ├── screens/
    ├── components/
    └── theme/
```

### 本地开发

1. 克隆项目
   ```bash
   git clone https://github.com/yourusername/PDFToEBook.git
   ```

2. 使用Android Studio打开项目

3. 同步Gradle依赖

4. 运行到模拟器或真机

### 构建APK

```bash
# Debug版本
./gradlew assembleDebug

# Release版本
./gradlew assembleRelease
```

## 常见问题

**Q: 支持哪些PDF格式？**  
A: 支持所有标准PDF格式，包括扫描版和文本版。

**Q: 处理一个PDF需要多长时间？**  
A: 取决于PDF页数、选择的处理选项和API响应速度。一般每页需要3-10秒。

**Q: 是否支持离线使用？**  
A: OCR功能支持离线使用，但AI优化功能需要网络连接。

**Q: 如何保护我的API密钥？**  
A: API密钥存储在本地数据库中，不会上传到服务器。建议定期更换密钥。

**Q: 支持哪些电子书阅读器？**  
A: EPUB格式支持大多数主流阅读器，如Kindle、iBooks、多看阅读等。

## 许可证

本项目采用MIT许可证。详见[LICENSE](LICENSE)文件。

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

- 项目主页：https://github.com/yourusername/PDFToEBook
- 问题反馈：https://github.com/yourusername/PDFToEBook/issues
