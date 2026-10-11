package com.pharmacare.app.ui.screens.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmacare.app.data.model.*
import com.pharmacare.app.data.repository.InventoryRepository
import com.pharmacare.app.data.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartItem(
    val medicine: MedicineItem,
    val batch: BatchItem,
    var quantity: Int
) {
    val subtotal: Double
        get() = batch.selling_price * quantity
}

data class BillingUiState(
    val isLoading: Boolean = false,
    val isCheckingOut: Boolean = false,
    val customers: List<CustomerItem> = emptyList(),
    val medicines: List<MedicineItem> = emptyList(),
    val availableBatches: List<BatchItem> = emptyList(),
    val selectedCustomer: CustomerItem? = null,
    val selectedMedicine: MedicineItem? = null,
    val selectedBatch: BatchItem? = null,
    val addQuantity: Int = 1,
    val paymentMethod: String = "Cash",
    val discountPercent: Double = 0.0,
    val cart: List<CartItem> = emptyList(),
    val checkoutResult: SaleResultData? = null,
    val errorMessage: String? = null
) {
    val grossTotal: Double
        get() = cart.sumOf { it.subtotal }

    val discountAmount: Double
        get() = grossTotal * (discountPercent / 100.0)

    val netTotal: Double
        get() = (grossTotal - discountAmount).coerceAtLeast(0.0)
}

class BillingViewModel(
    private val salesRepository: SalesRepository = SalesRepository(),
    private val inventoryRepository: InventoryRepository = InventoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState(isLoading = true))
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val customersResult = salesRepository.getCustomers()
            val medicinesResult = inventoryRepository.getMedicines(status = "In Stock")

            val customers = customersResult.getOrDefault(emptyList())
            val meds = medicinesResult.getOrDefault(emptyList())

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                customers = customers,
                medicines = meds,
                selectedCustomer = customers.firstOrNull()
            )
        }
    }

    fun selectCustomer(customer: CustomerItem) {
        _uiState.value = _uiState.value.copy(selectedCustomer = customer)
    }

    fun selectMedicine(medicine: MedicineItem) {
        _uiState.value = _uiState.value.copy(
            selectedMedicine = medicine,
            selectedBatch = null,
            availableBatches = emptyList(),
            addQuantity = 1
        )
        viewModelScope.launch {
            val result = inventoryRepository.getAvailableBatches(medicine.medicine_id)
            result.onSuccess { batches ->
                _uiState.value = _uiState.value.copy(
                    availableBatches = batches,
                    selectedBatch = batches.firstOrNull() // FEFO first
                )
            }
        }
    }

    fun selectBatch(batch: BatchItem) {
        _uiState.value = _uiState.value.copy(
            selectedBatch = batch,
            addQuantity = 1
        )
    }

    fun setAddQuantity(quantity: Int) {
        val maxAvailable = _uiState.value.selectedBatch?.quantity ?: 1
        _uiState.value = _uiState.value.copy(addQuantity = quantity.coerceIn(1, maxAvailable))
    }

    fun setPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun setDiscountPercent(discount: Double) {
        _uiState.value = _uiState.value.copy(discountPercent = discount.coerceIn(0.0, 50.0))
    }

    fun addToCart() {
        val state = _uiState.value
        val med = state.selectedMedicine ?: return
        val batch = state.selectedBatch ?: return
        val qty = state.addQuantity

        val existingIndex = state.cart.indexOfFirst { it.batch.batch_id == batch.batch_id }
        val updatedCart = state.cart.toMutableList()

        if (existingIndex >= 0) {
            val existing = updatedCart[existingIndex]
            val newQty = (existing.quantity + qty).coerceAtMost(batch.quantity)
            updatedCart[existingIndex] = existing.copy(quantity = newQty)
        } else {
            updatedCart.add(CartItem(med, batch, qty))
        }

        _uiState.value = _uiState.value.copy(
            cart = updatedCart,
            selectedMedicine = null,
            selectedBatch = null,
            availableBatches = emptyList()
        )
    }

    fun removeFromCart(batchId: Int) {
        val updated = _uiState.value.cart.filterNot { it.batch.batch_id == batchId }
        _uiState.value = _uiState.value.copy(cart = updated)
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(cart = emptyList(), checkoutResult = null, errorMessage = null)
    }

    fun checkout() {
        val state = _uiState.value
        val customer = state.selectedCustomer
        if (customer == null) {
            _uiState.value = state.copy(errorMessage = "Please select a customer")
            return
        }
        if (state.cart.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Cart is empty")
            return
        }

        _uiState.value = state.copy(isCheckingOut = true, errorMessage = null)

        viewModelScope.launch {
            val payload = CreateSaleRequest(
                customerId = customer.customer_id,
                paymentMethod = state.paymentMethod,
                discountAmount = state.discountAmount,
                items = state.cart.map {
                    SaleItemPayload(
                        batchId = it.batch.batch_id,
                        quantity = it.quantity
                    )
                }
            )

            val result = salesRepository.createSale(payload)
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isCheckingOut = false,
                    cart = emptyList(),
                    checkoutResult = response.data
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isCheckingOut = false,
                    errorMessage = err.message ?: "Transaction failed and rolled back."
                )
            }
        }
    }

    fun dismissReceipt() {
        _uiState.value = _uiState.value.copy(checkoutResult = null)
    }
}
