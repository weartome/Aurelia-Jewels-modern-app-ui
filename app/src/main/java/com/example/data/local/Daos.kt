package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY featuredOrder ASC, updatedAtMs DESC")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeProductById(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("DELETE FROM products")
    suspend fun clearAllProducts()
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY addedAtMs DESC")
    fun observeCart(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items")
    suspend fun getCartSnapshot(): List<CartItemEntity>

    @Query("SELECT * FROM cart_items WHERE cartKey = :cartKey")
    suspend fun getCartItem(cartKey: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE cartKey = :cartKey")
    suspend fun deleteCartItem(cartKey: String)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItemsForProduct(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items ORDER BY savedAtMs DESC")
    fun observeWishlist(): Flow<List<WishlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId)")
    suspend fun isSaved(productId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(item: WishlistEntity)

    @Query("DELETE FROM wishlist_items WHERE productId = :productId")
    suspend fun removeFromWishlist(productId: String)
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses ORDER BY isDefault DESC, label ASC")
    fun observeAddresses(): Flow<List<AddressEntity>>

    @Query("SELECT COUNT(*) FROM addresses")
    suspend fun countAddresses(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAddress(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0")
    suspend fun clearDefaults()

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteAddress(id: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAtMs DESC")
    fun observeOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    fun observeOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAtMs DESC")
    fun observeNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun countNotifications(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotifications(items: List<NotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(item: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllRead()
}

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY searchedAtMs DESC LIMIT 10")
    fun observeRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSearch(item: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE query = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAll()
}

@Dao
interface StoreConfigDao {
    @Query("SELECT * FROM store_config WHERE id = 1")
    fun observeConfig(): Flow<StoreConfigEntity?>

    @Query("SELECT * FROM store_config WHERE id = 1")
    suspend fun getConfig(): StoreConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: StoreConfigEntity)

    @Update
    suspend fun updateConfig(config: StoreConfigEntity)
}
