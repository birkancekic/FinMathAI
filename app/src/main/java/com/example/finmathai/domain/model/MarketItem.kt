package com.example.finmathai.domain.model

data class MarketItem(
    val name: String,
    val symbol: String,
    val price: String,
    val change: String,
    val isPositive: Boolean
)