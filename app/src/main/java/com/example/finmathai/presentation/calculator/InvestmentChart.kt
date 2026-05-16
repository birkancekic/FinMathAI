package com.example.finmathai.presentation.calculator

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.finmathai.domain.model.ChartPoint
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun InvestmentChart(points: List<ChartPoint>) {
    // AndroidView: Compose içinde eski Android View'larını kullanmamızı sağlar.
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        factory = { context ->
            // 1. Grafiği oluştur ve ilk ayarlarını yap (Renkler, çizgiler)
            LineChart(context).apply {
                description.isEnabled = false // Sağ alttaki yazıyı kaldır
                setTouchEnabled(true)
                setPinchZoom(true)

                // X Ekseni (Yıllar) Ayarları
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = android.graphics.Color.GRAY
                xAxis.setDrawGridLines(false)

                // Y Ekseni (Para miktarı) Ayarları
                axisLeft.textColor = android.graphics.Color.GRAY
                axisRight.isEnabled = false // Sağdaki gereksiz çizgiyi kaldır

                // Alt taraftaki açıklama (Legend)
                legend.textColor = android.graphics.Color.WHITE
            }
        },
        update = { chart ->
            // 2. Veri her değiştiğinde burası tetiklenir ve grafik güncellenir.
            val entries = points.map { Entry(it.year, it.value) }

            val dataSet = LineDataSet(entries, "Varlık Gelişimi (₺)").apply {
                color = android.graphics.Color.GREEN // Çizgi rengi (Yatırım Yeşili)
                setCircleColor(android.graphics.Color.GREEN)
                lineWidth = 3f // Çizgi kalınlığı
                valueTextColor = android.graphics.Color.WHITE
                setDrawValues(false) // Her noktanın üzerine rakam yazıp grafiği kirletme

                // Çizginin altını doldur (Profesyonel görünüm)
                setDrawFilled(true)
                fillColor = android.graphics.Color.GREEN
                fillAlpha = 30
            }

            chart.data = LineData(dataSet)
            chart.invalidate() // Grafiği "Zorla Yenile" komutu
        }
    )
}