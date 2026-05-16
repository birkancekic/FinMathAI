package com.example.finmathai.presentation.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.*


@Composable
fun DashboardScreen(
    onNavigateToCalculator: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToAddAsset: () -> Unit,
    onNavigateToAssetList: () -> Unit,
    onNavigateToMarketDetails: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding() - 10.dp)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)

        ) {
            MarketTickerTape(viewModel)
            Spacer(modifier = Modifier.height(4.dp))

            // --- PORTFÖY KARTI ---
            PortfolioPerformanceCard(
                balance = uiState.totalBalance,
                profitAmount = uiState.totalProfitAmount,
                percentage = uiState.dailyChangePercent,
                // uiState'ten gelen canlı seçimi kullanıyoruz
                selectedFilter = uiState.selectedFilter,
                onFilterClick = { filter ->
                    // ViewModel'daki fonksiyonu tetikliyoruz
                    viewModel.onFilterSelected(filter)
                },
                onClick = onNavigateToAssetList
            )

            // --- İŞLEMLER (2x2 GRID) ---
            Text("İşlemler", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OrijinalMenuCard("Varlık Ekle", Icons.Default.Add, Color(0xFF32D74B), Modifier.weight(1f)) { onNavigateToAddAsset() }
                    OrijinalMenuCard("Piyasalar", Icons.Default.Public, Color(0xFF0A84FF), Modifier.weight(1f)) { onNavigateToMarketDetails() }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OrijinalMenuCard("AI Pilot", Icons.Outlined.ChatBubbleOutline, Color(0xFFBF5AF2), Modifier.weight(1f)) { onNavigateToChat() }
                    OrijinalMenuCard(
                        "Analiz",
                        Icons.Default.TrendingUp,
                        Color(0xFFFFD60A),
                        Modifier.weight(1f)
                    ) { onNavigateToCalculator() }
                }
            }

            AiNewsCard(
                uiState = uiState,
                onClick = { /* Şimdilik boş bırakıyoruz */ }
            )
            Spacer(modifier = Modifier.height(43.dp))
        }
    }
}
@Composable
fun PortfolioPerformanceCard(
    balance: Double,
    profitAmount: Double,
    percentage: Double,
    selectedFilter: DashboardFilter, // Parametre duruyor (Hata vermemesi için)
    onFilterClick: (DashboardFilter) -> Unit, // Parametre duruyor
    onClick: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    val isProfit = profitAmount >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Üstteki Row (filtreleme butonları) kaldırıldı, direkt başlığa geçildi
            Text(
                text = "TOPLAM PORTFÖY",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ana Bakiye
            Text(
                text = formatter.format(balance),
                color = Color.White,
                fontSize = 38.sp, // Kazandığımız dikey alan sayesinde boyutu biraz artırdık
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kâr/Zarar Gösterge Paneli
            Surface(
                color = if (isProfit) Color(0xFF32D74B).copy(0.12f) else Color(0xFFFF453A).copy(0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isProfit) Color(0xFF32D74B) else Color(0xFFFF453A),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${if (isProfit) "+" else ""}${formatter.format(profitAmount)} (%${String.format("%.2f", percentage)})",
                        color = if (isProfit) Color(0xFF32D74B) else Color(0xFFFF453A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
@Composable
fun OrijinalMenuCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(115.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF1C1C1E)).clickable { onClick() }.padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
@Composable
fun MarketTickerTape(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Sonsuz Kaydırma Animasyonu
    LaunchedEffect(uiState.stocks) { // Hisseler yüklendiğinde animasyonu tetikle
        if (uiState.stocks.isNotEmpty()) {
            while (true) {
                scrollState.animateScrollTo(
                    scrollState.maxValue,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 3500000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                scrollState.scrollTo(0)
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp), // Dashboard ile arasındaki boşluğu azaltmak için optimize edildi
        color = Color(0xFF0A0A0A) // Derin siyah arka plan
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState, enabled = false)
                .padding(vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Şeridin akıcı ve sonsuz görünmesi için veriyi tekrarlıyoruz
            repeat(10) {
                // 1. Sabit Döviz ve Emtia Verileri (Zaten canlı geliyor)
                TickerItem("USD/TRY", uiState.usdRate, uiState.usdChange)
                TickerItem("ALTIN/GR", uiState.goldPrice, uiState.goldChange)
                TickerItem("BTC/USD", uiState.btcPrice, uiState.btcChange, isUsd = true)

                // 2. Dinamik Hisse Senetleri (Yahoo Finance'den gelen canlı veriler)
                uiState.stocks.forEach { stock ->
                    TickerItem(
                        label = stock.symbol,   // 'symbol' yerine 'label' yazdık
                        price = stock.price,
                        change = stock.change
                    )
                }
                // Buraya istersen manuel bir ayırıcı veya boşluk ekleyebilirsin
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@Composable
fun TickerItem(label: String, price: Double, change: Double, isUsd: Boolean = false) {
    val color = if (change >= 0) Color(0xFF2ECC71) else Color(0xFFE74C3C)
    val icon = if (change >= 0) "▲" else "▼"

    Row(
        modifier = Modifier.padding(end = 45.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            color = Color.LightGray,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "${String.format("%,.2f", price)}${if (isUsd) "$" else "₺"} $icon %${String.format("%.2f", Math.abs(change))}",
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
@Composable
fun AiNewsCard(
    uiState: DashboardUiState, // ViewModel'dan gelen state'i parametre olarak alıyoruz
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp), // Biraz daha yumuşak köşeler
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Başlık Kısmı
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                    tint = Color(0xFF0A84FF)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Portföy Gözlemcisi",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold, // Daha belirgin başlık
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ViewModel'dan gelen Dinamik Analiz Metni
            Text(
                text = uiState.economySummary,
                color = Color.LightGray,
                fontSize = 14.sp,
                lineHeight = 22.sp // Okunabilirliği artırmak için satır aralığı
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Küçük Yasal Uyarı İbaresi (Silik ve İtalik)
            Text(
                text = "* Burada sunulan analizler portföy verilerinizin özetidir, yatırım tavsiyesi içermez.",
                color = Color.Gray.copy(alpha = 0.8f),
                fontSize = 11.sp, // 10.sp çok küçüktü, 11'e çektik
                fontStyle = FontStyle.Italic,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}