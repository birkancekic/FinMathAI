package com.example.finmathai.data.remote

import com.google.gson.annotations.SerializedName


data class CurrencyResponse(
    @SerializedName("data")
    val data: Map<String, Double>
)