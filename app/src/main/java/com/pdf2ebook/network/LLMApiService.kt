package com.pdf2ebook.network

import com.pdf2ebook.model.APIProvider
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * 通用LLM API服务接口
 */
interface LLMApiService {
    // OpenAI兼容格式（OpenAI, Anthropic, Moonshot, DeepSeek等）
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Body request: ChatRequest,
        @Header("Authorization") authorization: String? = null
    ): ChatResponse

    // 文心一言特殊接口
    @POST("wenxinworkshop/chat/{model}")
    suspend fun baiduChat(
        @Path("model") model: String,
        @Query("access_token") accessToken: String,
        @Body request: BaiduChatRequest
    ): ChatResponse

    // 通义千问特殊接口
    @POST("services/aigc/text-generation/generation")
    suspend fun qwenChat(
        @Header("Authorization") authorization: String,
        @Body request: QwenChatRequest
    ): ChatResponse
}

/**
 * API客户端工厂
 */
object APIClientFactory {

    /**
     * 创建API客户端
     */
    fun createClient(config: APIConfig): LLMApiService {
        val okHttpClient = createOkHttpClient(config)

        val retrofit = Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(LLMApiService::class.java)
    }

    private fun createOkHttpClient(config: APIConfig): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor(config))
            .addInterceptor(createLoggingInterceptor())
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun createAuthInterceptor(config: APIConfig): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder().apply {
                when (config.provider) {
                    APIProvider.OPENAI, APIProvider.MOONSHOT, APIProvider.DEEPSEEK -> {
                        addHeader("Authorization", "Bearer ${config.apiKey}")
                    }
                    APIProvider.ANTHROPIC -> {
                        addHeader("x-api-key", config.apiKey)
                        addHeader("anthropic-version", "2023-06-01")
                    }
                    APIProvider.ALIBABA -> {
                        addHeader("Authorization", "Bearer ${config.apiKey}")
                    }
                    else -> {
                        addHeader("Authorization", "Bearer ${config.apiKey}")
                    }
                }
                // 添加自定义headers
                config.customHeaders.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }.build()
            chain.proceed(request)
        }
    }

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
}
