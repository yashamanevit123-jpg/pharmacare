package com.pharmacare.app.ui.screens.reports

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmacare.app.ui.components.*
import com.pharmacare.app.ui.theme.*
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Financial Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Pine
                    )
                    Text(
                        text = "Aggregated via MySQL view vw_monthly_sales",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = { viewModel.loadReports() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Pine)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.errorMessage != null) {
                ErrorBanner(
                    message = state.errorMessage!!,
                    onRetry = { viewModel.loadReports() }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.isLoading) {
                FullScreenLoading()
            } else if (state.monthlySales.isEmpty()) {
                EmptyStateView(
                    title = "No Sales Records Found",
                    subtitle = "Monthly aggregations will appear as sales are recorded in the system."
                )
            } else {
                // Top KPI Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Cumulative Revenue",
                        value = String.format(Locale.getDefault(), "₹%.2f", state.totalRevenue),
                        subtext = "Across all months",
                        icon = Icons.Default.TrendingUp,
                        iconTint = StatusOk,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Invoices",
                        value = "${state.totalInvoices}",
                        subtext = "Processed transactions",
                        icon = Icons.Default.ReceiptLong,
                        iconTint = Pine,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Month-by-Month Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.monthlySales, key = { "${it.sales_year}-${it.month_label}" }) { item ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.month_label} ${item.sales_year}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Pine
                                    )
                                    StatusBadge(status = "${item.total_invoices} Invoices")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Gross Subtotal:", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            text = String.format(Locale.getDefault(), "₹%.2f", item.gross_subtotal),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Column {
                                        Text("Total Discounts:", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            text = String.format(Locale.getDefault(), "₹%.2f", item.total_discounts),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StatusBad
                                        )
                                    }
                                    Column {
                                        Text("Net Revenue:", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            text = String.format(Locale.getDefault(), "₹%.2f", item.net_revenue),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusOk
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = LineLight.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = String.format(Locale.getDefault(), "Average Invoice Value: ₹%.2f", item.average_invoice_value),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
