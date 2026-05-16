package com.example.finmathai.presentation.assets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finmathai.data.local.entity.AssetEntity
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetListScreen(
    viewModel: AssetViewModel,
    onBackClick: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToAddAsset: () -> Unit
) {
    // Varlıkları toplu olarak ViewModel'dan dinliyoruz
    val assets by viewModel.allAssets.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Varlık Detaylarım", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F0F))
            )
        },
        // --- BU KISMI EKLE ---
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddAsset, // Yeni eklediğimiz parametreye gidiyor
                containerColor = Color(0xFF2ECC71),
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Varlık Ekle", modifier = Modifier.size(30.dp))
            }
        },
        // ---------------------
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        if (assets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Henüz bir varlık eklemediniz.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                items(
                    items = assets,
                    key = { it.id } // Performans için her elemana benzersiz anahtar veriyoruz
                ) { asset ->
                    // Fiyat bilgisini saklamak için remember kullanıyoruz
                    var livePrice by remember(asset.id) { mutableStateOf(asset.purchasePrice) }

                    // Liste ilk açıldığında veya ID değiştiğinde fiyatı bir kez çekiyoruz
                    LaunchedEffect(asset.id) {
                        try {
                            livePrice = viewModel.getLivePrice(asset)
                        } catch (e: Exception) {
                            // Hata durumunda alış fiyatı kalır, uygulama çökmez
                        }
                    }

                    AssetItemCard(
                        asset = asset,
                        livePrice = livePrice,
                        onDetailClick = { onNavigateToDetail(asset.id) },
                        onEditClick = { onNavigateToEdit(asset.id) },
                        onDelete = { viewModel.deleteAsset(asset) }
                    )
                }
            }
        }
    }
}

@Composable
fun AssetItemCard(
    asset: AssetEntity,
    livePrice: Double,
    onDetailClick: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit
) {
// AssetItemCard içinde
    val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    // --- HESAPLAMA MANTIĞI ---
    val totalValue = livePrice * asset.amount
    val purchaseValue = asset.purchasePrice * asset.amount
    val profitAmount = totalValue - purchaseValue
    val profitPercent = if (purchaseValue != 0.0) (profitAmount / purchaseValue) * 100 else 0.0
    val isProfit = profitAmount >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SOL KISIM: İsim ve Miktar
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = asset.assetName.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    maxLines = 1
                )
                Text(
                    text = "${asset.amount} Birim",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Text(
                    text = asset.location ?: "Bilinmiyor",
                    color = Color(0xFF2ECC71).copy(alpha = 0.8f), // Lokasyon daha belirgin
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // ORTA KISIM: Performans (△ % / ±₺)
            Column(
                modifier = Modifier.weight(1.1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isProfit) "△ %${String.format("%.2f", profitPercent)}"
                    else "▽ %${String.format("%.2f", Math.abs(profitPercent))}",
                    color = if (isProfit) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (isProfit) "+${formatter.format(profitAmount)}"
                    else formatter.format(profitAmount),
                    color = if (isProfit) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                    fontSize = 11.sp
                )
            }

            // SAĞ KISIM: Güncel Değer ve Aksiyonlar
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatter.format(totalValue),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, "Düzenle", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, "Sil", tint = Color.Red.copy(0.6f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}