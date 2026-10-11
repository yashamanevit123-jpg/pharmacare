package com.pharmacare.app.ui.screens.purchases

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmacare.app.ui.components.*
import com.pharmacare.app.ui.theme.*
import java.util.Locale

@Composable
fun PurchasesScreen(
    viewModel: PurchasesViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: History, 1: New PO

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
                        text = "Purchases & Intake",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Pine
                    )
                    Text(
                        text = "Batch reception via sp_record_purchase",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = { viewModel.loadData() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Pine)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Pine
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("PO History (${state.purchases.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Receive New Stock") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.errorMessage != null) {
                ErrorBanner(message = state.errorMessage!!)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.successMessage != null) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusOkBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusOk)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.successMessage!!,
                            color = StatusOk,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.dismissSuccess() }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = StatusOk)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedTab == 0) {
                // Tab 0: PO History
                if (state.isLoading) {
                    FullScreenLoading()
                } else if (state.purchases.isEmpty()) {
                    EmptyStateView(
                        title = "No Purchase Orders",
                        subtitle = "Recorded purchase invoices will appear here."
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.purchases, key = { it.purchase_id }) { po ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
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
                                            text = po.invoice_number,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Pine
                                        )
                                        StatusBadge(status = po.payment_status)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Supplier: ${po.supplier_name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Date: ${po.purchase_date.take(10)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = String.format(Locale.getDefault(), "₹%.2f", po.total_amount),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1: New Purchase Order Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Supplier & Invoice Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Supplier Selector
                            Text("Supplier:", style = MaterialTheme.typography.labelSmall)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                state.suppliers.take(3).forEach { sup ->
                                    val isSelected = state.selectedSupplier?.supplier_id == sup.supplier_id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectSupplier(sup) },
                                        label = { Text(sup.name, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = state.invoiceNumber,
                                onValueChange = { viewModel.onInvoiceNumberChange(it) },
                                label = { Text("Supplier Invoice #") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Batch Intake Specification",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Select Medicine:", style = MaterialTheme.typography.labelSmall)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                state.medicines.take(3).forEach { med ->
                                    val isSelected = state.selectedMedicine?.medicine_id == med.medicine_id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectMedicine(med) },
                                        label = { Text(med.name, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = state.batchNumber,
                                onValueChange = { viewModel.onBatchNumberChange(it) },
                                label = { Text("New Batch Number (e.g. BATCH-2026-X)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = state.expiryDate,
                                onValueChange = { viewModel.onExpiryDateChange(it) },
                                label = { Text("Expiry Date (YYYY-MM-DD)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = state.quantity.toString(),
                                    onValueChange = { it.toIntOrNull()?.let { q -> viewModel.onQuantityChange(q) } },
                                    label = { Text("Quantity") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.unitCost.toString(),
                                    onValueChange = { it.toDoubleOrNull()?.let { c -> viewModel.onUnitCostChange(c) } },
                                    label = { Text("Unit Cost (₹)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.sellingPrice.toString(),
                                    onValueChange = { it.toDoubleOrNull()?.let { sp -> viewModel.onSellingPriceChange(sp) } },
                                    label = { Text("Selling (₹)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.submitPurchase() },
                                enabled = !state.isSubmitting,
                                colors = ButtonDefaults.buttonColors(containerColor = Pine),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (state.isSubmitting) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text("Record Stock Intake (sp_record_purchase)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
