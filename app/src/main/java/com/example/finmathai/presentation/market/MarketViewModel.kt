package com.example.finmathai.presentation.market

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finmathai.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. EKSİK OLAN MODELİ BURAYA EKLEDİK
data class MarketItem(
    val name: String,
    val symbol: String,
    val price: String,
    val change: String,
    val isPositive: Boolean
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: AssetRepository
) : ViewModel() {

    var marketList by mutableStateOf<List<MarketItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        fetchLiveMarketData()
    }

    fun fetchLiveMarketData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val usdTry = repository.getLatestUsdTryRate()

                // Yahoo Finance üzerinden çekilecek genişletilmiş liste
                val tickers = listOf(
                    // --- DÖVİZ & EMTİA (Temel Göstergeler) ---
                    Triple("Amerikan Doları", "USD/TRY", "USDTRY=X"),
                    Triple("Euro", "EUR/TRY", "EURTRY=X"),
                    Triple("İngiliz Sterlini", "GBP/TRY", "GBPTRY=X"),
                    Triple("Gram Altın", "ALTIN/GR", "GC=F"),
                    Triple("Gümüş Gram", "GÜMÜŞ/GR", "SI=F"),
                    Triple("Brent Petrol", "BRENT", "BZ=F"),

                    // --- BIST DEVLERİ (BIST 30'un En Çok İşlem Görenleri) ---
                    Triple("Türk Hava Yolları", "THYAO", "THYAO.IS"),
                    Triple("Tüpraş", "TUPRS", "TUPRS.IS"),
                    Triple("Aselsan", "ASELS", "ASELS.IS"),
                    Triple("Ereğli Demir Çelik", "EREGL", "EREGL.IS"),
                    Triple("Sasa Polyester", "SASA", "SASA.IS"),
                    Triple("Koç Holding", "KCHOL", "KCHOL.IS"),
                    Triple("Sabancı Holding", "SAHOL", "SAHOL.IS"),
                    Triple("Garanti Bankası", "GARAN", "GARAN.IS"),
                    Triple("İş Bankası", "ISCTR", "ISCTR.IS"),
                    Triple("Akbank", "AKBNK", "AKBNK.IS"),
                    Triple("Yapı Kredi", "YKBNK", "YKBNK.IS"),
                    Triple("Ford Otosan", "FROTO", "FROTO.IS"),
                    Triple("Türk Telekom", "TTKOM", "TTKOM.IS"),
                    Triple("Turkcell", "TCELL", "TCELL.IS"),
                    Triple("Arçelik", "ARCLK", "ARCLK.IS"),

                    // --- KRİPTO PARALAR (Popüler Altcoinler Dahil) ---
                    Triple("Bitcoin", "BTC/USD", "BTC-USD"),
                    Triple("Ethereum", "ETH/USD", "ETH-USD"),
                    Triple("Solana", "SOL/USD", "SOL-USD"),
                    Triple("Ripple", "XRP/USD", "XRP-USD"),
                    Triple("Cardano", "ADA/USD", "ADA-USD"),

                    // --- GLOBAL ENDEKSLER & HİSSELER ---
                    Triple("Nasdaq 100", "NASDAQ", "^NDX"),
                    Triple("S&P 500", "S&P 500", "^GSPC"),
                    Triple("Dow Jones", "DOW 30", "^DJI"),
                    Triple("Apple Inc.", "AAPL", "AAPL"),
                    Triple("NVIDIA Corp.", "NVDA", "NVDA"),
                    Triple("Tesla Inc.", "TSLA", "TSLA")
                )

                val deferredItems = tickers.map { (name, symbol, apiSymbol) ->
                    async {
                        // Yahoo'dan anlık fiyatı çekiyoruz
                        var price = repository.getLivePriceManual(apiSymbol) // Repository'de public bir fetch ekleyelim

                        // Altın için gram hesaplaması (Dashboard'daki formülün aynısı)
                        if (apiSymbol == "GC=F") {
                            price = (price / 31.1035) * usdTry
                        }

                        // Kripto veya Global endeksleri TL'ye çevirmek istersen:
                        if (apiSymbol.contains("-USD") || apiSymbol.startsWith("^")) {
                            price *= usdTry
                        }

                        MarketItem(
                            name = name,
                            symbol = symbol,
                            price = "${String.format("%,.2f", price)} ₺",
                            change = "%0.00", // Yahoo Response'dan 'regularMarketChangePercent' ekleyebilirsin
                            isPositive = true
                        )
                    }
                }
                marketList = deferredItems.awaitAll()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }}