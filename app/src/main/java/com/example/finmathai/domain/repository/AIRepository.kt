package com.example.finmathai.domain.repository

interface AIRepository {
    suspend fun getInvestmentAnalysis(prompt: String): Result<String>
}