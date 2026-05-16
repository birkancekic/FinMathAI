package com.example.finmathai.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface FinanceApi {
    @GET("v1/latest")
    suspend fun getLatestRates(
        @Query("apikey") apiKey: String,
        @Query("base_currency") base: String, // Burada ne yazıyorsa Repository'de o ismi kullanmalısın
        @Query("currencies") currencies: String
    ): FinanceResponse
}
