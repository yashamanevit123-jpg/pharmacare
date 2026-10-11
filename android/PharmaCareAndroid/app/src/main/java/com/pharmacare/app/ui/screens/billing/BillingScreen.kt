package com.pharmacare.app.ui.screens.billing

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
import com.pharmacare.app.data.model.CustomerItem
import com.pharmacare.app.data.model.MedicineItem
import com.pharmacare.app.ui.components.*
import com.pharmacare.app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: BillingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    var showCustomerPicker by remember { mutableStateOf(false) }
    var showMedicinePicker by remember { mutableStateOf(false) }

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
                        text = "POS Billing & Dispensing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Pine
                    )
                    Text(
                        text = "ACID Transaction via sp_process_sale",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (state.cart.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearCart() }) {
                        Text("Clear Cart", color = StatusBad)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.errorMessage != null) {
                ErrorBanner(
                    message = state.errorMessage!!,
                    onRetry = { viewModel.checkout() }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Customer Selector Card
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCustomerPicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Customer",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = state.selectedCustomer?.let { "${it.name} (${it.phone})" } ?: "Select Customer",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Customer",
                        tint = Pine
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add Item Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Add Medicine to Cart",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Select Medicine Button
                    OutlinedButton(
                        onClick = { showMedicinePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.selectedMedicine?.name ?: "Tap to choose medicine...",
                            color = if (state.selectedMedicine != null) Pine else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Batch Selection (FEFO)
                    if (state.selectedMedicine != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select Batch (Ordered by FEFO - Expiry First):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.availableBatches.forEach { batch ->
                                val isSelected = state.selectedBatch?.batch_id == batch.batch_id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectBatch(batch) },
                                    label = {
                                        Text("${batch.batch_number} (Exp: ${batch.expiry_date} | Stock: ${batch.quantity})")
                                    }
                                )
                            }
                        }

                        // Quantity and Add Button
                        if (state.selectedBatch != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Qty: ", style = MaterialTheme.typography.bodySmall)
                                    IconButton(
                                        onClick = { viewModel.setAddQuantity(state.addQuantity - 1) },
                                        enabled = state.addQuantity > 1
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    Text(
                                        text = "${state.addQuantity}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { viewModel.setAddQuantity(state.addQuantity + 1) },
                                        enabled = state.addQuantity < (state.selectedBatch?.quantity ?: 1)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }

                                Button(
                                    onClick = { viewModel.addToCart() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Pine),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cart Items List
            Text(
                text = "Cart (${state.cart.size} items)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (state.cart.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cart is empty. Select medicines to build sale invoice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.cart, key = { it.batch.batch_id }) { item ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.medicine.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Batch: ${item.batch.batch_number} • ${item.quantity} x ₹${item.batch.selling_price}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "₹%.2f", item.subtotal),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Pine
                                )
                                IconButton(onClick = { viewModel.removeFromCart(item.batch.batch_id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = StatusBad,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment and Checkout Footer
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Mint.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Payment Method
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payment:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Cash", "UPI", "Card").forEach { method ->
                                FilterChip(
                                    selected = state.paymentMethod == method,
                                    onClick = { viewModel.setPaymentMethod(method) },
                                    label = { Text(method, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Discount buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Discount:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.0, 5.0, 10.0).forEach { disc ->
                                FilterChip(
                                    selected = state.discountPercent == disc,
                                    onClick = { viewModel.setDiscountPercent(disc) },
                                    label = { Text("${disc.toInt()}%", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LineLight)

                    // Totals
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(Locale.getDefault(), "₹%.2f", state.netTotal),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Pine
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.checkout() },
                        enabled = !state.isCheckingOut && state.cart.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Pine),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isCheckingOut) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Process Sale (sp_process_sale)")
                        }
                    }
                }
            }
        }

        // Customer Selection Dialog
        if (showCustomerPicker) {
            AlertDialog(
                onDismissRequest = { showCustomerPicker = false },
                title = { Text("Select Customer") },
                text = {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.customers, key = { it.customer_id }) { customer ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectCustomer(customer)
                                        showCustomerPicker = false
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(customer.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${customer.phone} ${if (customer.email != null) "• ${customer.email}" else ""}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCustomerPicker = false }) { Text("Cancel") }
                }
            )
        }

        // Medicine Selection Dialog
        if (showMedicinePicker) {
            AlertDialog(
                onDismissRequest = { showMedicinePicker = false },
                title = { Text("Select Medicine") },
                text = {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.medicines, key = { it.medicine_id }) { med ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectMedicine(med)
                                        showMedicinePicker = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(med.name, fontWeight = FontWeight.Bold)
                                        Text("${med.category_name} • ${med.dosage_form}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    StatusBadge(status = "${med.total_stock} stock")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMedicinePicker = false }) { Text("Cancel") }
                }
            )
        }

        // Checkout Success Modal
        if (state.checkoutResult != null) {
            val result = state.checkoutResult!!
            AlertDialog(
                onDismissRequest = { viewModel.dismissReceipt() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusOk)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sale Completed!")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Invoice Number: ${result.invoiceNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Pine
                        )
                        Text(
                            text = "Amount Paid: ₹%.2f".format(result.totalAmount),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Mint.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "ACID Verification Passed",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Pine
                                )
                                Text(
                                    text = "• Stock deducted via sp_process_sale\n• Audit logged via trg_after_batch_stock_update\n• Row-level locks released on COMMIT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissReceipt() },
                        colors = ButtonDefaults.buttonColors(containerColor = Pine)
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
