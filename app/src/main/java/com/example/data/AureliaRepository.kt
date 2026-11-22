package com.example.data

import com.example.data.local.AddressEntity
import com.example.data.local.AureliaDatabase
import com.example.data.local.CartItemEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.local.RecentSearchEntity
import com.example.data.local.StoreConfigEntity
import com.example.data.local.WishlistEntity
import com.example.domain.CommerceRules
import com.example.domain.OrderLineSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class VariantOption(
    val label: String,
    val priceDeltaInr: Long
)

fun ProductEntity.parsedVariants(): List<VariantOption> {
    return variantsCsv.split("|")
        .mapNotNull { entry ->
            val parts = entry.split(":")
            val label = parts.firstOrNull()?.trim().orEmpty()
            if (label.isBlank()) null
            else VariantOption(label, parts.getOrNull(1)?.trim()?.toLongOrNull() ?: 0L)
        }
        .ifEmpty { listOf(VariantOption(purity.ifBlank { metal }, 0L)) }
}

fun ProductEntity.parsedSizes(): List<String> {
    return sizesCsv.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .ifEmpty { listOf("One Size") }
}

fun ProductEntity.requiresExplicitSize(): Boolean {
    val sizes = parsedSizes()
    return sizes.size > 1 || (sizes.size == 1 && !sizes.first().equals("One Size", ignoreCase = true))
}

