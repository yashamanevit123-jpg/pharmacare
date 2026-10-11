package com.pharmacare.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmacare.app.ui.components.*
import com.pharmacare.app.ui.theme.*
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        FullScreenLoading(modifier = modifier)
        return
    }

    if (state.errorMessage != null && state.data == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            ErrorBanner(
                message = state.errorMessage!!,
                onRetry = { viewModel.loadDashboard() }
            )
        }
        return
    }

    val data = state.data ?: return
    val metrics = data.metrics

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Overview & Live Telemetry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Pine
                    )
                    Text(
                        text = "Real-time metrics calculated by MySQL functions & views",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = { viewModel.loadDashboard() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Pine
                    )
                }
            }
        }

        // Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Sellable Stock",
                        value = "${metrics.totalSellableStock} units",
                        subtext = "${metrics.totalMedicines} distinct medicines",
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Inventory Value",
                        value = String.format(Locale.getDefault(), "₹%.2f", metrics.totalInventoryValue),
                        subtext = "fn_inventory_value()",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = StatusOk,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Today's Revenue",
                        value = String.format(Locale.getDefault(), "₹%.2f", metrics.todaySalesRevenue),
                        subtext = "${metrics.todaySalesCount} sales invoices",
                        icon = Icons.Default.TrendingUp,
                        iconTint = StatusOk,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Stock & Expiry Alerts",
                        value = "${metrics.lowStockCount + metrics.expiringBatchesCount} alerts",
                        subtext = "${metrics.lowStockCount} low, ${metrics.expiringBatchesCount} near expiry",
                        icon = Icons.Default.Warning,
                        iconTint = if (metrics.lowStockCount > 0) StatusBad else StatusOk,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Low Stock Alerts (vw_low_stock)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Low Stock Watchlist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        StatusBadge(status = "${data.lowStockAlerts.size} Items")
                    }
                    Text(
                        text = "Source: MySQL view vw_low_stock (stock <= reorder_level)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (data.lowStockAlerts.isEmpty()) {
                        Text(
                            text = "All medicines are currently above reorder levels.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        data.lowStockAlerts.forEach { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alert.medicine_name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${alert.category_name} • Rack: ${alert.rack_location}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                StatusBadge(status = "${alert.batch_quantity} left")
                            }
                            HorizontalDivider(color = LineLight.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Expiring Soon (vw_expiring_batches)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Expiring Batches (Next 90 Days)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        StatusBadge(status = "${data.expiringAlerts.size} Batches")
                    }
                    Text(
                        text = "Source: MySQL view vw_expiring_batches",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (data.expiringAlerts.isEmpty()) {
                        Text(
                            text = "No active batches expiring within the next 90 days.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        data.expiringAlerts.forEach { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alert.medicine_name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Batch: ${alert.batch_number} • Expires: ${alert.expiry_date}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                StatusBadge(status = alert.expiry_status)
                            }
                            HorizontalDivider(color = LineLight.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}
