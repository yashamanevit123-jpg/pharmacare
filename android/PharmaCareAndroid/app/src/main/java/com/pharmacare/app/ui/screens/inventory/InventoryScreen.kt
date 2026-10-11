package com.pharmacare.app.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmacare.app.data.model.MedicineItem
import com.pharmacare.app.ui.components.*
import com.pharmacare.app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
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
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search medicine, generic name, or dosage...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Pine)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategoryId == null,
                    onClick = { viewModel.onCategorySelect(null) },
                    label = { Text("All Categories") }
                )
                state.categories.forEach { cat ->
                    FilterChip(
                        selected = state.selectedCategoryId == cat.category_id,
                        onClick = {
                            viewModel.onCategorySelect(
                                if (state.selectedCategoryId == cat.category_id) null else cat.category_id
                            )
                        },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status & Count Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.medicines.size} Medicines Found",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val statusFilters = listOf("All", "In Stock", "Low Stock", "Out of Stock")
                    statusFilters.forEach { st ->
                        val isSelected = if (st == "All") state.selectedStatus == null else state.selectedStatus == st
                        AssistChip(
                            onClick = { viewModel.onStatusSelect(if (st == "All") null else st) },
                            label = { Text(st, style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) Mint else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Medicine List
            if (state.isLoading) {
                FullScreenLoading()
            } else if (state.medicines.isEmpty()) {
                EmptyStateView(
                    title = "No Medicines Found",
                    subtitle = "Try adjusting your search query or category filter."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.medicines, key = { it.medicine_id }) { med ->
                        MedicineCard(
                            medicine = med,
                            onClickBatches = { viewModel.selectMedicineForBatches(med) }
                        )
                    }
                }
            }
        }

        // Batch Details Dialog
        if (state.selectedMedicine != null) {
            val med = state.selectedMedicine!!
            AlertDialog(
                onDismissRequest = { viewModel.selectMedicineForBatches(null) },
                title = {
                    Column {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "FEFO Batches (First-Expiring First-Out)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Pine
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.isBatchesLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(16.dp)
                            )
                        } else if (state.batches.isEmpty()) {
                            Text(
                                text = "No active batches available for this medicine.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            state.batches.forEach { batch ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Batch #${batch.batch_number}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            StatusBadge(status = "${batch.quantity} units")
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Expires: ${batch.expiry_date}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = String.format(Locale.getDefault(), "₹%.2f", batch.selling_price),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Pine
                                            )
                                        }
                                        if (batch.days_remaining != null) {
                                            Text(
                                                text = "${batch.days_remaining} days remaining",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (batch.days_remaining < 60) StatusWarn else StatusOk
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.selectMedicineForBatches(null) }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun MedicineCard(
    medicine: MedicineItem,
    onClickBatches: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!medicine.generic_name.isNullOrBlank()) {
                        Text(
                            text = medicine.generic_name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                val stockStatus = when {
                    medicine.total_stock <= 0 -> "Out of Stock"
                    medicine.total_stock <= medicine.reorder_level -> "Low Stock"
                    else -> "In Stock"
                }
                StatusBadge(status = stockStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${medicine.category_name} • ${medicine.dosage_form} (${medicine.unit_pack})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (medicine.is_prescription_required) {
                    StatusBadge(status = "Rx Required")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LineLight.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Stock: ${medicine.total_stock} units (Rack: ${medicine.rack_location})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    val priceText = if (medicine.min_price != null) {
                        String.format(Locale.getDefault(), "₹%.2f - ₹%.2f", medicine.min_price, medicine.max_price ?: medicine.min_price)
                    } else "No active batch"
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Pine
                    )
                }

                OutlinedButton(
                    onClick = onClickBatches,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Batches (${medicine.batch_count})",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
