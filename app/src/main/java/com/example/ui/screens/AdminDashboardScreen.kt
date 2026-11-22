package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderSnapshotCodec
import com.example.data.SeedData
import com.example.data.local.AddressEntity
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.domain.InrFormatter
import com.example.ui.CartDisplayItem
import com.example.ui.components.EmptyLuxuryState
import com.example.ui.components.JewelleryPhoto
import com.example.ui.components.LuxuryTopBar
import com.example.ui.components.PrimaryOliveButton
import com.example.ui.theme.AureliaJewelsTheme
import com.example.ui.theme.LocalAureliaColors

private enum class AdminTab(val label: String) {
    ORDERS("Order Management"),
    INVENTORY("Product Inventory"),
    ANALYTICS("User Analytics")
}

private enum class InventoryFilter(val label: String) {
    ALL("All SKUs"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock"),
    ON_SALE("On Sale")
}

/**
 * Isolated Admin Dashboard Screen completely separate from the customer shopping UI.
 * Provides tabs for Order Management, Product Inventory, and User Analytics backed by live Room data.
 */
@Composable
fun AdminDashboardScreen(
    orders: List<OrderEntity>,
    products: List<ProductEntity>,
    wishlistIds: Set<String>,
    cartItems: List<CartDisplayItem>,
    addresses: List<AddressEntity>,
    onBack: () -> Unit,
    onAdvanceOrderStatus: (String) -> Unit,
    onUpsertProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onOpenBrandStudio: () -> Unit
) {
    val ext = LocalAureliaColors.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = AdminTab.entries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("admin_dashboard_screen")
    ) {
        LuxuryTopBar(
            title = "Admin Dashboard",
            subtitle = "Isolated Store Operations Console",
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = onOpenBrandStudio,
                    modifier = Modifier.testTag("admin_open_brand_studio_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Open Brand & Theme Studio",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        // Separation Notice Banner
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color(0xFFFCF6BA),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Separated Admin Console (Local Demo). Production deployment isolates this to admin web portal.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFAF7F2),
                    maxLines = 2
                )
            }
        }

        // 3 Dedicated Admin Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}"),
                    text = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        HorizontalDivider(thickness = 0.75.dp, color = ext.hairlineBorder)

        when (tabs[selectedTabIndex]) {
            AdminTab.ORDERS -> AdminOrdersTab(
                orders = orders,
                onAdvanceOrderStatus = onAdvanceOrderStatus,
                modifier = Modifier.weight(1f)
            )
            AdminTab.INVENTORY -> AdminInventoryTab(
                products = products,
                onUpsertProduct = onUpsertProduct,
                onDeleteProduct = onDeleteProduct,
                onOpenBrandStudio = onOpenBrandStudio,
                modifier = Modifier.weight(1f)
            )
            AdminTab.ANALYTICS -> AdminAnalyticsTab(
                orders = orders,
                products = products,
                wishlistIds = wishlistIds,
                cartItems = cartItems,
                addresses = addresses,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AdminOrdersTab(
    orders: List<OrderEntity>,
    onAdvanceOrderStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalAureliaColors.current
    val activeOrdersCount = orders.count { !it.status.equals("Delivered", ignoreCase = true) }
    val deliveredCount = orders.count { it.status.equals("Delivered", ignoreCase = true) }
    val totalGmvInr = orders.sumOf { it.grandTotalInr }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_orders_tab_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminKpiCard("Active Orders", activeOrdersCount.toString(), Modifier.weight(1f))
                AdminKpiCard("Delivered", deliveredCount.toString(), Modifier.weight(1f))
                AdminKpiCard("Demo GMV", InrFormatter.format(totalGmvInr), Modifier.weight(1.3f))
            }
        }

        if (orders.isEmpty()) {
            item {
                EmptyLuxuryState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No Demo Orders Yet",
                    subtitle = "Orders placed from the customer checkout flow will appear here for stage management."
                )
            }
        } else {
            items(orders, key = { it.orderId }) { order ->
                val itemsList = remember(order.itemsSnapshotSerialized) {
                    OrderSnapshotCodec.decode(order.itemsSnapshotSerialized)
                }
                val isDelivered = order.status.equals("Delivered", ignoreCase = true)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.75.dp, ext.hairlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_order_card_${order.orderId}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Order #${order.orderId}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${order.customerName}  |  ${InrFormatter.formatDateTime(order.createdAtMs)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDelivered) Color(0xFFEBF5EE) else ext.champagne
                            ) {
                                Text(
                                    text = order.status,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isDelivered) ext.success else ext.deepWarmBrown,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${itemsList.sumOf { it.quantity }} piece(s)  |  ${order.paymentMethod}  |  ${order.deliveryMethod}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = order.deliveryAddressSnapshot,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = ext.hairlineBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = InrFormatter.format(order.grandTotalInr),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!isDelivered) {
                                PrimaryOliveButton(
                                    text = "Advance Stage",
                                    leadingIcon = Icons.Outlined.LocalShipping,
                                    onClick = { onAdvanceOrderStatus(order.orderId) },
                                    modifier = Modifier.height(40.dp),
                                    testTag = "admin_advance_order_${order.orderId}"
                                )
                            } else {
                                Text(
                                    text = "Fulfilment Completed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ext.success
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminInventoryTab(
    products: List<ProductEntity>,
    onUpsertProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onOpenBrandStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalAureliaColors.current
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(InventoryFilter.ALL) }
    var pendingDeleteProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val lowStockCount = products.count { it.stockQuantity in 1..2 }
    val outOfStockCount = products.count { it.stockQuantity <= 0 }
    val onSaleCount = products.count { it.originalPriceInr != null && it.originalPriceInr > it.priceInr }

    val visibleProducts = remember(products, searchQuery, activeFilter) {
        val q = searchQuery.trim().lowercase()
        products.filter { p ->
            val matchesFilter = when (activeFilter) {
                InventoryFilter.ALL -> true
                InventoryFilter.LOW_STOCK -> p.stockQuantity in 1..2
                InventoryFilter.OUT_OF_STOCK -> p.stockQuantity <= 0
                InventoryFilter.ON_SALE -> p.originalPriceInr != null && p.originalPriceInr > p.priceInr
            }
            val matchesQuery = q.isBlank() ||
                p.name.lowercase().contains(q) ||
                p.sku.lowercase().contains(q) ||
                p.category.lowercase().contains(q) ||
                p.metal.lowercase().contains(q)
            matchesFilter && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_inventory_tab_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminKpiCard("SKUs", products.size.toString(), Modifier.weight(1f))
                AdminKpiCard("Low Stock", lowStockCount.toString(), Modifier.weight(1f))
                AdminKpiCard("Out", outOfStockCount.toString(), Modifier.weight(0.85f))
                AdminKpiCard("On Sale", onSaleCount.toString(), Modifier.weight(1f))
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search SKU, name, metal, category...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_inventory_search")
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(InventoryFilter.entries) { filter ->
                    val selected = activeFilter == filter
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            0.85.dp,
                            if (selected) MaterialTheme.colorScheme.primary else ext.hairlineBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { activeFilter = filter }
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ext.secondaryCream,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenBrandStudio)
                    .testTag("admin_add_edit_studio_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Create or Edit Jewellery SKUs", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Open Brand & Product Studio to upload photos or edit pricing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Open Brand Studio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (visibleProducts.isEmpty()) {
            item {
                EmptyLuxuryState(
                    icon = Icons.Outlined.Inventory2,
                    title = "No Matching Inventory",
                    subtitle = "Try clearing the search query or switching the stock filter."
                )
            }
        } else {
            items(visibleProducts, key = { it.id }) { product ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.75.dp, ext.hairlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_inventory_item_${product.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ext.secondaryCream,
                            modifier = Modifier.size(64.dp)
                        ) {
                            JewelleryPhoto(
                                imageKey = product.imageKey,
                                customImageUri = product.customImageUri,
                                metal = product.metal,
                                contentDescription = product.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${product.sku}  |  ${product.category}  |  ${product.metal}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = InrFormatter.format(product.priceInr),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (product.stockQuantity > 0) {
                                            onUpsertProduct(
                                                product.copy(
                                                    stockQuantity = product.stockQuantity - 1,
                                                    updatedAtMs = System.currentTimeMillis()
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("admin_stock_dec_${product.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Remove,
                                        contentDescription = "Decrease stock",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "${product.stockQuantity}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (product.stockQuantity <= 2) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = {
                                        onUpsertProduct(
                                            product.copy(
                                                stockQuantity = product.stockQuantity + 1,
                                                updatedAtMs = System.currentTimeMillis()
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("admin_stock_inc_${product.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = "Increase stock",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { pendingDeleteProduct = product },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("admin_delete_${product.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Delete ${product.name}",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteProduct?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteProduct = null },
            title = { Text("Delete Product?") },
            text = { Text("Remove \"${target.name}\" (${target.sku}) from local demo inventory?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProduct(target.id)
                        pendingDeleteProduct = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteProduct = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminAnalyticsTab(
    orders: List<OrderEntity>,
    products: List<ProductEntity>,
    wishlistIds: Set<String>,
    cartItems: List<CartDisplayItem>,
    addresses: List<AddressEntity>,
    modifier: Modifier = Modifier
) {
    val ext = LocalAureliaColors.current
    val totalUnitsInBag = cartItems.sumOf { it.entity.quantity }
    val avgOrderValue = if (orders.isNotEmpty()) orders.sumOf { it.grandTotalInr } / orders.size else 0L
    val metalBreakdown = remember(products) {
        products.groupingBy { it.metal }.eachCount().entries.sortedByDescending { it.value }
    }
    val categoryBreakdown = remember(products) {
        products.groupBy { it.category }.map { (cat, list) ->
            Triple(cat, list.size, list.sumOf { it.priceInr } / list.size.coerceAtLeast(1))
        }.sortedByDescending { it.second }.take(6)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_analytics_tab_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Customer Funnel (Live Local Device)", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminKpiCard("Wishlist Saves", wishlistIds.size.toString(), Modifier.weight(1f))
                AdminKpiCard("Bag Units", totalUnitsInBag.toString(), Modifier.weight(1f))
                AdminKpiCard("Addresses", addresses.size.toString(), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminKpiCard("Orders Placed", orders.size.toString(), Modifier.weight(1f))
                AdminKpiCard(
                    "Avg. Order Value",
                    if (avgOrderValue > 0L) InrFormatter.format(avgOrderValue) else "N/A",
                    Modifier.weight(1.3f)
                )
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = ext.antiqueGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Catalogue Distribution by Metal", style = MaterialTheme.typography.titleMedium)
                    }
                    val total = products.size.coerceAtLeast(1).toFloat()
                    metalBreakdown.forEach { (metal, count) ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(metal, style = MaterialTheme.typography.bodySmall)
                                Text("$count SKUs", style = MaterialTheme.typography.labelSmall, color = ext.antiqueGold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { count / total },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = ext.secondaryCream
                            )
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Top Categories by SKU & Avg. Price", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    categoryBreakdown.forEachIndexed { idx, (category, count, avgPrice) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$category ($count)", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "Avg ${InrFormatter.format(avgPrice)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (idx < categoryBreakdown.lastIndex) {
                            HorizontalDivider(thickness = 0.5.dp, color = ext.hairlineBorder)
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ext.secondaryCream,
                border = BorderStroke(0.75.dp, ext.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Analytics Note: Figures above are computed live from local Room SQLite tables on this device. Multi-customer cohort analytics will connect once the standalone cloud backend (docs/backend_prd.md) is provisioned.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val ext = LocalAureliaColors.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, ext.hairlineBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminDashboardScreenPreview() {
    AureliaJewelsTheme {
        AdminDashboardScreen(
            orders = emptyList(),
            products = SeedData.defaultProducts().take(6),
            wishlistIds = setOf("prod_ring_floral_22k"),
            cartItems = emptyList(),
            addresses = listOf(SeedData.defaultAddress()),
            onBack = {},
            onAdvanceOrderStatus = {},
            onUpsertProduct = {},
            onDeleteProduct = {},
            onOpenBrandStudio = {}
        )
    }
}
