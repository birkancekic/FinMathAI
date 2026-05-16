package com.example.finmathai.data.repository

import android.util.Log
import com.example.finmathai.data.local.dao.AssetDao
import com.example.finmathai.data.local.entity.AssetEntity
import com.example.finmathai.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val apiService: ApiService
) {
    val allAssets: Flow<List<AssetEntity>> = assetDao.getAllAssets()

    suspend fun getLivePrice(asset: AssetEntity): Double {
        return try {
            when (asset.assetType) {
                "BIST Hisse" -> {
                    // Yahoo Finance'te BIST hisseleri sonuna .IS eklenerek çekilir
                    val symbol = if (asset.apiSymbol.contains(".IS")) asset.apiSymbol else "${asset.apiSymbol}.IS"
                    fetchYahooPrice(symbol)
                }
                "Kripto Para" -> {
                    val symbol = if (asset.apiSymbol.contains("-USD")) asset.apiSymbol else "${asset.apiSymbol}-USD"
                    val usdPrice = fetchYahooPrice(symbol)
                    usdPrice * getLatestUsdTryRate() // Dolar kuruna çevir
                }
                "Emtia/Altın" -> {
                    // GC=F Ons Altın fiyatıdır. Bunu Gram Altın'a çeviriyoruz.
                    val onsPrice = fetchYahooPrice("GC=F")
                    val usdTry = getLatestUsdTryRate()
                    val gramGold = (onsPrice / 31.1035) * usdTry
                    if (asset.assetName == "CA") gramGold * 1.63 else gramGold
                }
                "Döviz" -> {
                    if (asset.apiSymbol == "USD") getLatestUsdTryRate()
                    else fetchYahooPrice("${asset.apiSymbol}TRY=X")
                }
                else -> asset.purchasePrice
            }
        } catch (e: Exception) {
            Log.e("API_UPGRADE", "${asset.assetName} hatası: ${e.message}")
            asset.purchasePrice
        }
    }

    private suspend fun fetchYahooPrice(symbol: String): Double {
        return try {
            val response = apiService.getYahooPrice(symbol)
            // Yahoo JSON hiyerarşisinden en son fiyatı alıyoruz
            response.chart.result[0].meta.regularMarketPrice
        } catch (e: Exception) {
            Log.e("YAHOO_ERROR", "$symbol çekilemedi")
            0.0
        }
    }

    suspend fun getLatestUsdTryRate(): Double {
        val rate = fetchYahooPrice("USDTRY=X")
        // Eğer Yahoo hata verirse veya 0 dönerse (hafta sonu vs.) sabit kur dön
        return if (rate > 0) rate else 34.50
    }

    // Mevcut kodlarının içine, getLatestUsdTryRate fonksiyonunun altına ekle:
    suspend fun getLivePriceManual(symbol: String): Double {
        return fetchYahooPrice(symbol)
    }
    suspend fun insertAsset(asset: AssetEntity) = assetDao.insertAsset(asset)
    suspend fun updateAsset(asset: AssetEntity) = assetDao.updateAsset(asset)
    suspend fun deleteAsset(asset: AssetEntity) = assetDao.deleteAsset(asset)
}