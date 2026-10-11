package com.pharmacare.app.ui.screens.prescriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun PrescriptionsScreen(
    viewModel: PrescriptionsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    val filteredList = remember(state.prescriptions, state.searchQuery) {
        if (state.searchQuery.isBlank()) state.prescriptions
        else {
            val q = state.searchQuery.lowercase()
            state.prescriptions.filter {
                it.customer_name.lowercase().contains(q) ||
                it.doctor_name.lowercase().contains(q) ||
                it.hospital_clinic.lowercase().contains(q)
            }
        }
    }

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
                        text = "Doctor Prescriptions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Pine
                    )
                    Text(
                        text = "Relational Linkage: Customer + Doctor + Rx Items",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = { viewModel.loadPrescriptions() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Pine)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchChange(it) },
                label = { Text("Filter by patient, doctor, or clinic...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Pine)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.errorMessage != null) {
                ErrorBanner(
                    message = state.errorMessage!!,
                    onRetry = { viewModel.loadPrescriptions() }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.isLoading) {
                FullScreenLoading()
            } else if (filteredList.isEmpty()) {
                EmptyStateView(
                    title = "No Prescriptions Found",
                    subtitle = "No doctor prescription records match your criteria."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredList, key = { it.prescription_id }) { rx ->
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Patient: ${rx.customer_name}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Prescribed by Dr. ${rx.doctor_name} (${rx.specialization})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Pine,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    StatusBadge(status = "${rx.total_items} Items")
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Clinic: ${rx.hospital_clinic} • Date: ${rx.prescription_date.take(10)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                if (!rx.diagnosis_notes.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        shape = RoundedCornerShape(6.dp),
                                        colors = CardDefaults.cardColors(containerColor = Mint.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Diagnosis: ${rx.diagnosis_notes}",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
