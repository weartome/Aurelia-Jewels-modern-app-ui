package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val cartKey: String, // "$productId|$selectedVariant|$selectedSize"
    val productId: String,
    val selectedVariant: String,
    val variantPriceDeltaInr: Long,
    val selectedSize: String,
    val quantity: Int,
    val addedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistEntity(
    @PrimaryKey val productId: String,
    val savedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val label: String,
    val fullName: String,
    val phone: String,
    val line1: String,
    val line2: String,
    val landmark: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val createdAtMs: Long,
    val status: String, // Placed, Processing, Shipped, Out for Delivery, Delivered, Cancelled
    val customerName: String,
    val customerPhone: String,
    val deliveryAddressSnapshot: String,
    val deliveryMethod: String,
    val paymentMethod: String,
    val couponCode: String?,
    val subtotalInr: Long,
    val discountInr: Long,
    val shippingInr: Long,
    val grandTotalInr: Long,
    val itemsSnapshotSerialized: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String, // ORDER_CREATED, ORDER_UPDATED, COLLECTION, OFFER
    val targetRoute: String,
    val imageKey: String,
    val createdAtMs: Long,
    val isRead: Boolean = false
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val searchedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "store_config")
data class StoreConfigEntity(
    @PrimaryKey val id: Int = 1,
    val brandName: String = "AURELIA",
    val brandSubName: String = "JEWELS",
    val tagline: String = "Timeless Jewellery for Every Chapter of Your Life",
    val primaryColorHex: String = "#27351A",
    val accentColorHex: String = "#AD853C",
    val heroTitle: String = "The Bridal Collection",
    val heroSubtitle: String = "Heirloom 22K gold and polki craftsmanship curated for Indian weddings",
    val heroCta: String = "Explore Collection",
    val standardShippingInr: Long = 0L,
    val expressShippingInr: Long = 149L,
    val whatsappNumber: String = "919820011223",
    val contactPhone: String = "+91 98200 11223",
    val contactEmail: String = "concierge@aureliajewels.demo",
    val storeAddress: String = "Suite 14, Heritage Arcade, Zaveri Bazaar Road, Mumbai 400002 (Demo Store)",
    val customerName: String = "Ananya Deshmukh",
    val customerEmail: String = "ananya.demo@example.com",
    val customerPhone: String = "9876543210",
    val onboardingCompleted: Boolean = false
)
