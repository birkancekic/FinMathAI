package com.example.finmathai.presentation.assets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    assetId: Int,
    viewModel: AssetViewModel,
    onBackClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onAddAssetClick: () -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    val asset = assets.find { it.id == assetId }
    val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Varlık Analizi", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F0F))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAssetClick,
                containerColor = Color(0xFF2ECC71),
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Yeni Varlık Ekle",
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            asset?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(item.assetName.uppercase(), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(formatter.format(item.purchasePrice * item.amount), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(20.dp))
                            DetailedAssetChart(modifier = Modifier.fillMaxWidth().height(120.dp), color = Color(0xFF2ECC71))
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProfitInfoBox("GÜNLÜK", "+%1.2", Color(0xFF2ECC71), Modifier.weight(1f))
                                ProfitInfoBox("AYLIK", "+%8.5", Color(0xFF2ECC71), Modifier.weight(1f))
                                ProfitInfoBox("TOTAL", "+%24.8", Color(0xFF2ECC71), Modifier.weight(1f))
                            }
                        }
                    }

                    Text("Varlık Bilgileri", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    DetailRow(label = "Miktar", value = "${item.amount} Birim", icon = Icons.Default.Inventory)
                    DetailRow(label = "Birim Maliyet", value = formatter.format(item.purchasePrice), icon = Icons.Default.Payments)
                    DetailRow(label = "Lokasyon", value = item.location.toString(), icon = Icons.Default.AccountBalance)

                    val dateText = try {
                        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR"))
                        sdf.format(Date(item.purchaseDate ?: 0L))
                    } catch (e: Exception) {
                        item.purchaseDate.toString()
                    }
                    DetailRow(label = "Alım Tarihi", value = dateText, icon = Icons.Default.CalendarToday)

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onEditClick(assetId) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verileri Düzenle", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(80.dp)) // FAB'ın önünü kapatmaması için boşluk
                }
            } ?: run {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2ECC71)
                )
            }
        }
    }
}

@Composable
fun ProfitInfoBox(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun DetailedAssetChart(modifier: Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val path = Path()
        val points = listOf(0.7f, 0.75f, 0.6f, 0.65f, 0.5f, 0.55f, 0.4f, 0.35f, 0.3f, 0.2f)
        val width = size.width
        val height = size.height
        val xStep = width / (points.size - 1)

        points.forEachIndexed { index, point ->
            val x = index * xStep
            val y = point * height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path = path, color = color, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(color.copy(alpha = 0.3f), Color.Transparent)))
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFF2ECC71).copy(0.6f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color.Gray, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}