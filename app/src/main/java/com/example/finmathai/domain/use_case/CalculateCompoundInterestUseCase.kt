package com.example.finmathai.domain.use_case

import com.example.finmathai.domain.model.InvestmentResult
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject // KRİTİK İMPORT
import kotlin.math.pow

// 1. ADIM: @Inject constructor() ekleyerek Hilt'e bu sınıfın "tarifini" verdik.
class CalculateCompoundInterestUseCase @Inject constructor() {

    operator fun invoke(
        principal: Double,
        annualRate: Double,
        years: Int,
        annualInflation: Double = 0.0,
        compoundingPeriods: Int = 12
    ): InvestmentResult {

        // Bileşik faiz formülü: A = P(1 + r/n)^(nt)
        val ratePerPeriod = annualRate / compoundingPeriods
        val totalPeriods = compoundingPeriods * years
        val finalAmount = principal * (1 + ratePerPeriod).pow(totalPeriods.toDouble())

        // Enflasyon düzeltmesi formülü: Real = Nominal / (1 + i)^t
        val inflationAdjusted = finalAmount / (1 + annualInflation).pow(years.toDouble())

        // 2. ADIM: Matematiksel hassasiyet için BigDecimal dönüşümleri
        // String üzerinden dönüştürmek "Double" hassasiyet hatalarını (0.000000001 gibi) engeller.
        val totalAmountBD = BigDecimal(finalAmount.toString())
            .setScale(2, RoundingMode.HALF_UP)

        val interestEarnedBD = BigDecimal((finalAmount - principal).toString())
            .setScale(2, RoundingMode.HALF_UP)

        val inflationAdjustedBD = BigDecimal(inflationAdjusted.toString())
            .setScale(2, RoundingMode.HALF_UP)

        return InvestmentResult(
            totalAmount = totalAmountBD,
            totalInterestEarned = interestEarnedBD,
            inflationAdjustedValue = inflationAdjustedBD
        )
    }
}