# API配置详细说明

本文档详细介绍如何配置各种大模型API。

## OpenAI配置

### 获取API Key

1. 访问 https://platform.openai.com/api-keys
2. 登录或注册OpenAI账号
3. 点击"Create new secret key"
4. 复制生成的API Key（格式：`sk-...`）

### 配置参数

| 参数 | 值 |
|------|-----|
| 配置名称 | OpenAI GPT-4（自定义） |
| API提供商 | OpenAI |
| API Key | sk-xxxxxxxxxxxxxxxx |
| Base URL | https://api.openai.com/v1 |
| 模型版本 | gpt-4-turbo-preview |
| Max Tokens | 128000 |
| Temperature | 0.7 |
| 优先级 | 10 |

### 可用模型

- `gpt-4-turbo-preview`：最新GPT-4模型，128K上下文
- `gpt-4`：标准GPT-4模型
- `gpt-3.5-turbo`：快速且经济的选择

### 费用参考

| 模型 | 输入价格 | 输出价格 |
|------|---------|---------|
| GPT-4 Turbo | $0.01/1K tokens | $0.03/1K tokens |
| GPT-4 | $0.03/1K tokens | $0.06/1K tokens |
| GPT-3.5 Turbo | $0.0005/1K tokens | $0.0015/1K tokens |

## Anthropic Claude配置

### 获取API Key

1. 访问 https://console.anthropic.com/
2. 登录或注册账号
3. 在API Keys页面创建新密钥
4. 复制API Key

### 配置参数

| 参数 | 值 |
|------|-----|
| 配置名称 | Claude 3 Opus（自定义） |
| API提供商 | Anthropic |
| API Key | sk-ant-xxxxxxxxxxxxxxxx |
| Base URL | https://api.anthropic.com/v1 |
| 模型版本 | claude-3-opus-20240229 |
| Max Tokens | 200000 |
| Temperature | 0.7 |
| 优先级 | 9 |

### 可用模型

- `claude-3-opus-20240229`：最强大的Claude 3模型
- `claude-3-sonnet-20240229`：平衡性能和成本
- `claude-3-haiku-20240307`：快速响应

## 百度文心一言配置

### 获取API Key

1. 访问 https://console.bce.baidu.com/qianfan/
2. 登录百度账号
3. 创建应用获取API Key和Secret Key
4. 格式：`API Key,Secret Key`

### 配置参数

| 参数 | 值 |
|------|-----|
| 配置名称 | 文心一言4.0（自定义） |
| API提供商 | 百度文心一言 |
| API Key | your_api_key,your_secret_key |
| Base URL | https://aip.baidubce.com/rpc/2.0/ai_custom/v1 |
| 模型版本 | ernie-bot-4 |
| Max Tokens | 8192 |
| Temperature | 0.7 |
| 优先级 | 8 |

### 可用模型

- `ernie-bot-4`：文心大模型4.0
- `ernie-bot-turbo`：快速响应版本
- `ernie-bot`：文心大模型3.5

### 费用参考

| 模型 | 价格 |
|------|------|
| ERNIE-Bot-4 | ¥0.12/千tokens |
| ERNIE-Bot-Turbo | ¥0.008/千tokens |
| ERNIE-Bot | ¥0.012/千tokens |

## 阿里通义千问配置

### 获取API Key

1. 访问 https://dashscope.console.aliyun.com/
2. 登录阿里云账号
3. 开通灵积模型服务
4. 获取API Key

### 配置参数

| 参数 | 值 |
|------|-----|
| 配置名称 | 通义千问Max（自定义） |
| API提供商 | 阿里通义千问 |
| API Key | sk-xxxxxxxxxxxxxxxx |
| Base URL | https://dashscope.aliyuncs.com/api/v1 |
| 模型版本 | qwen-max |
| Max Tokens | 8192 |
| Temperature | 0.7 |
| 优先级 | 7 |

### 可用模型

- `qwen-max`：通义千问最强模型
- `qwen-plus`：平衡版本
- `qwen-turbo`：快速响应

## 智谱AI配置

### 获取API Key

1. 访问 https://open.bigmodel.cn/
2. 注册并登录
3. 在API密钥管理中创建密钥

### 配置参数

| 参数 | 值 |
|------|-----|
| 配置名称 | GLM-4（自定义） |
| API提供商 | 智谱AI |
| API Key | xxxxxxxxxxxxxxxx |
| Base URL | https://open.bigmodel.cn/api/paas/v3 |
| 模型版本 | glm-4 |
| Max Tokens | 8192 |
| Temperature | 0.7 |
| 优先级 | 6 |

## Moonshot配置

### 获取API Key

1. 访问 https://platform.moonshot.cn/
2. 注册并登录
3. 在API密钥页面创建密钥

