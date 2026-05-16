package com.example.finmathai.presentation.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finmathai.presentation.dashboard.DashboardViewModel

// 1. UI MODEL (İkon ve Renk eklendi)
data class MarketItemUI(
    val name: String,
    val symbol: String,
    val price: String,
    val change: String,
    val isPositive: Boolean,
    val icon: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    onBackClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("CANLI PİYASALAR", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0A0A0A))
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // --- 1. DÖVİZ VE EMTİALAR ---
            item {
                MarketListItem(MarketItemUI(
                    "Amerikan Doları", "USD/TRY", "${String.format("%.2f", uiState.usdRate)} ₺",
                    "${String.format("%.2f", uiState.usdChange)}%", uiState.usdChange >= 0, "＄", Color(0xFF32D74B)
                ))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            }

            item {
                MarketListItem(MarketItemUI(
                    "Gram Altın", "ALTIN/GR", "${String.format("%.2f", uiState.goldPrice)} ₺",
                    "${String.format("%.2f", uiState.goldChange)}%", uiState.goldChange >= 0, "✨", Color(0xFFFFD60A)
                ))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            }

            // --- 2. DİNAMİK HİSSELER VE KRİPTOLAR ---
            items(uiState.stocks) { stock ->
                val cleanSymbol = stock.symbol.replace(".IS", "")

                // İSİM BELİRLEME
                val companyName = when (cleanSymbol) {
                    "THYAO" -> "Türk Hava Yolları"
                    "TUPRS" -> "Tüpraş"
                    "EREGL" -> "Ereğli Demir Çelik"
                    "ASELS" -> "Aselsan"
                    "SASA" -> "Sasa Polyester"
                    "KCHOL" -> "Koç Holding"
                    "SAHOL" -> "Sabancı Holding"
                    "GARAN" -> "Garanti Bankası"
                    "ISCTR" -> "İş Bankası"
                    "AKBNK" -> "Akbank"
                    "YKBNK" -> "Yapı Kredi"
                    "FROTO" -> "Ford Otosan"
                    "SISE" -> "Şişecam"
                    "BIMAS" -> "BİM Birleşik Mağazalar"
                    "TCELL" -> "Turkcell"
                    "PGSUS" -> "Pegasus Hava Yolları"
                    "ARCLK" -> "Arçelik"
                    "AAPL" -> "Apple Inc."
                    "NVDA" -> "NVIDIA Corporation"
                    "TSLA" -> "Tesla, Inc."
                    "MSFT" -> "Microsoft"
                    "AMZN" -> "Amazon.com"
                    "GOOGL" -> "Alphabet (Google)"
                    "BTC-USD" -> "Bitcoin"
                    "ETH-USD" -> "Ethereum"
                    "SOL-USD" -> "Solana"
                    else -> "Hisse Senedi"
                }

                // İKON VE RENK BELİRLEME
                val (iconText, iconColor) = when (cleanSymbol) {
                    "THYAO", "PGSUS" -> "✈️" to Color(0xFF0A84FF)
                    "TUPRS" -> "⛽" to Color(0xFF8E8E93)
                    "EREGL", "KARDM" -> "🏗️" to Color(0xFF64D2FF)
                    "ASELS" -> "📡" to Color(0xFF5E5CE6)
                    "SASA", "ARCLK", "SISE" -> "🏭" to Color(0xFFBF5AF2)
                    "KCHOL", "SAHOL" -> "🏢" to Color(0xFFFFD60A)
                    "GARAN", "ISCTR", "AKBNK", "YKBNK" -> "🏦" to Color(0xFF30D158)
                    "BIMAS" -> "🛒" to Color(0xFFFF375F)
                    "AAPL" -> "" to Color.White
                    "NVDA" -> "💚" to Color(0xFF32D74B)
                    "TSLA" -> "⚡" to Color(0xFFFF453A)
                    "BTC-USD" -> "₿" to Color(0xFFF7931A)
                    "ETH-USD" -> "Ξ" to Color(0xFF627EEA)
                    "SOL-USD" -> "◎" to Color(0xFF14F195)
                    else -> "📈" to Color(0xFF5E5E5E)
                }

                MarketListItem(MarketItemUI(
                    companyName, stock.symbol, "${String.format("%.2f", stock.price)} ₺",
                    "${String.format("%.2f", stock.change)}%", stock.change >= 0, iconText, iconColor
                ))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun MarketListItem(item: MarketItemUI) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(item.color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, item.color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, color = item.color, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = item.symbol, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text(text = item.name, color = Color.Gray, fontSize = 12.sp)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = item.price, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = (if (item.isPositive) Color(0xFF32D74B) else Color(0xFFFF453A)).copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = (if (item.isPositive) "▲ " else "▼ ") + item.change,
                    color = if (item.isPositive) Color(0xFF32D74B) else Color(0xFFFF453A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}