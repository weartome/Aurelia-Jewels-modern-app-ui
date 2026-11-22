package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SeedData
import com.example.data.local.Product
import com.example.domain.InrFormatter
import com.example.ui.components.EmptyLuxuryState
import com.example.ui.components.JewelleryPhoto
import com.example.ui.components.LuxuryTopBar
import com.example.ui.components.PrimaryOliveButton
import com.example.ui.components.SecondaryOutlinedButton
import com.example.ui.theme.AureliaJewelsTheme
import com.example.ui.theme.LocalAureliaColors

enum class JewelleryPriceRange(
    val label: String,
    val minInr: Long,
    val maxInr: Long?
) {
    UNDER_10K("Under ₹10,000", 0L, 10_000L),
    RANGE_10K_50K("₹10,000 - ₹50,000", 10_000L, 50_000L),
    RANGE_50K_150K("₹50,000 - ₹1,50,000", 50_000L, 1_50_000L),
    ABOVE_150K("Above ₹1,50,000", 1_50_000L, null);

    fun matches(priceInr: Long): Boolean {
        val aboveMin = priceInr >= minInr
        val belowMax = maxInr == null || priceInr <= maxInr
        return aboveMin && belowMax
    }
}

enum class GridSortOrder(val label: String) {
    FEATURED("Featured"),
    PRICE_ASC("Price: Low to High"),
    PRICE_DESC("Price: High to Low"),
    NEWEST("Newest Arrivals"),
    NAME_ASC("Name: A to Z")
}

data class GridFilterCriteria(
    val category: String? = null,
    val material: String? = null,
    val priceRange: JewelleryPriceRange? = null,
    val savedOnly: Boolean = false,
    val inStockOnly: Boolean = false,
    val sortOrder: GridSortOrder = GridSortOrder.FEATURED
) {
    val activeFilterCount: Int
        get() = listOfNotNull(category, material, priceRange).size +
            (if (savedOnly) 1 else 0) +
            (if (inStockOnly) 1 else 0)
}

fun applyJewelleryGridFilters(
    products: List<Product>,
    wishlistIds: Set<String>,
    criteria: GridFilterCriteria
): List<Product> {
    val filtered = products.filter { p ->
        val matchesCategory = criteria.category == null || p.category.equals(criteria.category, ignoreCase = true)
        val matchesMaterial = criteria.material == null || p.metal.equals(criteria.material, ignoreCase = true)
        val matchesPrice = criteria.priceRange == null || criteria.priceRange.matches(p.priceInr)
        val matchesSaved = !criteria.savedOnly || wishlistIds.contains(p.id)
        val matchesStock = !criteria.inStockOnly || p.stockQuantity > 0
        matchesCategory && matchesMaterial && matchesPrice && matchesSaved && matchesStock
    }
    return when (criteria.sortOrder) {
        GridSortOrder.FEATURED -> filtered.sortedWith(
            compareByDescending<Product> { it.isFeatured }.thenBy { it.featuredOrder }
        )
        GridSortOrder.PRICE_ASC -> filtered.sortedBy { it.priceInr }
        GridSortOrder.PRICE_DESC -> filtered.sortedByDescending { it.priceInr }
        GridSortOrder.NEWEST -> filtered.sortedWith(
            compareByDescending<Product> { it.isNewArrival }.thenByDescending { it.updatedAtMs }
        )
        GridSortOrder.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
    }
}

