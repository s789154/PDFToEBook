package com.pdf2ebook.network

import com.pdf2ebook.model.APIConfig
import com.pdf2ebook.model.APIProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 大模型API并行调用管理器
 * 支持多个API同时并行调用，提高处理速度和可靠性
 */
@Singleton
class LLMApiManager @Inject constructor() {

    private val apiClients = mutableMapOf<Long, LLMApiService>()

    /**
     * 并行调用多个大模型API
     * @param configs API配置列表
     * @param prompt 提示词
     * @return 返回所有成功响应中最好的一个
     */
    suspend fun callParallel(
        configs: List<APIConfig>,
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (configs.isEmpty()) {
            return@withContext Result.failure(Exception("没有可用的API配置"))
        }

        coroutineScope {
            // 并行发起所有API调用
            val deferredResults = configs.map { config ->
                async {
                    try {
                        val response = callSingleAPI(config, prompt)
                        Pair(config, Result.success(response))
                    } catch (e: Exception) {
                        Pair(config, Result.failure<String>(e))
                    }
                }
            }

            // 等待所有结果
            val results = deferredResults.awaitAll()

            // 选择最佳结果（优先级：成功 > 速度快 > 优先级高）
            val successResults = results.filter { it.second.isSuccess }
            if (successResults.isNotEmpty()) {
                // 按优先级排序返回结果
                val best = successResults.maxByOrNull { it.first.priority }
                return@coroutineScope best!!.second
            }

            // 所有API都失败，返回第一个错误
            val firstError = results.first().second.exceptionOrNull()
            Result.failure(firstError ?: Exception("所有API调用失败"))
        }
    }

    /**
     * 单个API调用
     */
    private suspend fun callSingleAPI(config: APIConfig, prompt: String): String {
        val client = apiClients.getOrPut(config.id) {
            APIClientFactory.createClient(config)
        }

        return when (config.provider) {
            APIProvider.BAIDU -> callBaiduAPI(client, config, prompt)
            APIProvider.ALIBABA -> callQwenAPI(client, config, prompt)
            else -> callOpenAICompatibleAPI(client, config, prompt)
        }
    }

    /**
     * OpenAI兼容格式API调用
     */
    private suspend fun callOpenAICompatibleAPI(
        client: LLMApiService,
        config: APIConfig,
        prompt: String
    ): String {
        val request = ChatRequest(
            model = config.modelVersion,
            messages = listOf(
                Message(role = "user", content = prompt)
            ),
            max_tokens = config.maxTokens,
            temperature = config.temperature.toDouble()
        )

        val response = client.chatCompletions(request)

        if (response.error != null) {
            throw Exception(response.error.message)
        }

        return response.choices?.firstOrNull()?.message?.content
            ?: throw Exception("API返回内容为空")
    }

    /**
     * 文心一言API调用
     */
    private suspend fun callBaiduAPI(
        client: LLMApiService,
        config: APIConfig,
        prompt: String
    ): String {
        // 文心一言需要先获取access_token
        val accessToken = getBaiduAccessToken(config.apiKey)

        val request = BaiduChatRequest(
            messages = listOf(
                Message(role = "user", content = prompt)
            ),
            temperature = config.temperature.toDouble()
        )

        val response = client.baiduChat(
            model = config.modelVersion,
            accessToken = accessToken,
            request = request
        )

        if (response.error != null) {
            throw Exception(response.error.message)
        }

        return response.choices?.firstOrNull()?.message?.content
            ?: throw Exception("API返回内容为空")
    }

    /**
     * 通义千问API调用
     */
    private suspend fun callQwenAPI(
        client: LLMApiService,
        config: APIConfig,
        prompt: String
    ): String {
        val request = QwenChatRequest(
            model = config.modelVersion,
            input = QwenInput(
                messages = listOf(
                    Message(role = "user", content = prompt)
                )
            ),
            parameters = QwenParameters(
                max_tokens = config.maxTokens,
                temperature = config.temperature.toDouble()
            )
        )

        val response = client.qwenChat(
            authorization = "Bearer ${config.apiKey}",
            request = request
        )

        if (response.error != null) {
            throw Exception(response.error.message)
        }

        return response.choices?.firstOrNull()?.message?.content
            ?: throw Exception("API返回内容为空")
    }

    /**
     * 获取百度API的access_token
     */
    private suspend fun getBaiduAccessToken(apiKey: String): String {
        // 这里需要实现百度API的access_token获取逻辑
        // API Key格式：API Key,Secret Key
        val parts = apiKey.split(",")
        if (parts.size != 2) {
            throw Exception("百度API Key格式错误，应为：API Key,Secret Key")
        }
        // 实际实现需要调用百度的token接口
        return "mock_access_token"
    }

    /**
     * 清除缓存的API客户端
     */
    fun clearCache() {
        apiClients.clear()
    }
}
