package com.example.finmathai.presentation.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finmathai.data.repository.AssetRepository
import com.example.finmathai.domain.model.ChartPoint
import com.example.finmathai.domain.model.InvestmentResult
import com.example.finmathai.domain.repository.AIRepository // 3. ADIM: Yeni eklenen
import com.example.finmathai.domain.use_case.CalculateCompoundInterestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val assetRepository: AssetRepository,
    private val calculateCompoundInterestUseCase: CalculateCompoundInterestUseCase,
    private val aiRepository: AIRepository // 3. ADIM: Constructor'a eklendi
) : ViewModel() {

    // --- Mevcut State'ler ---
    var result by mutableStateOf<InvestmentResult?>(null)
        private set

    var chartData by mutableStateOf<List<ChartPoint>>(emptyList())
        private set

    var exchangeRates by mutableStateOf<Map<String, Double>>(emptyMap())
        private set

    // --- 3. ADIM: YENİ AI STATE'LERİ ---
    var aiAnalysis by mutableStateOf<String?>(null)
        private set

    var isAiLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchCurrentRates()
    }
    var goldPrice by mutableStateOf<Double?>(null)
        private set


    private fun fetchCurrentRates() {
        viewModelScope.launch {
            val usdRate = assetRepository.getLatestUsdTryRate()
            // usdRate'i burada kullan...
        }

    }

    // --- Mevcut Hesaplama Fonksiyonu ---
    fun onCalculateClicked(principal: String, rate: String, years: String, inflation: String) {
        val p = principal.toDoubleOrNull() ?: 0.0
        val r = (rate.toDoubleOrNull() ?: 0.0) / 100
        val y = years.toIntOrNull() ?: 0
        val i = (inflation.toDoubleOrNull() ?: 0.0) / 100

        result = calculateCompoundInterestUseCase(p, r, y, i)

        val points = mutableListOf<ChartPoint>()
        for (yearCount in 0..y) {
            val yearlyResult = calculateCompoundInterestUseCase(p, r, yearCount, i)
            points.add(ChartPoint(yearCount.toFloat(), yearlyResult.totalAmount.toFloat()))
        }
        chartData = points

        // Önemli: Yeni hesaplama yapıldığında eski AI yorumunu temizle
        aiAnalysis = null
    }

    // --- 3. ADIM: YENİ AI ANALİZ FONKSİYONU ---
    fun getAiAnalysis(principal: String, rate: String, years: String, inflation: String) {
        viewModelScope.launch {
            isAiLoading = true
            val prompt = """
                Bir finans uzmanı ve matematikçi gibi davran. Kullanıcının şu verileri var:
                Anapara: $principal TL, Beklenen Yıllık Getiri: %$rate, Vade: $years yıl, Beklenen Enflasyon: %$inflation.
                
                Lütfen şunları yap:
                1. Bu yatırımın reel getirisini (enflasyon sonrası) analiz et.
                2. Riskleri ve fırsatları ODTÜ'lü bir mühendis titizliğiyle yorumla.
                3. Yatırım tavsiyesi vermeden stratejik bir bakış açısı sun.
                4. Kısa, öz ve profesyonel bir dil kullan.
            """.trimIndent()

            aiRepository.getInvestmentAnalysis(prompt)
                .onSuccess { analysis ->
                    aiAnalysis = analysis
                    errorMessage = null
                }
                .onFailure {
                    errorMessage = "Gemini AI şu an yanıt veremiyor."
                }
            isAiLoading = false
        }
    }
}