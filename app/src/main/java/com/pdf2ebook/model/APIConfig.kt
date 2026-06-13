package com.pdf2ebook.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 大模型API配置
 */
@Entity(tableName = "api_configs")
@Serializable
data class APIConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // GPT-4, Claude, 文心一言, 通义千问等
    val provider: APIProvider,
    val apiKey: String,
    val baseUrl: String,
    val modelVersion: String, // gpt-4-turbo, claude-3-opus等
    val isEnabled: Boolean = true,
    val priority: Int = 0, // 并行调用时的优先级
    val maxTokens: Int = 4096,
    val temperature: Float = 0.7f,
    val customHeaders: Map<String, String> = emptyMap()
)

/**
 * API提供商枚举
 */
@Serializable
enum class APIProvider(val displayName: String, val defaultBaseUrl: String) {
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com/v1"),
    BAIDU("百度文心一言", "https://aip.baidubce.com/rpc/2.0/ai_custom/v1"),
    ALIBABA("阿里通义千问", "https://dashscope.aliyuncs.com/api/v1"),
    ZHIPU("智谱AI", "https://open.bigmodel.cn/api/paas/v3"),
    MOONSHOT("Moonshot", "https://api.moonshot.cn/v1"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1"),
    CUSTOM("自定义", "")
}

/**
 * API版本配置
 */
data class APIVersion(
    val modelId: String,
    val displayName: String,
    val maxTokens: Int,
    val supportsVision: Boolean = false,
    val supportsFunctionCall: Boolean = false
)

/**
 * 预定义的模型版本
 */
object ModelVersions {
    val OPENAI_VERSIONS = listOf(
        APIVersion("gpt-4-turbo-preview", "GPT-4 Turbo", 128000, true, true),
        APIVersion("gpt-4", "GPT-4", 8192, false, true),
        APIVersion("gpt-3.5-turbo", "GPT-3.5 Turbo", 16385, true, true)
    )

    val ANTHROPIC_VERSIONS = listOf(
        APIVersion("claude-3-opus-20240229", "Claude 3 Opus", 200000, true, true),
        APIVersion("claude-3-sonnet-20240229", "Claude 3 Sonnet", 200000, true, true),
        APIVersion("claude-3-haiku-20240307", "Claude 3 Haiku", 200000, true, true)
    )

    val BAIDU_VERSIONS = listOf(
        APIVersion("ernie-bot-4", "文心大模型 4.0", 8192, false, true),
        APIVersion("ernie-bot-turbo", "文心大模型 Turbo", 4096, false, true),
        APIVersion("ernie-bot", "文心大模型 3.5", 4096, false, false)
    )

    val ALIBABA_VERSIONS = listOf(
        APIVersion("qwen-max", "通义千问 Max", 8192, false, true),
        APIVersion("qwen-plus", "通义千问 Plus", 32768, false, true),
        APIVersion("qwen-turbo", "通义千问 Turbo", 8192, false, false)
    )
}
