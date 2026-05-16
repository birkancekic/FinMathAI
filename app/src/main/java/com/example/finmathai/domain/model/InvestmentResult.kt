package com.example.finmathai.domain.model

import java.math.BigDecimal

data class InvestmentResult(
    val totalAmount: BigDecimal,
    val totalInterestEarned: BigDecimal,
    val inflationAdjustedValue: BigDecimal
)