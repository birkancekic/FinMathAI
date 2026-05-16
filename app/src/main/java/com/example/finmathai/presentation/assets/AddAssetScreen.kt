package com.example.finmathai.presentation.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.finmathai.data.local.entity.AssetEntity
import com.example.finmathai.data.models.AssetCategory
import com.example.finmathai.data.models.AssetLibrary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAssetScreen(
    viewModel: AssetViewModel,
    navController: NavController,
    assetId: Int? = null
) {
    val assets by viewModel.allAssets.collectAsState()
    val existingAsset = remember(assetId, assets) { assets.find { it.id == assetId } }

    var selectedCategory by remember(existingAsset) {
        mutableStateOf(
            AssetCategory.entries.find { it.title == existingAsset?.assetType } ?: AssetCategory.COMMODITY
        )
    }

    var selectedAsset by remember(selectedCategory, existingAsset) {
        mutableStateOf(
            AssetLibrary.assets.find { it.symbol == existingAsset?.assetName }
                ?: AssetLibrary.assets.first { it.category == selectedCategory }
        )
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    var amount by remember(existingAsset) { mutableStateOf(existingAsset?.amount?.toString() ?: "") }
    var price by remember(existingAsset) { mutableStateOf(existingAsset?.purchasePrice?.toString() ?: "") }
    var purchaseDate by remember(existingAsset) { mutableStateOf(existingAsset?.purchaseDate ?: System.currentTimeMillis()) }

    val locationOptions = when (selectedCategory) {
        AssetCategory.COMMODITY -> listOf("Yastık Altı", "Banka (Altın)", "Fiziksel Kasa")
        AssetCategory.STOCK_TR, AssetCategory.STOCK_US -> listOf("Midas", "Ziraat", "İş Bankası", "Diğer")
        AssetCategory.CRYPTO -> listOf("Binance", "BTCTurk", "Soğuk Cüzdan")
        AssetCategory.CURRENCY -> listOf("Banka", "Fiziksel", "Kasa")
    }

    var selectedLocation by remember(selectedCategory, existingAsset) {
        mutableStateOf(existingAsset?.location ?: locationOptions[0])
    }
    LaunchedEffect(selectedAsset) {
        viewModel.updateLivePrice(selectedAsset.apiSymbol)
    }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = purchaseDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    purchaseDate = datePickerState.selectedDateMillis ?: purchaseDate
                    showDatePicker = false
                }) { Text("Tamam") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (assetId == null) "Yeni Varlık Ekle" else "Varlığı Düzenle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Kategori Seçimi
            Text("Varlık Kategorisi", color = Color.White, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssetCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            selectedAsset = AssetLibrary.assets.first { it.category == category }
                        },
                        label = { Text(category.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2ECC71),
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            // 2. Varlık Seçimi (Düzeltildi: Tetikleyici Eklendi)
            Text("Varlık Seçin", color = Color.White, fontWeight = FontWeight.Bold)

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = "${selectedAsset.name} (${selectedAsset.symbol})",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2ECC71),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(Color(0xFF1A1A1A))
                ) {
                    AssetLibrary.assets.filter { it.category == selectedCategory }.forEach { asset ->
                        DropdownMenuItem(
                            text = { Text("${asset.name} (${asset.symbol})", color = Color.White) },
                            onClick = {
                                selectedAsset = asset
                                dropdownExpanded = false
                                // 2. ADIM: Varlık seçildiğinde canlı fiyatı çekmeye başla
                                viewModel.updateLivePrice(asset.apiSymbol)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // 3. Miktar ve Fiyat (Düzeltildi: Şimşek ve Dinamik Yazı Eklendi)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Miktar") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2ECC71))
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Birim Fiyat") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = {
                                // 3. ADIM: Şimşek butonu canlı fiyatı kutuya yazar
                                IconButton(onClick = {
                                    if (viewModel.currentLivePrice > 0.0) {
                                        price = viewModel.currentLivePrice.toString()
                                    }
                                }) {
                                    Icon(Icons.Default.Bolt, contentDescription = "Canlı Fiyatı Kullan", tint = Color(0xFF2ECC71))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2ECC71))
                        )

                        // Dinamik Bilgi Yazısı
                        Text(
                            text = if (viewModel.currentLivePrice > 0.0)
                                "Güncel Piyasa: ${"%.2f".format(viewModel.currentLivePrice)} ₺"
                            else "Fiyat bekleniyor...",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }

            // 4. Tarih ve Lokasyon
            Text("Detaylar", color = Color.White, fontWeight = FontWeight.Bold)
            OutlinedCard(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Alış Tarihi:")
                    Text(dateFormatter.format(Date(purchaseDate)), color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold)
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                locationOptions.forEach { loc ->
                    FilterChip(
                        selected = selectedLocation == loc,
                        onClick = { selectedLocation = loc },
                        label = { Text(loc) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. Kaydet Butonu
            Button(
                onClick = {
                    if (amount.isNotBlank() && price.isNotBlank()) {
                        val asset = AssetEntity(
                            id = assetId ?: 0,
                            assetName = selectedAsset.symbol,
                            assetType = selectedCategory.title,
                            apiSymbol = selectedAsset.apiSymbol,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            purchasePrice = price.toDoubleOrNull() ?: 0.0,
                            location = selectedLocation,
                            purchaseDate = purchaseDate
                        )
                        if (assetId == null) viewModel.insertAsset(asset) else viewModel.updateAsset(asset)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
            ) {
                Text(if (assetId == null) "PORTFÖYE EKLE" else "GÜNCELLE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}