object OrderSnapshotCodec {
    fun encode(items: List<OrderLineSummary>): String {
        val arr = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("productId", item.productId)
            obj.put("productName", item.productName)
            obj.put("imageKey", item.imageKey)
            obj.put("selectedVariant", item.selectedVariant)
            obj.put("selectedSize", item.selectedSize)
            obj.put("unitPriceInr", item.unitPriceInr)
            obj.put("quantity", item.quantity)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun decode(raw: String): List<OrderLineSummary> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                OrderLineSummary(
                    productId = obj.optString("productId"),
                    productName = obj.optString("productName"),
                    imageKey = obj.optString("imageKey", "img_ring_gold"),
                    selectedVariant = obj.optString("selectedVariant"),
                    selectedSize = obj.optString("selectedSize", "One Size"),
                    unitPriceInr = obj.optLong("unitPriceInr", 0L),
                    quantity = obj.optInt("quantity", 1)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

class AureliaRepository(private val db: AureliaDatabase) {

    val products: Flow<List<ProductEntity>> = db.productDao().observeAllProducts()
    val cartItems: Flow<List<CartItemEntity>> = db.cartDao().observeCart()
    val wishlistIds: Flow<Set<String>> = db.wishlistDao().observeWishlist().map { list ->
        list.map { it.productId }.toSet()
    }
    val addresses: Flow<List<AddressEntity>> = db.addressDao().observeAddresses()
    val orders: Flow<List<OrderEntity>> = db.orderDao().observeOrders()
    val notifications: Flow<List<NotificationEntity>> = db.notificationDao().observeNotifications()
    val recentSearches: Flow<List<String>> = db.recentSearchDao().observeRecentSearches().map { list ->
        list.map { it.query }
    }
    val storeConfig: Flow<StoreConfigEntity> = db.storeConfigDao().observeConfig().map {
        it ?: SeedData.defaultStoreConfig()
    }

    suspend fun ensureSeeded() {
        if (db.storeConfigDao().getConfig() == null) {
            db.storeConfigDao().upsertConfig(SeedData.defaultStoreConfig())
        }
        if (db.productDao().countProducts() == 0) {
            db.productDao().upsertProducts(SeedData.defaultProducts())
        }
        if (db.addressDao().countAddresses() == 0) {
            db.addressDao().upsertAddress(SeedData.defaultAddress())
        }
        if (db.notificationDao().countNotifications() == 0) {
            db.notificationDao().upsertNotifications(SeedData.defaultNotifications())
        }
    }

    suspend fun resetDemoData() {
        db.cartDao().clearCart()
        db.productDao().clearAllProducts()
        db.productDao().upsertProducts(SeedData.defaultProducts())
        val currentOnboarding = db.storeConfigDao().getConfig()?.onboardingCompleted ?: true
        db.storeConfigDao().upsertConfig(
            SeedData.defaultStoreConfig().copy(onboardingCompleted = currentOnboarding)
        )
    }

    suspend fun completeOnboarding() {
        val current = db.storeConfigDao().getConfig() ?: SeedData.defaultStoreConfig()
        db.storeConfigDao().upsertConfig(current.copy(onboardingCompleted = true))
    }

    suspend fun saveStoreConfig(config: StoreConfigEntity) {
        db.storeConfigDao().upsertConfig(config.copy(id = 1))
    }

    suspend fun upsertProduct(product: ProductEntity) {
        db.productDao().upsertProduct(product.copy(updatedAtMs = System.currentTimeMillis()))
    }

    suspend fun deleteProduct(productId: String) {
        db.productDao().deleteProductById(productId)
        db.cartDao().deleteCartItemsForProduct(productId)
        db.wishlistDao().removeFromWishlist(productId)
    }

    suspend fun toggleWishlist(productId: String): Boolean {
        val exists = db.wishlistDao().isSaved(productId)
        return if (exists) {
            db.wishlistDao().removeFromWishlist(productId)
            false
        } else {
            db.wishlistDao().addToWishlist(WishlistEntity(productId = productId))
            true
        }
    }

    suspend fun addToCart(
        product: ProductEntity,
        selectedVariant: VariantOption,
        selectedSize: String,
        quantityToAdd: Int
    ): Result<Unit> {
        if (product.stockQuantity <= 0) {
            return Result.failure(IllegalStateException("This piece is currently out of stock"))
        }
        val safeQty = quantityToAdd.coerceAtLeast(1)
        val cartKey = "${product.id}|${selectedVariant.label}|$selectedSize"
        val existing = db.cartDao().getCartItem(cartKey)
        val newQty = ((existing?.quantity ?: 0) + safeQty).coerceAtMost(product.stockQuantity.coerceAtLeast(1))
        db.cartDao().upsertCartItem(
            CartItemEntity(
                cartKey = cartKey,
                productId = product.id,
                selectedVariant = selectedVariant.label,
                variantPriceDeltaInr = selectedVariant.priceDeltaInr,
                selectedSize = selectedSize,
                quantity = newQty
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateCartQuantity(cartKey: String, newQuantity: Int) {
        val existing = db.cartDao().getCartItem(cartKey) ?: return
        if (newQuantity <= 0) {
            db.cartDao().deleteCartItem(cartKey)
            return
        }
        val product = db.productDao().getProductById(existing.productId)
        val maxStock = product?.stockQuantity?.coerceAtLeast(1) ?: 10
        db.cartDao().upsertCartItem(existing.copy(quantity = newQuantity.coerceAtMost(maxStock)))
    }

    suspend fun removeCartItem(cartKey: String) {
        db.cartDao().deleteCartItem(cartKey)
    }

    suspend fun saveAddress(address: AddressEntity) {
        if (address.isDefault) {
            db.addressDao().clearDefaults()
        }
        db.addressDao().upsertAddress(address)
    }

    suspend fun deleteAddress(id: String) {
        db.addressDao().deleteAddress(id)
    }

    suspend fun recordSearch(query: String) {
        val clean = query.trim()
        if (clean.length >= 2) {
            db.recentSearchDao().upsertSearch(RecentSearchEntity(query = clean))
        }
    }

    suspend fun removeRecentSearch(query: String) {
        db.recentSearchDao().deleteSearch(query)
    }

    suspend fun clearRecentSearches() {
        db.recentSearchDao().clearAll()
    }

    suspend fun placeDemoOrder(
        address: AddressEntity,
        deliveryMethod: String,
        shippingChargeInr: Long,
        paymentMethod: String,
        couponCode: String?
    ): Result<OrderEntity> {
        val cartSnapshot = db.cartDao().getCartSnapshot()
        if (cartSnapshot.isEmpty()) {
            return Result.failure(IllegalStateException("Your shopping bag is empty"))
        }

        val lineSummaries = mutableListOf<OrderLineSummary>()
        for (cartItem in cartSnapshot) {
            val product = db.productDao().getProductById(cartItem.productId)
                ?: return Result.failure(IllegalStateException("An item in your bag is no longer available"))
            if (product.stockQuantity <= 0) {
                return Result.failure(IllegalStateException("${product.name} is currently out of stock"))
            }
            val unitPrice = (product.priceInr + cartItem.variantPriceDeltaInr).coerceAtLeast(100L)
            lineSummaries.add(
                OrderLineSummary(
                    productId = product.id,
                    productName = product.name,
                    imageKey = product.imageKey,
                    selectedVariant = cartItem.selectedVariant,
                    selectedSize = cartItem.selectedSize,
                    unitPriceInr = unitPrice,
                    quantity = cartItem.quantity.coerceIn(1, product.stockQuantity)
                )
            )
        }

        val breakdown = CommerceRules.calculateTotals(
            itemTotalsInr = lineSummaries.map { it.lineTotalInr },
            couponCode = couponCode,
            shippingChargeInr = shippingChargeInr
        )

        val now = System.currentTimeMillis()
        val uniqueOrderId = "AJ${(now % 1_000_000L).toString().padStart(6, '0')}"
        val addressText = buildString {
            append(address.line1)
            if (address.line2.isNotBlank()) append(", ${address.line2}")
            if (address.landmark.isNotBlank()) append(" (${address.landmark})")
            append(", ${address.city}, ${address.state} - ${address.pinCode}")
        }

        val order = OrderEntity(
            orderId = uniqueOrderId,
            createdAtMs = now,
            status = "Placed",
            customerName = address.fullName,
            customerPhone = address.phone,
            deliveryAddressSnapshot = addressText,
            deliveryMethod = deliveryMethod,
            paymentMethod = paymentMethod,
            couponCode = breakdown.appliedCouponCode,
            subtotalInr = breakdown.subtotalInr,
            discountInr = breakdown.discountInr,
            shippingInr = breakdown.shippingInr,
            grandTotalInr = breakdown.grandTotalInr,
            itemsSnapshotSerialized = OrderSnapshotCodec.encode(lineSummaries)
        )

        db.orderDao().insertOrder(order)
        db.cartDao().clearCart()

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                title = "Demo Order #$uniqueOrderId Placed",
                message = "Your demo order for ${lineSummaries.size} item(s) has been recorded locally.",
                type = "ORDER_CREATED",
                targetRoute = "order_tracking/$uniqueOrderId",
                imageKey = lineSummaries.firstOrNull()?.imageKey ?: "img_ring_gold",
                createdAtMs = now,
                isRead = false
            )
        )

        return Result.success(order)
    }

    suspend fun advanceDemoOrderStatus(orderId: String): String? {
        val order = db.orderDao().getOrderById(orderId) ?: return null
        val stages = listOf("Placed", "Processing", "Shipped", "Out for Delivery", "Delivered")
        val idx = stages.indexOf(order.status)
        if (idx < 0 || idx >= stages.lastIndex) return order.status
        val next = stages[idx + 1]
        db.orderDao().updateOrderStatus(orderId, next)
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                title = "Demo Order #$orderId Updated",
                message = "Order status changed to $next (demo state update).",
                type = "ORDER_UPDATED",
                targetRoute = "order_tracking/$orderId",
                imageKey = OrderSnapshotCodec.decode(order.itemsSnapshotSerialized).firstOrNull()?.imageKey ?: "img_ring_gold",
                createdAtMs = System.currentTimeMillis(),
                isRead = false
            )
        )
        return next
    }

    suspend fun markNotificationRead(id: String) = db.notificationDao().markRead(id)
    suspend fun markAllNotificationsRead() = db.notificationDao().markAllRead()
}
