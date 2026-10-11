package com.pharmacare.app.ui.screens.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmacare.app.data.model.*
import com.pharmacare.app.data.repository.InventoryRepository
import com.pharmacare.app.data.repository.PurchasesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PurchasesUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val purchases: List<PurchaseHistoryItem> = emptyList(),
    val suppliers: List<SupplierItem> = emptyList(),
    val medicines: List<MedicineItem> = emptyList(),
    val selectedSupplier: SupplierItem? = null,
    val invoiceNumber: String = "",
    val selectedMedicine: MedicineItem? = null,
    val batchNumber: String = "",
    val expiryDate: String = "2027-12-31",
    val quantity: Int = 100,
    val unitCost: Double = 25.0,
    val sellingPrice: Double = 40.0,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class PurchasesViewModel(
    private val purchasesRepository: PurchasesRepository = PurchasesRepository(),
    private val inventoryRepository: InventoryRepository = InventoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchasesUiState(isLoading = true))
    val uiState: StateFlow<PurchasesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val supResult = purchasesRepository.getSuppliers()
            val purResult = purchasesRepository.getPurchases()
            val medResult = inventoryRepository.getMedicines()

            val suppliers = supResult.getOrDefault(emptyList())
            val purchases = purResult.getOrDefault(emptyList())
            val meds = medResult.getOrDefault(emptyList())

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                suppliers = suppliers,
                purchases = purchases,
                medicines = meds,
                selectedSupplier = suppliers.firstOrNull(),
                selectedMedicine = meds.firstOrNull(),
                invoiceNumber = "PO-M-" + System.currentTimeMillis().toString().takeLast(6)
            )
        }
    }

    fun selectSupplier(supplier: SupplierItem) {
        _uiState.value = _uiState.value.copy(selectedSupplier = supplier)
    }

    fun selectMedicine(medicine: MedicineItem) {
        _uiState.value = _uiState.value.copy(selectedMedicine = medicine)
    }

    fun onInvoiceNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(invoiceNumber = value)
    }

    fun onBatchNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(batchNumber = value)
    }

    fun onExpiryDateChange(value: String) {
        _uiState.value = _uiState.value.copy(expiryDate = value)
    }

    fun onQuantityChange(value: Int) {
        _uiState.value = _uiState.value.copy(quantity = value)
    }

    fun onUnitCostChange(value: Double) {
        _uiState.value = _uiState.value.copy(unitCost = value)
    }

    fun onSellingPriceChange(value: Double) {
        _uiState.value = _uiState.value.copy(sellingPrice = value)
    }

    fun submitPurchase() {
        val state = _uiState.value
        val supplier = state.selectedSupplier ?: return
        val medicine = state.selectedMedicine ?: return

        if (state.batchNumber.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Batch number cannot be blank")
            return
        }

        _uiState.value = state.copy(isSubmitting = true, errorMessage = null, successMessage = null)

        viewModelScope.launch {
            val payload = CreatePurchaseRequest(
                supplierId = supplier.supplier_id,
                invoiceNumber = state.invoiceNumber,
                items = listOf(
                    PurchaseItemPayload(
                        medicineId = medicine.medicine_id,
                        batchNumber = state.batchNumber,
                        expiryDate = state.expiryDate,
                        quantity = state.quantity,
                        unitCost = state.unitCost,
                        sellingPrice = state.sellingPrice
                    )
                )
            )

            val result = purchasesRepository.createPurchase(payload)
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    successMessage = "PO created! Batch ${state.batchNumber} stock added into MySQL.",
                    invoiceNumber = "PO-M-" + System.currentTimeMillis().toString().takeLast(6),
                    batchNumber = ""
                )
                loadData()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = err.message ?: "Failed to process purchase order"
                )
            }
        }
    }

    fun dismissSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
