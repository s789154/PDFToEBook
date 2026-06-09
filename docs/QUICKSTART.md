# 快速开始指南

5分钟快速上手PDF转电子书应用。

## 第一步：安装应用

1. 下载APK文件
2. 允许安装未知来源应用
3. 完成安装

## 第二步：配置API（必需）

### 使用文心一言（推荐国内用户）

1. 访问 https://console.bce.baidu.com/qianfan/
2. 创建应用获取API Key和Secret Key
3. 在应用中添加配置：
   - 名称：文心一言
   - 提供商：百度文心一言
   - API Key：`你的API Key,你的Secret Key`
   - 模型：ernie-bot-turbo

### 使用OpenAI（推荐海外用户）

1. 访问 https://platform.openai.com/api-keys
2. 创建API Key
3. 在应用中添加配置：
   - 名称：OpenAI
   - 提供商：OpenAI
   - API Key：`sk-xxx`
   - 模型：gpt-3.5-turbo

## 第三步：导入PDF

1. 点击首页"导入PDF文件"
2. 选择PDF文件
3. 查看PDF信息

## 第四步：设置参数（可选）

### 推荐设置（首次使用）

```
OCR引擎: Tesseract
OCR语言: chi_sim+eng
启用AI优化: 开启
水印过滤: 开启
输出格式: EPUB
质量级别: BALANCED
```

### 快速设置（追求速度）

```
OCR引擎: ML Kit
启用AI优化: 关闭
输出格式: TXT
质量级别: FAST
```

### 高质量设置（追求效果）

```
OCR引擎: PaddleOCR
启用AI优化: 开启
图片增强: 开启
图表识别: 开启
输出格式: EPUB
质量级别: HIGH_QUALITY
```

## 第五步：开始处理

1. 点击"开始处理"
2. 等待处理完成
3. 查看处理结果

## 第六步：导出文件

1. 在"我的文档"中查看
2. 点击预览
3. 选择导出格式
4. 保存或分享

## 常见问题快速解答

**Q: 处理很慢怎么办？**  
A: 关闭AI优化，使用FAST模式

**Q: OCR识别不准确？**  
A: 确认选择了正确的OCR语言

**Q: API调用失败？**  
A: 检查API Key和网络连接

**Q: 如何降低成本？**  
A: 使用国内API，关闭不必要的优化

## 下一步

- 阅读完整[使用文档](../README.md)
- 查看[API配置详细说明](API_CONFIGURATION.md)
- 了解高级功能和优化技巧

## 需要帮助？

- 查看[常见问题](../README.md#常见问题)
- 提交[Issue](https://github.com/yourusername/PDFToEBook/issues)
