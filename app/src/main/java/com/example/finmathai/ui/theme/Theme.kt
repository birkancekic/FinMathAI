package com.example.finmathai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Finans uygulamasına yakışır "Dark" renk paleti
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00C853),   // Yatırım yeşili
    secondary = Color(0xFF2962FF), // Profesyonel mavi
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun FinMathAITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}