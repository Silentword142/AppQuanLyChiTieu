package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = "application/json",
    @Json(name = "temperature") val temperature: Float? = 0.1f
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

@JsonClass(generateAdapter = true)
data class ParsedTransaction(
    @Json(name = "amount") val amount: Double,
    @Json(name = "type") val type: String, // "EXPENSE" or "INCOME"
    @Json(name = "bankName") val bankName: String, // e.g., "Vietcombank", "Techcombank", "MB Bank", etc.
    @Json(name = "note") val note: String,
    @Json(name = "category") val category: String, // "Ăn uống", "Di chuyển", "Mua sắm", "Giải trí", "Lương", "Nhà cửa", "Khác"
    @Json(name = "accountNo") val accountNo: String? = null,
    @Json(name = "isValidTransaction") val isValidTransaction: Boolean = true
)
