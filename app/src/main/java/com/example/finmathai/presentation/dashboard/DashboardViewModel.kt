package com.example.finmathai.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finmathai.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
// Yeni Eklenen Importlar
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

enum class DashboardFilter { DAILY, MONTHLY, TOTAL }

// --- YAHOO API ALTYAPISI ---
interface StockApiService {
    @GET("v8/finance/chart/{symbol}")
    suspend fun getStockData(@Path("symbol") symbol: String): YahooResponse
}

// Retrofit objesini dosya seviyesinde (üstte) tanımlıyoruz
val yahooRetrofit = Retrofit.Builder()
    .baseUrl("https://query1.finance.yahoo.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val stockApiService = yahooRetrofit.create(StockApiService::class.java)
// ----------------------------

// Yahoo'dan gelen verinin haritası
data class YahooResponse(val chart: ChartData)
data class ChartData(val result: List<ChartResult>)
data class ChartResult(val meta: StockMeta)
data class StockMeta(
    val regularMarketPrice: Double,    // Güncel Fiyat
    val chartPreviousClose: Double,   // Dünkü Kapanış
    val symbol: String                // Hisse Kodu
)

// Uygulama içinde bizim kullanacağımız sadeleştirilmiş model
data class StockInfo(
    val symbol: String,
    val price: Double,
    val change: Double
)

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val dailyChangePercent: Double = 0.0,
    val totalProfitAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: DashboardFilter = DashboardFilter.TOTAL,
    // Ekonomi Özeti Metni
    val economySummary: String = "Piyasalar analiz ediliyor...",
    // CANLI PİYASA VERİLERİ VE DEĞİŞİM ORANLARI
    val stocks: List<StockInfo> = emptyList(),
    val usdRate: Double = 34.50,
    val usdChange: Double = 0.15,
    val btcPrice: Double = 65000.0,
    val btcChange: Double = -1.20,
    val goldPrice: Double = 2450.0,
    val goldChange: Double = 0.85
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AssetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var calculationJob: Job? = null

    init {
        observeAssets()
        fetchLiveStocks() // Uygulama açılınca hisseleri çek
    }

    private fun observeAssets() {
        repository.allAssets.onEach { assets ->
            calculateResults(assets)
        }.launchIn(viewModelScope)
    }

    fun fetchLiveStocks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. LİSTEYİ DEVLEŞTİRDİK (İstediğini ekleyip çıkarabilirsin)
                val specialSymbols = listOf("USDTRY=X", "BTC-USD", "GC=F")
                val stockSymbols = listOf(
                    "THYAO.IS", "TUPRS.IS", "EREGL.IS", "ASELS.IS", "SASA.IS",
                    "KCHOL.IS", "SAHOL.IS", "GARAN.IS", "ISCTR.IS", "AKBNK.IS",
                    "YKBNK.IS", "FROTO.IS", "SISE.IS", "BIMAS.IS", "TCELL.IS",
                    "PGSUS.IS", "ARCLK.IS", "AAPL", "NVDA", "TSLA"
                )
                val allSymbols = specialSymbols + stockSymbols

                // 2. PARALEL VERİ ÇEKME (async kullanarak hızlandırıyoruz)
                val deferredResults = allSymbols.map { ticker ->
                    async {
                        try {
                            val response = stockApiService.getStockData(ticker)
                            val meta = response.chart.result.firstOrNull()?.meta
                            if (meta != null) ticker to meta else null
                        } catch (e: Exception) {
                            Log.e("DashboardVM", "Hata ($ticker): ${e.message}")
                            null
                        }
                    }
                }

                val results = deferredResults.awaitAll().filterNotNull()

                val loadedStocks = mutableListOf<StockInfo>()
                var liveUsd = _uiState.value.usdRate
                var liveUsdChange = _uiState.value.usdChange
                var liveBtc = _uiState.value.btcPrice
                var liveBtcChange = _uiState.value.btcChange
                var liveGold = _uiState.value.goldPrice
                var liveGoldChange = _uiState.value.goldChange

                // 3. GELEN VERİLERİ SINIFLANDIRMA
                results.forEach { (ticker, meta) ->
                    val price = meta.regularMarketPrice
                    val prevClose = meta.chartPreviousClose
                    val changePercent = if (prevClose != 0.0) ((price - prevClose) / prevClose) * 100 else 0.0

                    when (ticker) {
                        "USDTRY=X" -> {
                            liveUsd = price
                            liveUsdChange = changePercent
                        }
                        "BTC-USD" -> {
                            liveBtc = price
                            liveBtcChange = changePercent
                        }
                        "GC=F" -> {
                            // Ons -> Gram çevrimi (Dolar kuru ile çarpıyoruz)
                            liveGold = (price / 31.1035) * liveUsd
                            liveGoldChange = changePercent
                        }
                        else -> {
                            loadedStocks.add(
                                StockInfo(
                                    symbol = ticker, // Screen'de Mapper kullanacağımız için tam ismi yolluyoruz
                                    price = price,
                                    change = changePercent
                                )
                            )
                        }
                    }
                }

                // 4. TEK SEFERDE GÜNCELLEME
                _uiState.update { it.copy(
                    stocks = loadedStocks,
                    usdRate = liveUsd,
                    usdChange = liveUsdChange,
                    btcPrice = liveBtc,
                    btcChange = liveBtcChange,
                    goldPrice = liveGold,
                    goldChange = liveGoldChange,
                    isLoading = false
                ) }

            } catch (e: Exception) {
                Log.e("DashboardVM", "Genel Yahoo hatası: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFilterSelected(filter: DashboardFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedFilter = filter, isLoading = true) }
            val assets = repository.allAssets.firstOrNull() ?: emptyList()

            if (filter == DashboardFilter.TOTAL) {
                calculateResults(assets)
            } else {
                simulateFilterResults(filter)
            }
        }
    }

    private fun simulateFilterResults(filter: DashboardFilter) {
        val multiplier = if (filter == DashboardFilter.DAILY) 0.012 else 0.085
        _uiState.update { state ->
            state.copy(
                dailyChangePercent = multiplier * 100,
                totalProfitAmount = state.totalBalance * multiplier,
                isLoading = false
            )
        }
    }

    private fun calculateResults(assets: List<com.example.finmathai.data.local.entity.AssetEntity>) {
        calculationJob?.cancel()

        calculationJob = viewModelScope.launch {
            if (assets.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalBalance = 0.0,
                        economySummary = "FinMathAI'ye Hoş Geldiniz! 👋 Finansal yolculuğunuza başlamak için yukarıdaki 'Varlık Ekle' butonunu kullanarak portföyünüzü oluşturabilirsiniz. Ben burada piyasaları sizin için takip edip özetleyeceğim."
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val deferredResults = assets.map { asset ->
                    async {
                        val livePrice = repository.getLivePrice(asset)
                        val currentVal = livePrice * asset.amount
                        val purchaseVal = asset.purchasePrice * asset.amount
                        Pair(currentVal, purchaseVal)
                    }
                }

                val usdRateDeferred = async { repository.getLatestUsdTryRate() }

                val results = deferredResults.awaitAll()
                val currentUsdRate = usdRateDeferred.await()

                var currentTotalValue = 0.0
                var totalPurchaseValue = 0.0

                results.forEach { (current, purchase) ->
                    currentTotalValue += current
                    totalPurchaseValue += purchase
                }

                val profitAmount = currentTotalValue - totalPurchaseValue
                val profitPercent = if (totalPurchaseValue != 0.0) {
                    (profitAmount / totalPurchaseValue) * 100
                } else 0.0

                val summary = generateSmartSummary(profitPercent, currentUsdRate)

                _uiState.update {
                    it.copy(
                        totalBalance = currentTotalValue,
                        totalProfitAmount = profitAmount,
                        dailyChangePercent = profitPercent,
                        usdRate = if (currentUsdRate > 0) currentUsdRate else it.usdRate,
                        economySummary = summary,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("DashboardVM", "Hata: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun generateSmartSummary(profitPercent: Double, usdRate: Double): String {
        return when {
            profitPercent > 2.0 ->
                "Bugün portföyün parlıyor! %${String.format("%.2f",profitPercent)} kazanç ile piyasanın oldukça önündesin. 🚀"

            profitPercent < -3.0 ->
                "Piyasalarda bugün sert bir geri çekilme hakim. 📉 Bu tür dönemler uzun vadeli planına sadık kalmak için iyi bir test olabilir. Panik yapmadan izlemeye devam."

            usdRate > 35.0 ->
                "Dolar kurundaki hareketlilik portföyündeki döviz bazlı varlıkların TL değerini yukarı çekiyor. 💵 Kur etkisini bakiyende hissedebilirsin."

            profitPercent in -0.5..0.5 ->
                "Piyasalar bugün yatay ve sakin seyrediyor. ☕ Portföyünde büyük bir değişim yok, varlık dağılımın dengeyi koruyor."

            profitPercent < -2.0 ->
                "Küresel piyasalardaki satış baskısı portföyüne yansımış durumda. Genel trend negatif yönlü seyrediyor. 📉"

            else -> "Portföyün piyasa koşullarına paralel olarak sağlıklı bir şekilde hareket ediyor. ⚖️ Genel finansal durumun stabil ilerliyor."
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            val currentAssets = repository.allAssets.firstOrNull() ?: emptyList()
            calculateResults(currentAssets)
            fetchLiveStocks() // Manuel yenilemede hisseleri de güncelle
        }
    }
}