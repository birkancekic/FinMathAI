package com.example.finmathai

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finmathai.presentation.Screen
import com.example.finmathai.presentation.assets.AddAssetScreen
import com.example.finmathai.presentation.assets.AssetDetailScreen
import com.example.finmathai.presentation.assets.AssetListScreen
import com.example.finmathai.presentation.assets.AssetViewModel
import com.example.finmathai.presentation.calculator.CalculatorScreen
import com.example.finmathai.presentation.chat.ChatScreen
import com.example.finmathai.presentation.dashboard.DashboardScreen
import com.example.finmathai.ui.theme.FinMathAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            FinMathAITheme {
                val view = LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
                        // Arka planı tamamen şeffaf yapıyoruz
                        window.statusBarColor = Color.Transparent.toArgb()
                        // Navigasyon barını (alttaki çizgi) şeffaf yapıyoruz
                        window.navigationBarColor = Color.Transparent.toArgb()

                        // İkonların (saat, pil) rengini ayarla (Koyu tema için açık renk ikonlar)
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinPilotApp()
                }
            }
        }
    }
}

@Composable
fun FinPilotApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable("market") {
            // BURASI ÇOK ÖNEMLİ: MarketScreen'i tam adresiyle çağıralım
            com.example.finmathai.presentation.market.MarketScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        // --- 1. ANA PANEL ---
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = hiltViewModel(), // BU SATIRI EKLE
                onNavigateToCalculator = { navController.navigate(Screen.Calculator.route) },
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onNavigateToAddAsset = { navController.navigate("add_asset") },
                onNavigateToAssetList = { navController.navigate("asset_list") },
                onNavigateToMarketDetails = { navController.navigate("market") }
            )
        }

        // --- 2. HESAPLAYICI ---
        composable(Screen.Calculator.route) {
            CalculatorScreen(
                onBackClick = {
                    navController.popBackStack() // Bu satır geri tuşuna basınca bir önceki ekrana döner
                }
            )
        }

        // --- 3. AI CHAT ---
        composable(Screen.Chat.route) {
            ChatScreen(onBackClick = { navController.popBackStack() })
        }

        // --- 4. VARLIK EKLEME VE DÜZENLEME ---
        composable(
            route = "add_asset?assetId={assetId}",
            arguments = listOf(
                navArgument("assetId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getInt("assetId") ?: -1
            AddAssetScreen(
                viewModel = hiltViewModel(),
                navController = navController,
                assetId = if (assetId == -1) null else assetId
            )
        }

        // --- 5. VARLIK LİSTESİ ---
        composable("asset_list") {
            AssetListScreen(
                viewModel = hiltViewModel(),
                onBackClick = { navController.popBackStack() },
                onNavigateToDetail = { id ->
                    navController.navigate("asset_detail/$id")
                },
                onNavigateToEdit = { id ->
                    navController.navigate("add_asset?assetId=$id")
                },
                // 👇 BU SATIRI EKLE
                onNavigateToAddAsset = {
                    navController.navigate("add_asset")
                }
            )
        }

        // --- 6. VARLIK DETAY (RÖNTGEN) EKRANI ---
        composable(
            route = "asset_detail/{assetId}",
            arguments = listOf(
                navArgument("assetId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getInt("assetId") ?: -1
            val assetViewModel: AssetViewModel = hiltViewModel()
            AssetDetailScreen(
                assetId = assetId,
                viewModel = assetViewModel,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate("edit_asset/$id")
                },
                onAddAssetClick = {
                    navController.navigate("add_asset")
                }
            )
        }
    }
}}