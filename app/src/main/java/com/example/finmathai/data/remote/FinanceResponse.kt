package com.example.finmathai.data.remote

import com.google.gson.annotations.SerializedName

data class FinanceResponse(
    @SerializedName("data")
    val data: Map<String, Double>
)