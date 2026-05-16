package com.example.finmathai.presentation.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finmathai.domain.model.InvestmentResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    var principalInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("") }
    var yearsInput by remember { mutableStateOf("") }
    var inflationInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Getiri Analizi",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- GİRİŞ FORMU ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Yatırım Parametreleri", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    ModernCalculatorTextField(value = principalInput, onValueChange = { principalInput = it }, label = "Anapara (₺)", icon = Icons.Default.Payments)
                    ModernCalculatorTextField(value = rateInput, onValueChange = { rateInput = it }, label = "Yıllık Getiri Beklentisi (%)", icon = Icons.Default.TrendingUp, suffix = "%")

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                        ModernCalculatorTextField(value = yearsInput, onValueChange = { yearsInput = it }, label = "Vade (Yıl)", icon = Icons.Default.Event, modifier = Modifier.weight(1f))
                        ModernCalculatorTextField(value = inflationInput, onValueChange = { inflationInput = it }, label = "Enflasyon (%)", icon = Icons.Default.ShowChart, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.onCalculateClicked(principalInput, rateInput, yearsInput, inflationInput) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
                    ) {
                        Text("HESAPLA VE ANALİZ ET", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // --- SONUÇLAR VE ANALİZ ---
            AnimatedVisibility(visible = viewModel.result != null) {
                viewModel.result?.let { res ->
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tahmini Gelecek Değer", color = Color.Gray, fontSize = 12.sp)
                                Text(
                                    text = "${res.totalAmount} ₺",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                    ResultItem("Net Kazanç", "${res.totalInterestEarned} ₺", Color(0xFF2ECC71))
                                    ResultItem("Alım Gücü", "${res.inflationAdjustedValue} ₺", Color(0xFF3498DB))
                                }
                            }
                        }

                        // Akıllı Piyasa Karşılaştırması (principalInput parametresiyle hatasız)
                        InvestmentInsightSection(res, viewModel, principalInput)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
@Composable
fun InvestmentInsightSection(
    result: InvestmentResult,
    viewModel: CalculatorViewModel,
    userPrincipalInput: String
) {
    // 1. CANLI VERİLERİ ÇEKİYORUZ
    // ViewModel'daki Map'ten TRY kurunu alıyoruz
    val usdRate = viewModel.exchangeRates["TRY"] ?: 45.00

    // ViewModel'daki canlı altın fiyatını alıyoruz (Eğer null ise 2550.0 varsayılan kalır)
    val goldRate = viewModel.goldPrice ?: 6500.00

    // 2. GÜVENLİ SAYI DÖNÜŞÜMÜ
    fun Any?.safeToDouble(): Double {
        if (this == null) return 0.0
        // Sadece rakamları ve ondalık ayracı tutar, format bozukluklarını temizler
        val cleanString = this.toString()
            .replace("₺", "")
            .replace(".", "") // Binlik ayracı siliyoruz
            .replace(",", ".") // Ondalık ayracı noktaya çeviriyoruz
            .trim()
        return cleanString.toDoubleOrNull() ?: 0.0
    }

    val totalAmountDouble = result.totalAmount.safeToDouble()
    val principalDouble = userPrincipalInput.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    val adjustedDouble = result.inflationAdjustedValue.safeToDouble()

    // 3. PİYASA KIYASLAMA HESABI
    val futureUsdValue = totalAmountDouble / usdRate
    val futureGoldValue = totalAmountDouble / goldRate

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Vade Sonu Reel Değer Analizi",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 4.dp)
        )

        // 4. KARŞILAŞTIRMA KARTLARI (Canlı Veriyle Çalışır)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InsightCard(
                label = "Altın Karşılığı",
                value = "${String.format("%.2f", futureGoldValue)} Gram",
                icon = Icons.Default.WorkspacePremium,
                color = Color(0xFFFFD700),
                modifier = Modifier.weight(1f)
            )
            InsightCard(
                label = "Dolar Karşılığı",
                value = "${String.format("%,.0f", futureUsdValue)} $",
                icon = Icons.Default.AttachMoney,
                color = Color(0xFF2ECC71),
                modifier = Modifier.weight(1f)
            )
        }

        // 5. AKILLI DURUM ANALİZİ
        val (insightText, insightIcon, iconColor) = when {
            adjustedDouble > principalDouble -> Triple(
                "Tebrikler! Yatırımınız enflasyon canavarını yeniyor. Reel alım gücünüzü artırıyorsunuz. 🛡️",
                Icons.Default.Security,
                Color(0xFF2ECC71)
            )
            adjustedDouble < principalDouble -> Triple(
                "Kazancınız enflasyonun altında kalıyor. Paran miktar olarak artsa da alım gücünüz azalacak. ⚠️",
                Icons.Default.Warning,
                Color(0xFFFF453A)
            )
            else -> Triple(
                "Yatırımınız tam olarak enflasyon sınırında. Mevcut alım gücünüzü koruyorsunuz. ⚖️",
                Icons.Default.Balance,
                Color(0xFFFFD700)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(insightIcon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Text(
                    text = insightText,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
@Composable
fun InsightCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun ResultItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 15.sp)
    }
}

@Composable
fun ModernCalculatorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    suffix: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray, fontSize = 13.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF2ECC71), modifier = Modifier.size(20.dp)) },
        suffix = suffix?.let { { Text(it, color = Color.Gray) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2ECC71),
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.03f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}