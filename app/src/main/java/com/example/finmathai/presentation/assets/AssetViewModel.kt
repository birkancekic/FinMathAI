package com.example.finmathai.presentation.assets

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.finmathai.data.local.entity.AssetEntity
import com.example.finmathai.data.repository.AssetRepository
import com.example.finmathai.presentation.dashboard.stockApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetViewModel @Inject constructor(
    private val repository: AssetRepository
) : ViewModel() {

    // Tüm varlıkları dinleyen akış
    val allAssets: StateFlow<List<AssetEntity>> = repository.allAssets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Canlı fiyat çekme fonksiyonu
    suspend fun getLivePrice(asset: AssetEntity): Double {
        return repository.getLivePrice(asset)
    }

    var currentLivePrice by mutableStateOf(0.0)
        private set

    fun updateLivePrice(symbol: String) {
        viewModelScope.launch {
            currentLivePrice = 0.0 // Her yeni seçimde sıfırla ki kullanıcı beklediğini anlasın
            try {
                val response = stockApiService.getStockData(symbol)
                val result = response.chart.result.firstOrNull()

                result?.meta?.let { meta ->
                    val rawPrice = meta.regularMarketPrice

                    if (symbol == "GC=F") {
                        // --- KRİTİK NOKTA ---
                        // Dolar kurunu repository'den çekiyoruz.
                        // Eğer repository'deki fonksiyon Flow döndürüyorsa .first() eklemelisin.
                        // Şimdilik en güvenli yol:
                        val usdRate = try {
                            repository.getLatestUsdTryRate()
                        } catch (e: Exception) {
                            36.15 // Hata alırsak manuel bir kur atayalım ki altın hesaplanabilsin
                        }

                        val safeUsd = if (usdRate > 0) usdRate else 36.15

                        // Ons -> Gram Çevrimi: (Ons / 31.1035) * Dolar Kuru
                        currentLivePrice = (rawPrice / 31.1035) * safeUsd

                        Log.d("AssetVM", "Altın Hesaplandı: Ons=$rawPrice, Kur=$safeUsd, Sonuç=$currentLivePrice")
                    } else {
                        currentLivePrice = rawPrice
                    }
                }
            } catch (e: Exception) {
                Log.e("AssetVM", "Fiyat çekilemedi ($symbol): ${e.message}")
            }
        }
    }

    // Varlık ekleme
    fun insertAsset(asset: AssetEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAsset(asset)
        }
    }

    // Varlık güncelleme (Hatanın muhtemel sebebi burasıydı)
    fun updateAsset(asset: AssetEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAsset(asset)
        }
    }

    // Varlık silme
    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAsset(asset)
        }
    }
}