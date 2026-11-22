package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiGeneratedJewellerySample
import com.example.data.AiImageRepository
import com.example.data.AureliaRepository
import com.example.data.ImageResolution
import com.example.data.OrderSnapshotCodec
import com.example.data.PlaceholderAiImageRepository
import com.example.data.SeedData
import com.example.data.VariantOption
import com.example.data.local.AddressEntity
import com.example.data.local.AureliaDatabase
import com.example.data.local.CartItemEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.local.StoreConfigEntity
import com.example.data.parsedSizes
import com.example.data.parsedVariants
import com.example.domain.CommerceRules
import com.example.domain.OrderLineSummary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartDisplayItem(
    val entity: CartItemEntity,
    val product: ProductEntity,
    val unitPriceInr: Long
) {
    val lineTotalInr: Long get() = unitPriceInr * entity.quantity
}

enum class SortOption(val label: String) {
    RECOMMENDED("Recommended"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    NEWEST("Newest"),
    NAME_ASC("Name: A to Z")
}

data class CatalogueFilterState(
    val category: String? = null,
    val metal: String? = null,
    val purity: String? = null,
    val occasion: String? = null,
    val maxPriceInr: Long? = null,
    val inStockOnly: Boolean = false,
    val sortOption: SortOption = SortOption.RECOMMENDED
) {
    val activeCount: Int
        get() = listOfNotNull(category, metal, purity, occasion, maxPriceInr).size +
            if (inStockOnly) 1 else 0
}

sealed interface BespokeAiUiState {
    data object Idle : BespokeAiUiState
    data object Loading : BespokeAiUiState
    data class Success(val sample: AiGeneratedJewellerySample) : BespokeAiUiState
    data class Error(val message: String) : BespokeAiUiState
}

class AureliaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AureliaDatabase.getInstance(application)
    private val repository = AureliaRepository(database)
    private val aiImageRepository: AiImageRepository = PlaceholderAiImageRepository()

    private val _isSeeded = MutableStateFlow(false)
    val isSeeded: StateFlow<Boolean> = _isSeeded.asStateFlow()

