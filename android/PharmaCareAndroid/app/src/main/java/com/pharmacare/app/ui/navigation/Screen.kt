package com.pharmacare.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Inventory : Screen("inventory", "Inventory", Icons.Default.Inventory2)
    object Billing : Screen("billing", "POS Billing", Icons.Default.ShoppingCart)
    object Purchases : Screen("purchases", "Purchases", Icons.Default.LocalShipping)
    object Prescriptions : Screen("prescriptions", "Prescriptions", Icons.Default.MedicalServices)
    object Reports : Screen("reports", "Reports", Icons.Default.BarChart)

    companion object {
        val navItems = listOf(Dashboard, Inventory, Billing, Purchases, Prescriptions, Reports)
    }
}
