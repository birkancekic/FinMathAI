package com.example.finmathai.data.remote

data class YahooResponse(
    val chart: YahooChart
)

data class YahooChart(
    val result: List<YahooResult>
)

data class YahooResult(
    val meta: YahooMeta
)

data class YahooMeta(
    val regularMarketPrice: Double,
    val regularMarketChangePercent: Double,
    val currency: String,
    val symbol: String
)