package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Product::class,
        CartItemEntity::class,
        WishlistEntity::class,
        AddressEntity::class,
        OrderEntity::class,
        NotificationEntity::class,
        RecentSearchEntity::class,
        StoreConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AureliaDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun addressDao(): AddressDao
    abstract fun orderDao(): OrderDao
    abstract fun notificationDao(): NotificationDao
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun storeConfigDao(): StoreConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AureliaDatabase? = null

        fun getInstance(context: Context): AureliaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AureliaDatabase::class.java,
                    "aurelia_jewels_demo.db"
                ).fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