/**
 * Adaptive luxury jewellery grid screen backed by Room Product & Wishlist persistence,
 * complete with Top Bar Filter/Sort sheets (Category, Material, Price Range, Saved Only).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LuxuryJewelleryGridScreen(
    products: List<Product>,
    wishlistIds: Set<String>,
    cartCount: Int = 0,
    title: String = "Luxury Jewellery",
    onBack: (() -> Unit)? = null,
    onOpenSearch: (() -> Unit)? = null,
    onOpenWishlist: (() -> Unit)? = null,
    onOpenCart: (() -> Unit)? = null,
    onProductClick: (Product) -> Unit,
    onToggleWishlist: (String) -> Unit
) {
    val ext = LocalAureliaColors.current
    var criteria by remember { mutableStateOf(GridFilterCriteria()) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    val displayedProducts = remember(products, wishlistIds, criteria) {
        applyJewelleryGridFilters(products, wishlistIds, criteria)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .testTag("luxury_jewellery_grid_screen")
    ) {
        LuxuryTopBar(
            title = title,
            subtitle = "${displayedProducts.size} Curated Pieces",
            onBack = onBack,
            actions = {
                if (onOpenSearch != null) {
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("grid_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search jewellery"
                        )
                    }
                }
                IconButton(
                    onClick = { showSortSheet = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("grid_sort_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = "Sort jewellery"
                    )
                }
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("grid_filter_btn")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filter by category, material, or price range"
                        )
                        if (criteria.activeFilterCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = ext.antiqueGold,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = criteria.activeFilterCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                if (onOpenCart != null) {
                    IconButton(
                        onClick = onOpenCart,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("grid_cart_btn")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = "Open shopping bag"
                            )
                            if (cartCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (cartCount > 9) "9+" else cartCount.toString(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )

        // Quick Material + Saved Filter Bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val allSelected = criteria.material == null && !criteria.savedOnly
                FilterChip(
                    selected = allSelected,
                    onClick = { criteria = criteria.copy(material = null, savedOnly = false) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = ext.secondaryCream
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = allSelected,
                        borderColor = ext.hairlineBorder
                    ),
                    modifier = Modifier.testTag("grid_metal_all")
                )
            }
            item {
                FilterChip(
                    selected = criteria.savedOnly,
                    onClick = { criteria = criteria.copy(savedOnly = !criteria.savedOnly) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (criteria.savedOnly) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Saved (${wishlistIds.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = ext.secondaryCream
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = criteria.savedOnly,
                        borderColor = ext.hairlineBorder
                    ),
                    modifier = Modifier.testTag("grid_saved_filter")
                )
            }
            items(SeedData.allMetals) { metal ->
                val selected = criteria.material == metal
                FilterChip(
                    selected = selected,
                    onClick = {
                        criteria = criteria.copy(material = if (selected) null else metal)
                    },
                    label = { Text(metal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = ext.secondaryCream
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = ext.hairlineBorder
                    ),
                    modifier = Modifier.testTag("grid_metal_$metal")
                )
            }
        }

        // Active Filter & Sort Status Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ext.secondaryCream,
                    border = BorderStroke(0.75.dp, ext.hairlineBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showFilterSheet = true }
                ) {
                    Text(
                        text = if (criteria.activeFilterCount > 0) "Filters (${criteria.activeFilterCount})" else "Filter",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ext.champagne,
                    border = BorderStroke(0.75.dp, ext.antiqueGold.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSortSheet = true }
                ) {
                    Text(
                        text = "Sort: ${criteria.sortOrder.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.deepWarmBrown,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            if (criteria.activeFilterCount > 0) {
                Text(
                    text = "Reset",
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.antiqueGold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { criteria = GridFilterCriteria(sortOrder = criteria.sortOrder) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("grid_reset_filters")
                )
            } else if (onOpenWishlist != null && wishlistIds.isNotEmpty()) {
                Text(
                    text = "Open Wishlist ->",
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.antiqueGold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onOpenWishlist)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (displayedProducts.isEmpty()) {
            EmptyLuxuryState(
                icon = if (criteria.savedOnly) Icons.Outlined.FavoriteBorder else Icons.Outlined.Diamond,
                title = if (criteria.savedOnly) "No Saved Pieces Yet" else "No Matching Jewellery",
                subtitle = if (criteria.savedOnly) {
                    "Tap the heart icon on any piece to save it permanently to your Wishlist."
                } else {
                    "No items match your selected category, material, and price range filters."
                },
                actionLabel = "Reset Filters",
                onAction = { criteria = GridFilterCriteria() },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 164.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("luxury_jewellery_grid")
            ) {
                items(displayedProducts, key = { it.id }) { product ->
                    LuxuryJewelleryGridItemCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        onClick = { onProductClick(product) },
                        onToggleWishlist = { onToggleWishlist(product.id) }
                    )
                }
            }
        }
    }

    // Filter Bottom Sheet (Category, Material, Price Range, Saved & Stock)
    if (showFilterSheet) {
        var draft by remember(criteria) { mutableStateOf(criteria) }
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .testTag("grid_filter_sheet")
            ) {
                Text("Filter Jewellery", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Category", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SeedData.allCategories.forEach { cat ->
                        val selected = draft.category == cat
                        FilterChip(
                            selected = selected,
                            onClick = { draft = draft.copy(category = if (selected) null else cat) },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Material", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SeedData.allMetals.forEach { metal ->
                        val selected = draft.material == metal
                        FilterChip(
                            selected = selected,
                            onClick = { draft = draft.copy(material = if (selected) null else metal) },
                            label = { Text(metal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Price Range (INR)", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    JewelleryPriceRange.entries.forEach { range ->
                        val selected = draft.priceRange == range
                        FilterChip(
                            selected = selected,
                            onClick = { draft = draft.copy(priceRange = if (selected) null else range) },
                            label = { Text(range.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { draft = draft.copy(savedOnly = !draft.savedOnly) }
                ) {
                    Checkbox(
                        checked = draft.savedOnly,
                        onCheckedChange = { draft = draft.copy(savedOnly = it) }
                    )
                    Text("Saved wishlist pieces only", style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { draft = draft.copy(inStockOnly = !draft.inStockOnly) }
                ) {
                    Checkbox(
                        checked = draft.inStockOnly,
                        onCheckedChange = { draft = draft.copy(inStockOnly = it) }
                    )
                    Text("Available in stock only", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryOutlinedButton(
                        text = "Clear All",
                        onClick = {
                            criteria = GridFilterCriteria(sortOrder = criteria.sortOrder)
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryOliveButton(
                        text = "Apply Filters",
                        onClick = {
                            criteria = draft
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "grid_apply_filters_btn"
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Sort Bottom Sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .testTag("grid_sort_sheet")
            ) {
                Text("Sort Collection", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(12.dp))
                GridSortOrder.entries.forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                criteria = criteria.copy(sortOrder = order)
                                showSortSheet = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = criteria.sortOrder == order,
                            onClick = {
                                criteria = criteria.copy(sortOrder = order)
                                showSortSheet = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = order.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun LuxuryJewelleryGridItemCard(
    product: Product,
    isWishlisted: Boolean,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit
) {
    val ext = LocalAureliaColors.current
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("grid_item_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, ext.hairlineBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(ext.secondaryCream)
            ) {
                JewelleryPhoto(
                    imageKey = product.imageKey,
                    customImageUri = product.customImageUri,
                    metal = product.metal,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    border = BorderStroke(0.5.dp, ext.hairlineBorder),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = product.purity.ifBlank { product.metal },
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.deepWarmBrown,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleWishlist()
                        }
                        .testTag("grid_wishlist_${product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isWishlisted) "Remove from wishlist" else "Save to wishlist",
                        tint = if (isWishlisted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 10.dp)
            ) {
                Text(
                    text = product.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = ext.antiqueGold,
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = InrFormatter.format(product.priceInr),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (product.originalPriceInr != null && product.originalPriceInr > product.priceInr) {
                        Text(
                            text = InrFormatter.format(product.originalPriceInr),
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.LineThrough,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LuxuryJewelleryGridScreenPreview() {
    AureliaJewelsTheme {
        LuxuryJewelleryGridScreen(
            products = SeedData.defaultProducts().take(6),
            wishlistIds = setOf("prod_ring_floral_gold"),
            cartCount = 2,
            onProductClick = {},
            onToggleWishlist = {}
        )
    }
}