### 配置参数

| 参数 | 值 |
|------|-----|
| 配置名称 | Moonshot（自定义） |
| API提供商 | Moonshot |
| API Key | sk-xxxxxxxxxxxxxxxx |
| Base URL | https://api.moonshot.cn/v1 |
| 模型版本 | moonshot-v1-8k |
| Max Tokens | 8192 |
| Temperature | 0.7 |
| 优先级 | 5 |

## DeepSeek配置

### 获取API Key

1. 访问 https://platform.deepseek.com/
2. 注册并登录
3. 创建API Key

### 配置参数

| 参数 | 值 |
|------|-----|
| 配置名称 | DeepSeek（自定义） |
| API提供商 | DeepSeek |
| API Key | sk-xxxxxxxxxxxxxxxx |
| Base URL | https://api.deepseek.com/v1 |
| 模型版本 | deepseek-chat |
| Max Tokens | 4096 |
| Temperature | 0.7 |
| 优先级 | 4 |

## 自定义API配置

适用于OpenAI兼容格式的第三方API。

### 配置示例（本地部署LLM）

| 参数 | 值 |
|------|-----|
| 配置名称 | 本地LLM（自定义） |
| API提供商 | 自定义 |
| API Key | 无需或自定义 |
| Base URL | http://localhost:8000/v1 |
| 模型版本 | local-model |
| Max Tokens | 4096 |
| Temperature | 0.7 |

### 自定义Headers

如需添加自定义请求头，在JSON配置中添加：

```json
{
  "customHeaders": {
    "X-Custom-Header": "value"
  }
}
```

## 并行调用策略

启用多API并行调用时的优先级规则：

1. **优先级数字越大，优先级越高**
2. 并行调用所有启用的API
3. 自动选择最快成功响应
4. 如果多个API成功，选择优先级最高的结果
5. 所有API失败时返回第一个错误

### 推荐配置策略

**高优先级（优先级 ≥ 8）**
- 响应速度快的API
- 稳定可靠的付费API

**中优先级（优先级 4-7）**
- 国内API（网络更稳定）
- 备用API

**低优先级（优先级 ≤ 3）**
- 免费试用API
- 测试用API

## 安全建议

1. **定期更换API Key**
   - 每3-6个月更换一次
   - 发现泄露立即更换

2. **设置使用限额**
   - 在各平台设置月度预算
   - 开启用量告警

3. **不要分享API Key**
   - 不要将配置文件上传到公开仓库
   - 不要分享截图或日志

4. **监控使用情况**
   - 定期检查API调用记录
   - 发现异常立即处理

## 成本优化

### 降低成本的策略

1. **使用国内API**
   - 文心一言、通义千问价格更低
   - 网络延迟更小

2. **合理设置Max Tokens**
   - 根据实际需求设置
   - 文本优化：1024-2048足够
   - 复杂处理：4096-8192

3. **选择性启用AI优化**
   - 高质量PDF不需要AI优化
   - 仅对OCR结果差的部分启用

4. **使用Temperature优化**
   - 文本处理：0.5-0.7（更确定性）
   - 创意处理：0.8-1.0（更多样性）

### 成本估算示例

处理100页PDF扫描版：

| 方案 | 预估成本 |
|------|---------|
| GPT-4 Turbo | ¥50-80 |
| Claude 3 Opus | ¥60-90 |
| 文心一言4.0 | ¥15-25 |
| 通义千问Max | ¥10-20 |
| DeepSeek | ¥5-10 |

## 故障排除

### API Key无效

**错误信息**：`Invalid API Key`

**解决方案**：
1. 检查API Key是否正确复制
2. 确认API Key是否已激活
3. 检查账户余额

### 超出配额

**错误信息**：`Rate limit exceeded`

**解决方案**：
1. 等待一段时间后重试
2. 升级API套餐
3. 使用多个API分担负载

### 网络错误

**错误信息**：`Network error` / `Timeout`

**解决方案**：
1. 检查网络连接
2. 尝试使用国内API
3. 配置代理（如需要）

### 模型不可用

**错误信息**：`Model not found`

**解决方案**：
1. 检查模型版本拼写
2. 确认账户是否有权限使用该模型
3. 查看平台公告了解模型状态

## 最佳实践

### 生产环境推荐配置

```
优先级 10: OpenAI GPT-4 Turbo（主力）
优先级 8: 文心一言4.0（国内备用）
优先级 6: 通义千问Max（成本优化）
```

### 开发测试推荐配置

```
优先级 5: GPT-3.5 Turbo（快速测试）
优先级 3: DeepSeek（低成本）
```

### 个人使用推荐配置

```
优先级 7: 通义千问Max（性价比高）
优先级 5: 文心一言Turbo（快速响应）
```
