package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a luxury Indian jewellery product stored locally in SQLite.
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val sku: String,
    val name: String,
    val description: String,
    val category: String,
    val collection: String,
    val metal: String,
    val purity: String,
    val weightGrams: String,
    val priceInr: Long,
    val originalPriceInr: Long? = null,
    val imageKey: String,
    val customImageUri: String? = null,
    val variantsCsv: String,
    val sizesCsv: String,
    val occasion: String,
    val careInstructions: String,
    val stockQuantity: Int,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isNewArrival: Boolean = false,
    val featuredOrder: Int = 100,
    val updatedAtMs: Long = System.currentTimeMillis()
)

typealias ProductEntity = Product
