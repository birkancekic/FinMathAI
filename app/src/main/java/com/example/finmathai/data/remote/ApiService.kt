package com.example.finmathai.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Binance API'den veri çeken modelimiz
data class PriceResponse(
    val symbol: String,
    val price: String
)

interface ApiService {
    // Yahoo Finance veya benzeri bir sağlayıcıdan BIST ve Kripto çekmek için
    @GET("v8/finance/chart/{symbol}")
    suspend fun getYahooPrice(
        @Path("symbol") symbol: String, // Örn: THYAO.IS, BTC-USD
        @Query("interval") interval: String = "1m",
        @Query("range") range: String = "1d"
    ): YahooResponse
}