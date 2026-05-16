package com.example.finmathai.data.repository

import com.example.finmathai.domain.repository.AIRepository
import com.example.finmathai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject

class AIRepositoryImpl @Inject constructor() : AIRepository {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override suspend fun getInvestmentAnalysis(prompt: String): Result<String> {
        return try {
            val response = generativeModel.generateContent(prompt)
            Result.success(response.text ?: "Analiz boş döndü.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}