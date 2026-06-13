package com.pdf2ebook.network

import com.pdf2ebook.model.APIConfig
import kotlinx.serialization.Serializable

/**
 * 通用API请求体（适配多种大模型API）
 */
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int? = null,
    val temperature: Double? = null,
    val stream: Boolean = false,
    val functions: List<Function>? = null
)

@Serializable
data class Message(
    val role: String,
    val content: String,
    val name: String? = null
)

@Serializable
data class Function(
    val name: String,
    val description: String,
    val parameters: Map<String, String>
)

/**
 * API响应体
 */
@Serializable
data class ChatResponse(
    val id: String? = null,
    val object_type: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null,
    val error: APIError? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String? = null
)

@Serializable
data class Usage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null
)

@Serializable
data class APIError(
    val message: String,
    val type: String? = null,
    val code: String? = null
)

/**
 * 文心一言特殊请求格式
 */
@Serializable
data class BaiduChatRequest(
    val messages: List<Message>,
    val temperature: Double? = null,
    val top_p: Double? = null,
    val penalty_score: Double? = null,
    val stream: Boolean = false,
    val user_id: String? = null
)

/**
 * 通义千问特殊请求格式
 */
@Serializable
data class QwenChatRequest(
    val model: String,
    val input: QwenInput,
    val parameters: QwenParameters? = null
)

@Serializable
data class QwenInput(
    val messages: List<Message>
)

@Serializable
data class QwenParameters(
    val result_format: String = "message",
    val max_tokens: Int? = null,
    val temperature: Double? = null,
    val top_p: Double? = null
)