    val products: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistIds: StateFlow<Set<String>> = repository.wishlistIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val addresses: StateFlow<List<AddressEntity>> = repository.addresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<String>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storeConfig: StateFlow<StoreConfigEntity> = repository.storeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedData.defaultStoreConfig())

    val cartDisplayItems: StateFlow<List<CartDisplayItem>> = combine(
        repository.cartItems,
        products
    ) { cartEntities, catalog ->
        val map = catalog.associateBy { it.id }
        cartEntities.mapNotNull { item ->
            val product = map[item.productId] ?: return@mapNotNull null
            val unitPrice = (product.priceInr + item.variantPriceDeltaInr).coerceAtLeast(100L)
            CartDisplayItem(item, product, unitPrice)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItemCount: StateFlow<Int> = cartDisplayItems.combine(storeConfig) { items, _ ->
        items.sumOf { it.entity.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadNotificationCount: StateFlow<Int> = notifications.combine(storeConfig) { list, _ ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _appliedCouponCode = MutableStateFlow<String?>(null)
    val appliedCouponCode: StateFlow<String?> = _appliedCouponCode.asStateFlow()

    private val _selectedDeliveryExpress = MutableStateFlow(false)
    val selectedDeliveryExpress: StateFlow<Boolean> = _selectedDeliveryExpress.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    private val _bespokeAiState = MutableStateFlow<BespokeAiUiState>(BespokeAiUiState.Idle)
    val bespokeAiState: StateFlow<BespokeAiUiState> = _bespokeAiState.asStateFlow()

    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            _isSeeded.value = true
        }
    }

    fun emitMessage(message: String) {
        _toastMessages.tryEmit(message)
    }

    fun completeOnboarding() {
        viewModelScope.launch { repository.completeOnboarding() }
    }

    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            val added = repository.toggleWishlist(productId)
            emitMessage(if (added) "Saved to Wishlist" else "Removed from Wishlist")
        }
    }

    fun addToCart(
        product: ProductEntity,
        variant: VariantOption = product.parsedVariants().first(),
        size: String = product.parsedSizes().first(),
        quantity: Int = 1,
        onAdded: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.addToCart(product, variant, size, quantity)
            result.onSuccess {
                emitMessage("${product.name} added to Bag")
                onAdded?.invoke()
            }.onFailure { err ->
                emitMessage(err.message ?: "Unable to add item")
            }
        }
    }

    fun updateCartQuantity(cartKey: String, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartKey, newQty)
        }
    }

    fun removeCartItem(cartKey: String) {
        viewModelScope.launch {
            repository.removeCartItem(cartKey)
            emitMessage("Item removed from Bag")
        }
    }

    fun applyCoupon(code: String): CommerceRules.CouponValidation {
        val subtotal = cartDisplayItems.value.sumOf { it.lineTotalInr }
        val validation = CommerceRules.validateCoupon(code, subtotal)
        if (validation.isValid) {
            _appliedCouponCode.value = code.trim().uppercase()
        }
        return validation
    }

    fun clearCoupon() {
        _appliedCouponCode.value = null
    }

    fun setExpressDelivery(express: Boolean) {
        _selectedDeliveryExpress.value = express
    }

    fun saveAddress(address: AddressEntity) {
        viewModelScope.launch {
            repository.saveAddress(address)
            emitMessage("Address saved")
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            repository.deleteAddress(id)
            emitMessage("Address deleted")
        }
    }

    fun placeDemoOrder(
        address: AddressEntity,
        paymentMethod: String,
        onOrderCreated: (OrderEntity) -> Unit
    ) {
        if (_isPlacingOrder.value) return
        viewModelScope.launch {
            _isPlacingOrder.value = true
            try {
                val cfg = storeConfig.value
                val isExpress = _selectedDeliveryExpress.value
                val shipping = if (isExpress) cfg.expressShippingInr else cfg.standardShippingInr
                val deliveryLabel = if (isExpress) "Express Demo Delivery" else "Standard Demo Delivery"
                val result = repository.placeDemoOrder(
                    address = address,
                    deliveryMethod = deliveryLabel,
                    shippingChargeInr = shipping,
                    paymentMethod = paymentMethod,
                    couponCode = _appliedCouponCode.value
                )
                result.onSuccess { order ->
                    _appliedCouponCode.value = null
                    _selectedDeliveryExpress.value = false
                    onOrderCreated(order)
                }.onFailure { err ->
                    emitMessage(err.message ?: "Unable to place demo order")
                }
            } finally {
                _isPlacingOrder.value = false
            }
        }
    }

    fun reorderFromOrder(order: OrderEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            val items = OrderSnapshotCodec.decode(order.itemsSnapshotSerialized)
            val catalog = products.value.associateBy { it.id }
            var addedCount = 0
            items.forEach { summary: OrderLineSummary ->
                val prod = catalog[summary.productId]
                if (prod != null && prod.stockQuantity > 0) {
                    val matchedVariant = prod.parsedVariants()
                        .firstOrNull { it.label == summary.selectedVariant }
                        ?: prod.parsedVariants().first()
                    repository.addToCart(prod, matchedVariant, summary.selectedSize, summary.quantity)
                    addedCount++
                }
            }
            if (addedCount > 0) {
                emitMessage("Added $addedCount available piece(s) to Bag")
                onDone()
            } else {
                emitMessage("Items from this order are no longer in stock")
            }
        }
    }

    fun advanceOrderStatus(orderId: String) {
        viewModelScope.launch {
            val next = repository.advanceDemoOrderStatus(orderId)
            if (next != null) {
                emitMessage("Demo order #$orderId updated to $next")
            }
        }
    }

    fun recordSearch(query: String) {
        viewModelScope.launch { repository.recordSearch(query) }
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch { repository.removeRecentSearch(query) }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { repository.clearRecentSearches() }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            emitMessage("All notifications marked as read")
        }
    }

    fun saveStoreConfig(config: StoreConfigEntity) {
        viewModelScope.launch {
            repository.saveStoreConfig(config)
            emitMessage("Store configuration saved")
        }
    }

    fun upsertProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.upsertProduct(product)
            emitMessage("${product.name} saved")
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            emitMessage("Product deleted from demo catalogue")
        }
    }

    fun resetDemoConfiguration() {
        viewModelScope.launch {
            repository.resetDemoData()
            emitMessage("Demo store reset to defaults")
        }
    }

    fun generateBespokeJewelleryImage(prompt: String, resolution: ImageResolution) {
        viewModelScope.launch {
            _bespokeAiState.value = BespokeAiUiState.Loading
            val res = aiImageRepository.generateBespokeConcept(prompt, resolution)
            res.onSuccess { sample ->
                _bespokeAiState.value = BespokeAiUiState.Success(sample)
            }.onFailure { err ->
                _bespokeAiState.value = BespokeAiUiState.Error(err.message ?: "Unable to generate concept")
            }
        }
    }

    fun clearBespokeAiState() {
        _bespokeAiState.value = BespokeAiUiState.Idle
    }

    fun filterProducts(
        allProducts: List<ProductEntity>,
        filter: CatalogueFilterState,
        searchQuery: String = ""
    ): List<ProductEntity> {
        val q = searchQuery.trim().lowercase()
        val filtered = allProducts.filter { p ->
            val matchesCategory = filter.category == null || p.category.equals(filter.category, ignoreCase = true)
            val matchesMetal = filter.metal == null || p.metal.equals(filter.metal, ignoreCase = true)
            val matchesPurity = filter.purity == null || p.purity.equals(filter.purity, ignoreCase = true)
            val matchesOccasion = filter.occasion == null || p.occasion.equals(filter.occasion, ignoreCase = true)
            val matchesPrice = filter.maxPriceInr == null || p.priceInr <= filter.maxPriceInr
            val matchesStock = !filter.inStockOnly || p.stockQuantity > 0
            val matchesQuery = q.isBlank() ||
                p.name.lowercase().contains(q) ||
                p.category.lowercase().contains(q) ||
                p.metal.lowercase().contains(q) ||
                p.purity.lowercase().contains(q) ||
                p.collection.lowercase().contains(q) ||
                p.occasion.lowercase().contains(q)
            matchesCategory && matchesMetal && matchesPurity && matchesOccasion && matchesPrice && matchesStock && matchesQuery
        }

        return when (filter.sortOption) {
            SortOption.RECOMMENDED -> filtered.sortedWith(
                compareByDescending<ProductEntity> { it.isFeatured }.thenBy { it.featuredOrder }
            )
            SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.priceInr }
            SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.priceInr }
            SortOption.NEWEST -> filtered.sortedWith(
                compareByDescending<ProductEntity> { it.isNewArrival }.thenByDescending { it.updatedAtMs }
            )
            SortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
        }
    }
}
