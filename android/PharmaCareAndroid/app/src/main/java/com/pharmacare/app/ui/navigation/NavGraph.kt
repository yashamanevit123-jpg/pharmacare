package com.pharmacare.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.pharmacare.app.data.repository.AuthRepository
import com.pharmacare.app.ui.components.PharmaCareTopBar
import com.pharmacare.app.ui.components.RoleSwitchDialog
import com.pharmacare.app.ui.screens.billing.BillingScreen
import com.pharmacare.app.ui.screens.billing.BillingViewModel
import com.pharmacare.app.ui.screens.dashboard.DashboardScreen
import com.pharmacare.app.ui.screens.dashboard.DashboardViewModel
import com.pharmacare.app.ui.screens.inventory.InventoryScreen
import com.pharmacare.app.ui.screens.inventory.InventoryViewModel
import com.pharmacare.app.ui.screens.login.LoginScreen
import com.pharmacare.app.ui.screens.login.LoginViewModel
import com.pharmacare.app.ui.screens.prescriptions.PrescriptionsScreen
import com.pharmacare.app.ui.screens.prescriptions.PrescriptionsViewModel
import com.pharmacare.app.ui.screens.purchases.PurchasesScreen
import com.pharmacare.app.ui.screens.purchases.PurchasesViewModel
import com.pharmacare.app.ui.screens.reports.ReportsScreen
import com.pharmacare.app.ui.screens.reports.ReportsViewModel
import com.pharmacare.app.ui.theme.Pine
import kotlinx.coroutines.launch

@Composable
fun PharmaCareNavGraph() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var showRoleSwitchDialog by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isAuthScreen = currentRoute == Screen.Login.route || currentRoute == null

    val currentScreen = Screen.navItems.firstOrNull { it.route == currentRoute } ?: Screen.Dashboard

    Scaffold(
        topBar = {
            if (!isAuthScreen) {
                PharmaCareTopBar(
                    title = currentScreen.title,
                    onLogout = {
                        AuthRepository.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onRoleSwitch = {
                        showRoleSwitchDialog = true
                    }
                )
            }
        },
        bottomBar = {
            if (!isAuthScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Pine
                ) {
                    Screen.navItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(imageVector = it, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Pine,
                                selectedTextColor = Pine,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                val loginVm: LoginViewModel = viewModel()
                LoginScreen(
                    viewModel = loginVm,
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                val dashboardVm: DashboardViewModel = viewModel()
                DashboardScreen(viewModel = dashboardVm)
            }

            composable(Screen.Inventory.route) {
                val inventoryVm: InventoryViewModel = viewModel()
                InventoryScreen(viewModel = inventoryVm)
            }

            composable(Screen.Billing.route) {
                val billingVm: BillingViewModel = viewModel()
                BillingScreen(viewModel = billingVm)
            }

            composable(Screen.Purchases.route) {
                val purchasesVm: PurchasesViewModel = viewModel()
                PurchasesScreen(viewModel = purchasesVm)
            }

            composable(Screen.Prescriptions.route) {
                val prescriptionsVm: PrescriptionsViewModel = viewModel()
                PrescriptionsScreen(viewModel = prescriptionsVm)
            }

            composable(Screen.Reports.route) {
                val reportsVm: ReportsViewModel = viewModel()
                ReportsScreen(viewModel = reportsVm)
            }
        }

        if (showRoleSwitchDialog) {
            RoleSwitchDialog(
                onDismiss = { showRoleSwitchDialog = false },
                onSelectRole = { username ->
                    scope.launch {
                        AuthRepository.login(username, "password123")
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
