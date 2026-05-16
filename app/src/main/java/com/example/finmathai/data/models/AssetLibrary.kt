package com.example.finmathai.data.models

enum class AssetCategory(val title: String) {
    COMMODITY("Emtia/Altın"), // 1. Sıra
    STOCK_TR("BIST Hisse"),   // 2. Sıra
    CRYPTO("Kripto Para"),    // 3. Sıra
    STOCK_US("ABD Hisse"),    // 4. Sıra
    CURRENCY("Döviz")         // 5. Sıra
}

data class AssetDefinition(
    val name: String,
    val symbol: String,
    val category: AssetCategory,
    val apiSymbol: String
)

object AssetLibrary {
    val categories = AssetCategory.entries

    val assets = listOf(
        // --- KRİPTO ---
        AssetDefinition("Bitcoin", "BTC", AssetCategory.CRYPTO, "BTCUSDT"),
        AssetDefinition("Ethereum", "ETH", AssetCategory.CRYPTO, "ETHUSDT"),
        AssetDefinition("Solana", "SOL", AssetCategory.CRYPTO, "SOLUSDT"),
        AssetDefinition("Cardano", "ADA", AssetCategory.CRYPTO, "ADAUSDT"),
        AssetDefinition("Ripple", "XRP", AssetCategory.CRYPTO, "XRPUSDT"),
        AssetDefinition("Avax", "AVAX", AssetCategory.CRYPTO, "AVAXUSDT"),

        // --- BIST HİSSE ---
        AssetDefinition("Türk Hava Yolları", "THYAO", AssetCategory.STOCK_TR, "THYAO.IS"),
        AssetDefinition("Aselsan", "ASELS", AssetCategory.STOCK_TR, "ASELS.IS"),
        AssetDefinition("Ereğli Demir Çelik", "EREGL", AssetCategory.STOCK_TR, "EREGL.IS"),
        AssetDefinition("Koç Holding", "KCHOL", AssetCategory.STOCK_TR, "KCHOL.IS"),
        AssetDefinition("SASA Polyester", "SASA", AssetCategory.STOCK_TR, "SASA.IS"),
        AssetDefinition("Garanti Bankası", "GARAN", AssetCategory.STOCK_TR, "GARAN.IS"),
        AssetDefinition("Tüpraş", "TUPRS", AssetCategory.STOCK_TR, "TUPRS.IS"),
        AssetDefinition("İş Bankası (C)", "ISCTR", AssetCategory.STOCK_TR, "ISCTR.IS"),

        // --- ABD HİSSE ---
        AssetDefinition("Apple", "AAPL", AssetCategory.STOCK_US, "AAPL"),
        AssetDefinition("Tesla", "TSLA", AssetCategory.STOCK_US, "TSLA"),
        AssetDefinition("Nvidia", "NVDA", AssetCategory.STOCK_US, "NVDA"),
        AssetDefinition("Microsoft", "MSFT", AssetCategory.STOCK_US, "MSFT"),
        AssetDefinition("Amazon", "AMZN", AssetCategory.STOCK_US, "AMZN"),
        AssetDefinition("Google", "GOOGL", AssetCategory.STOCK_US, "GOOGL"),

        // --- EMTİA / ALTIN ---
        AssetDefinition("Gram Altın", "GA", AssetCategory.COMMODITY, "XAU"),
        AssetDefinition("Çeyrek Altın", "CA", AssetCategory.COMMODITY, "XAU_C"),
        AssetDefinition("Gümüş", "GMS", AssetCategory.COMMODITY, "XAG"),

        // --- DÖVİZ ---
        AssetDefinition("Amerikan Doları", "USD", AssetCategory.CURRENCY, "USD"),
        AssetDefinition("Euro", "EUR", AssetCategory.CURRENCY, "EUR"),
        AssetDefinition("İngiliz Sterlini", "GBP", AssetCategory.CURRENCY, "GBP")
    )
}