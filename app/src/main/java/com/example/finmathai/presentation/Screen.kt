package com.example.finmathai.presentation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard_screen")
    object Calculator : Screen("calculator_screen")
    object Chat : Screen("chat")
}